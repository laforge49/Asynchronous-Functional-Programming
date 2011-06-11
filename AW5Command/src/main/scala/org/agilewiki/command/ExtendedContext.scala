/*
 * Copyright 2010 Bill La Forge
 *
 * This file is part of AgileWiki and is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License (LGPL) as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 * or navigate to the following url http://www.gnu.org/licenses/lgpl-2.1.txt
 *
 * Note however that only Scala, Java and JavaScript files are being covered by LGPL.
 * All other files are covered by the Common Public License (CPL).
 * A copy of this license is also included and can be
 * found as well at http://www.opensource.org/licenses/cpl1.0.txt
 */
package org.agilewiki
package command

import kernel.operation.Role
import actors.application.ReservedAttributes
import actors.application.Context
import java.util._
import util.actors._
import groovy.lang.Binding
import kernel.Kernel
import util.sequence.actors.basic.composites.{InvertedTimestampSequenceActor, PrefixSequenceActor, UnionSequenceActor, IntersectionSequenceActor}
import util.sequence.actors.basic.composits.SubSequenceActor
import util.sequence.actors.basic.NavigableSequenceActor
import util.sequence.actors.SequenceConvenience
import kernel.element.RolonRootElement
import core.batch._
import actors.application.query.{ObjectIdentifiers, Relationships}
import actors.sequences.rel.{SubjectUuidsSequenceAgent, SubjectValuesSequenceAgent, SubjectKeysSequenceAgent, SubjectTypesSequenceAgent}
import util.sequence.actors.basic.EmptySequenceActor
import actors.sequences.JournalEntryTimestampSequence
import core.CoreNames
import util.{RolonName, UuidExtension, Configuration, Timestamp}

trait ExtendedContext extends InternalAddress with CoreNames {
  this: Agent =>

  val reservedAttributes: Set[String] = ReservedAttributes

  val updateParameters: Map[String, Any] = new HashMap[String, Any]

  var xmlComposer: XmlComposer = null

  val userGroupsMap = new HashMap[String, HashSet[String]]

  protected var journalEntry: RolonRootElement = null

  private var generatedRolonName: RolonName = null

  def generateUuid(role: String) = {
    val uuid = Kernel(localContext).generateUuid(role)
    generatedRolonName = RolonName(uuid)
    uuid
  }

  def createJournalEntry(context: Context, tagLine: String) {
    journalEntry = BatchJE(localContext, String.valueOf(context.get("timestamp")), tagLine, context.get("user.uuid"))
  }

  def addJournalEntryRelationship(relType: String,
                                  objUuid: String,
                                  value: String) {
    BatchJE.addRelationship(journalEntry, relType, objUuid, value)
  }

  def addJournalEntryRelationships(userUuid: String, commandUuid: String) {
    addJournalEntryRelationship(USER_RELATIONSHIP, userUuid, " ")
    addJournalEntryRelationship(COMMAND_RELATIONSHIP, commandUuid, " ")
  }

  def delete(uuid: String) {
    BatchJE.delete(journalEntry, uuid)
  }

  def dependency(uuid: String) {
    BatchJE.update(journalEntry, uuid)
  }

  def create(role: String): String = {
    BatchJE.create(journalEntry, role)
  }

  def create {
    BatchJE.create(journalEntry, generatedRolonName)
  }

  def beforeSpec(uuid: String,
            relType: String,
            moveObjUuid: String,
            beforeObjUuid: String) {
    ReorderSpec.before(journalEntry, uuid, relType, moveObjUuid, beforeObjUuid)
  }

  def afterSpec(uuid: String,
            relType: String,
            moveObjUuid: String,
            afterObjUuid: String) {
    ReorderSpec.after(journalEntry, uuid, relType, moveObjUuid, afterObjUuid)
  }

  def attributeSpec(uuid: String,
                    attName: String, attValue: String) {
    if (attName == "password") throw new IllegalArgumentException
    AttributeSpec(journalEntry, uuid, attName, attValue)
  }

  def passwordSpec(uuid: String, password: String) {
    if (password == null || password.length < 6) throw new IllegalArgumentException
    AttributeSpec(journalEntry, uuid, "password", "disIsAPazzwrd:" + password)
  }

  def documentSpec(uuid: String,
                   bytes: Array[Byte]) {
    DocumentSpec(journalEntry, uuid, bytes)
  }

  def addRelationship(subjUuid: String, relType: String, objUuid: String, value: String) {
    RelationshipSpec.add(journalEntry, subjUuid, relType, objUuid, value)
  }

  def updateRelationship(subjUuid: String, relType: String, objUuid: String, value: String) {
    RelationshipSpec.update(journalEntry, subjUuid, relType, objUuid, value)
  }

  def removeRelationship(subjUuid: String, relType: String, objUuid: String) {
    RelationshipSpec.remove(journalEntry, subjUuid, relType, objUuid)
  }

  def assignViewParameters(context: Context) {
    updateParameters.put("_.xml", context.get("_.xml"))
    updateParameters.put("_.window", context.get("_.window"))
    updateParameters.put("_.rolonUuid", context.get("_.rolonUuid"))
    updateParameters.put("_.path", context.get("_.path"))
    updateParameters.put("_.desc", context.get("_.desc"))
  }

  def role(name: String): Role = Kernel(localContext).role(name)

  def singletonUuid(roleName: String): String = CommandLayer(localContext).singletonUuid(roleName)

  def userGroups(context: Context, privilege: String): java.util.HashSet[String] = {
    if (userGroupsMap.containsKey(privilege)) return userGroupsMap.get(privilege)
    val ug = _userGroups(context, privilege)
    userGroupsMap.put(privilege, ug)
    ug
  }

  private def _userGroups(context: Context, privilege: String): HashSet[String] = {
    val groupUuids = new HashSet[String]
    val uUuid = context.get("user.uuid")
    val uTimestamp = context.get("user.timestamp")
    val uVersionId = uTimestamp + "|" + uUuid
    var uRelationships = context.getSpecial(uVersionId + ".relationships").asInstanceOf[Relationships]
    if (uRelationships == null) return groupUuids
    val uMemberObjectIdentifiers = uRelationships.get(MEMBER_RELATIONSHIP).asInstanceOf[ObjectIdentifiers]
    if (uMemberObjectIdentifiers == null) return groupUuids
    val pl = privilegeLevel(privilege)
    val it = uMemberObjectIdentifiers.list.iterator
    while (it.hasNext) {
      val objectIdentifier = it.next
      val objUuid = objectIdentifier.uuid
      val value = objectIdentifier.value
      val objRoleName = UuidExtension(objUuid)
      val objRole = role(objRoleName)
      if (objRole.isA(MY_GROUP_TYPE)) {
        val gpl = privilegeLevel(value)
        if (gpl >= pl) {
          if (!groupUuids.contains(objUuid)) {
            groupUuids.add(objUuid)
            val roleSet = objRole.superRoles
            roleSet.foreach{
              rnm => {
                val r = role(rnm)
                if (r.isA(MY_GROUP_TYPE)) {
                  val rsu = singletonUuid(rnm)
                  groupUuids.add(rsu)
                }
              }
            }
          }
        }
      }
    }
    groupUuids
  }

  def privilegeLevel(privilege: String) = CommandLayer(localContext).versionCache.privilegeLevel(privilege)

  def hasPrivilege(context: Context, rolonUuid: String, privilege: String): Boolean =
    CommandLayer(localContext).versionCache.hasPrivilege(localContext, context, rolonUuid, privilege)

  def modelAccess(context: Context, rolonUuid: String, modelUuid: String) {
    val timestamp = context.get("timestamp")
    val mVersionId = timestamp + "|" + modelUuid
    val mRelationships = context.getSpecial(mVersionId + ".relationships").asInstanceOf[Relationships]
    val limitIdentifiers = mRelationships.get(LIMIT_RELATIONSHIP)
    if (limitIdentifiers != null) {
      val it = limitIdentifiers.list.iterator
      while (it.hasNext) {
        val objectIdentifier = it.next
        addRelationship(rolonUuid, LIMIT_RELATIONSHIP, objectIdentifier.uuid, objectIdentifier.value)
      }
    }
    val accessIdentifiers = mRelationships.get(ACCESS_RELATIONSHIP)
    if (accessIdentifiers != null) {
      val userUuid = context.get("user.uuid")
      addJournalEntryRelationship(ACCESS_RELATIONSHIP, userUuid, "reader")
      val it = accessIdentifiers.list.iterator
      while (it.hasNext) {
        val objectIdentifier = it.next
        addRelationship(rolonUuid, ACCESS_RELATIONSHIP, objectIdentifier.uuid, objectIdentifier.value)
      }
    }
  }

  def modelJournalEntryAccess(context: Context, modelUuid: String) {
    val timestamp = context.get("timestamp")
    val mVersionId = timestamp + "|" + modelUuid
    val mRelationships = context.getSpecial(mVersionId + ".relationships").asInstanceOf[Relationships]
    val limitIdentifiers = mRelationships.get(LIMIT_RELATIONSHIP)
    if (limitIdentifiers != null) {
      val it = limitIdentifiers.list.iterator
      while (it.hasNext) {
        val objectIdentifier = it.next
        if (objectIdentifier.value == "none")
          addJournalEntryRelationship(LIMIT_RELATIONSHIP, objectIdentifier.uuid, objectIdentifier.value)
      }
    }
    val accessIdentifiers = mRelationships.get(ACCESS_RELATIONSHIP)
    if (accessIdentifiers != null) {
      if (accessIdentifiers.map.containsKey(ACCESS_UUID))
        addJournalEntryRelationship(ACCESS_RELATIONSHIP, ACCESS_UUID, "reader")
      else {
        val userUuid = context.get("user.uuid")
        addJournalEntryRelationship(ACCESS_RELATIONSHIP, userUuid, "reader")
        val it = accessIdentifiers.list.iterator
        while (it.hasNext) {
          val objectIdentifier = it.next
          val uuid = objectIdentifier.uuid
          addJournalEntryRelationship(ACCESS_RELATIONSHIP, uuid, "reader")
        }
      }
    }
  }

  def getValue(context: Context, subjUuid: String, relType: String, objUuid: String): String = {
    val timestamp = context.get("timestamp")
    val versionId = timestamp + "|" + subjUuid
    val relationships = context.getSpecial(versionId + ".relationships").asInstanceOf[Relationships]
    if (relationships == null) return null
    val identifiers = relationships.get(relType)
    if (identifiers == null) return null
    identifiers.value(objUuid)
  }

  def modelJournalEntryAccess(context: Context, modelUuid: String, excludeUuid: String) {
    val timestamp = context.get("timestamp")
    val mVersionId = timestamp + "|" + modelUuid
    val mRelationships = context.getSpecial(mVersionId + ".relationships").asInstanceOf[Relationships]
    val limitIdentifiers = mRelationships.get(LIMIT_RELATIONSHIP)
    if (limitIdentifiers != null) {
      val it = limitIdentifiers.list.iterator
      while (it.hasNext) {
        val objectIdentifier = it.next
        val uuid = objectIdentifier.uuid
        if (objectIdentifier.value == "none" && uuid!=excludeUuid)
          addJournalEntryRelationship(LIMIT_RELATIONSHIP, uuid, objectIdentifier.value)
      }
    }
    val accessIdentifiers = mRelationships.get(ACCESS_RELATIONSHIP)
    if (accessIdentifiers != null) {
      val userUuid = context.get("user.uuid")
      addJournalEntryRelationship(ACCESS_RELATIONSHIP, userUuid, "reader")
      val it = accessIdentifiers.list.iterator
      while (it.hasNext) {
        val objectIdentifier = it.next
        val uuid = objectIdentifier.uuid
        if (uuid != excludeUuid)
          addJournalEntryRelationship(ACCESS_RELATIONSHIP, uuid, "reader")
      }
    }
  }

  def realmUuid(context: Context, uuid: String): String = {
    val timestamp = context.get("timestamp")
    val versionId = timestamp + "|" + uuid
    val relationships = context.getSpecial(versionId + ".relationships").asInstanceOf[Relationships]
    val realmIdentifiers = relationships.get(REALM_RELATIONSHIP)
    if (realmIdentifiers == null) return null
    val list = realmIdentifiers.list
    val identifier = list.get(0)
    return identifier.uuid
  }

  def addJournalEntryChangeRealmRelationship(context: Context, uuid: String) {
    val realm = realmUuid(context, uuid)
    if (realm == null) return
    addJournalEntryRelationship(context.REALM_CHANGE_RELATIONSHIP, realm, " ")
  }

  def propertyNames: NavigableSet[String] = Configuration(localContext).propertyNames

  def invertTimestamp(timestamp: String) = Timestamp.invert(timestamp)

  def groovySequence(context: Context, groovySequenceCommand: String): SequenceConvenience = {
    val gb = new Binding
    gb.setVariable("context", context)
    val cmdName = "seq_" + groovySequenceCommand + ".groovy"
    CommandLayer(localContext).gse.run(cmdName, gb)
    var loopPrefix = context.get("loopPrefix")
    if (loopPrefix != "") loopPrefix += "."
    context.getSpecial(loopPrefix + "sequence").asInstanceOf[SequenceConvenience]
  }

  def navigableSequence(set: NavigableSet[String], reverse: Boolean): NavigableSequenceActor =
    NavigableSequenceActor(localContext, set, reverse)

  def emptySequence = EmptySequenceActor(localContext)

  def intersectionSequence(sequences: List[SequenceConvenience], reverse: Boolean) = IntersectionSequenceActor(localContext, sequences, reverse)

  def unionSequence(sequences: List[SequenceConvenience], reverse: Boolean) = UnionSequenceActor(localContext, sequences, reverse)

  def subSequence(wrappedSequence: SequenceConvenience, prefix: String) = SubSequenceActor(localContext, wrappedSequence, prefix)

  def prefixesSequence(wrappedSequence: SequenceConvenience, delimiter: Char) = PrefixSequenceActor(localContext, wrappedSequence, delimiter)

  def invertedTimestampSequence(wrappedSequence: SequenceConvenience) = InvertedTimestampSequenceActor(localContext, wrappedSequence)

  def contextFilterSequence(context: Context, wseq: SequenceConvenience, filterName: String) =
    ContextFilterSequenceActor(localContext, context, wseq, filterName, 0.asInstanceOf[Char])

  def contextFilterSequence(context: Context, wseq: SequenceConvenience, filterName: String, delimiter: Char) =
    ContextFilterSequenceActor(localContext, context, wseq, filterName, delimiter)

  def subjectTypesSequence(timestamp: String,
                           objUuid: String) = SubjectTypesSequenceAgent(
    localContext,
    Configuration(localContext).localServerName,
    timestamp,
    objUuid)

  def subjectKeysSequence(timestamp: String,
                          objUuid: String,
                          relType: String) = SubjectKeysSequenceAgent(
    localContext,
    Configuration(localContext).localServerName,
    timestamp,
    objUuid,
    relType)

  def subjectValuesSequence(timestamp: String,
                            objUuid: String,
                            relType: String) = SubjectValuesSequenceAgent(
    localContext,
    Configuration(localContext).localServerName,
    timestamp,
    objUuid,
    relType)

  def subjectUuidsSequence(timestamp: String,
                           objUuid: String,
                           relType: String,
                           value: String) = SubjectUuidsSequenceAgent(
    localContext,
    Configuration(localContext).localServerName,
    timestamp,
    objUuid,
    relType,
    value)

  def objectsSequence(objectIdentifiers: ObjectIdentifiers): SequenceConvenience =
    objectIdentifiers.sequence(localContext)

  def objectsSequence(relationships: Relationships, relType: String): SequenceConvenience = {
    val objectIdentifiers = relationships.get(relType)
    if (objectIdentifiers == null) EmptySequenceActor(localContext)
    else objectsSequence(objectIdentifiers)
  }

  def objectTypesSequence(relationships: Relationships) = {
    val keys = relationships.navigableKeySet
    NavigableSequenceActor(localContext, keys, false)
  }

  def templateFilterSequence(context: Context, wrappedSequence: SequenceConvenience, templatePath: String): SequenceConvenience =
    templateFilterSequence(context, wrappedSequence, templatePath, 0.asInstanceOf[Char])

  def templateFilterSequence(context: Context, wrappedSequence: SequenceConvenience, templatePath: String, delimiter: Char): SequenceConvenience

  def journalEntryTimestampSequence(context: Context): JournalEntryTimestampSequence =
    JournalEntryTimestampSequence(localContext, context.get("timestamp"))

  override def toString = "Extended Context"

  def pushToClient(data: String) {
    throw new UnsupportedOperationException
  }

  def pushToUser(data: String) {
    throw new UnsupportedOperationException
  }

  def keySequence(tm: TreeMap[String, Object]) = {
    val nks = tm.navigableKeySet
    navigableSequence(nks, false)
  }

  def getValue(tm: TreeMap[String, Object], key: String) = {
    val rv = tm.get(key)
    rv
  }
}