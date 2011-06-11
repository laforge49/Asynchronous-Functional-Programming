/*
 * Copyright 2010 M.Naji
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
package actors
package application
package query

import util.actors._
import java.util.HashMap
import java.util.ArrayList
import java.util.TreeMap
import java.util.UUID
import res.ClassName
import util.sequence.actors._
import util.com.{DataOutputStack, DataInputStack}
import util.{UuidExtension, SystemComposite, RolonName}
import kernel.KernelNames

case class RolonResponse(
                          rolonName: RolonName,
                          attributes: Map[String, String],
                          relationships: Map[String, List[ObjectIdentifier]],
                          effectedRolons: Map[String, Map[String, Map[String, String]]],
                          document: Array[Byte]) extends ApplicationData {
  override def payload(dos: DataOutputStack) = {
    if (document == null || document.length == 0) dos.writeInt(0)
    else {
      dos.write(document)
      dos.writeInt(document.length)
    }
    dos.writeMapMapMap(effectedRolons)
    dos.writeMap[String, List[ObjectIdentifier]](relationships, "RELATIONSHIPS",
      (relType: String, dos: DataOutputStack) => dos.writeUTF(relType),
      (objectIdentifiers: List[ObjectIdentifier], dos: DataOutputStack) => {
        dos.writeList[ObjectIdentifier](objectIdentifiers, "OBJECTS",
          (objectIdentifier: ObjectIdentifier, dos: DataOutputStack) => {
            dos.writeUTF(objectIdentifier.value)
            dos.writeUTF(objectIdentifier.uuid)
          })
      })
    dos.writeStringMap(attributes)
    dos.writeRolonName(rolonName)
    dos.writeUTF(ROLON_RESPONSE)
    dos
  }

  private[application] override def sendableMessage(source: InternalAddress, header: Any) = {
    throw new UnsupportedOperationException
  }

  def uuid = rolonName.rolonUuid

  def load(versionId: String, context: Context) {
    if (context.get(versionId + ".role") != "") return
    val typeName = attributes("rolonType")
    context.setCon(versionId + ".role", typeName)
    if (attributes.contains("lastcommand"))
      context.setCon(versionId + ".lastCommand", attributes("lastcommand"))
    if (attributes.contains("password")) {
      val value = attributes("password")
      val password = new Password(value)
      context.setSpecial(versionId + ".password", password)
    }
    attributes.foreach{
      x =>
        if (!ReservedAttributes.contains(x._1)) {
          if (x._2.contains("disIsAPazzwrd:"))
            context.setCon(versionId + ".att." + x._1, UUID.randomUUID.toString)
          else
            context.setCon(versionId + ".att." + x._1, x._2)
        }
    }
    val rels = new Relationships(relationships)
    context.setSpecial(versionId + ".relationships", rels)
    if (typeName == KernelNames.HOME_TYPE) context.setCon(versionId + ".name", KernelNames.HOME_NAME)
    else {
      val parents = rels.get(context.PARENT_RELATIONSHIP)
      if (parents != null && parents.list.size > 0) {
        val first = parents.list.get(0)
        val firstName = first.value
        context.setCon(versionId + ".name", firstName)
      }
    }
    val tm = new EffectedRolons(effectedRolons)()
    context.setSpecial(versionId + ".effectedRolons", tm)
    context.setSpecial(versionId + ".document", document)
  }

  def getAttribute(key: String) = attributes.get(key) match {
    case None => ""
    case Some(value) => value
  }

}

object RolonResponse {
  def apply(payload: DataInputStack): RolonResponse = {
    val rolonName = payload.readRolonName
    val attributes = payload.readStringMap
    val relationships = payload.readMap[String, List[ObjectIdentifier]]("RELATIONSHIPS",
      (dis: DataInputStack) => dis.readUTF,
      (dis: DataInputStack) => {
        dis.readList[ObjectIdentifier]("OBJECTS", (dis: DataInputStack) => {
          ObjectIdentifier(dis.readUTF, dis.readUTF)
        })
      })
    val effectedRolons = payload.readMapMapMap
    val docLen = payload.readInt
    var document = new Array[Byte](docLen)
    if (docLen > 0) payload.readFully(document)
    RolonResponse(rolonName, attributes, relationships, effectedRolons, document)
  }
}

class
EffectedRolons(effectedRolons: Map[String, Map[String, Map[String, String]]]) {
  def apply() = {
    val tm = new TreeMap[String, TreeMap[String, TreeMap[String, String]]]
    effectedRolons.foreach{
      (t2) => {
        val uuid = t2._1
        val elements = t2._2
        val eleTree = new TreeMap[String, TreeMap[String, String]]
        elements.foreach{
          e => {
            val enbr = e._1
            val atts = e._2
            val attTree = new TreeMap[String, String]
            atts.foreach{
              x => {
                if (x._2 != null && x._2.startsWith("disIsAPazzwrd:"))
                  attTree.put(x._1, UUID.randomUUID.toString)
                else
                  attTree.put(x._1, x._2)
              }
            }
            eleTree.put(enbr, attTree)
          }
        }
        tm.put(uuid, eleTree)
      }
    }
    tm
  }
}

class Relationships(relationships: Map[String, List[ObjectIdentifier]])
  extends java.util.TreeMap[String, ObjectIdentifiers] {
  relationships.foreach{
    x => put(x._1, new ObjectIdentifiers(x._2))
  }

  def value(relType: String, objUuid: String) = {
    val identifiers = get(relType)
    if (identifiers != null) identifiers.value(objUuid) else null
  }
}

class ObjectIdentifiers(objectIdentifiers: List[ObjectIdentifier]) {
  val list = new ArrayList[ObjectIdentifier]()
  val map = new HashMap[String, Int]
  var ndx = 0
  objectIdentifiers.foreach{
    x => {
      map.put(x.uuid, ndx)
      list.add(x)
      ndx += 1
    }
  }

  def indexOf(objectUuid: String) = map.get(objectUuid)

  def uuid(i: Int) = list.get(i).uuid

  def role(i: Int) = UuidExtension(list.get(i).uuid)

  def value(i: Int): String = list.get(i).value

  def value(objectUuid: String): String = {
    if (map.containsKey(objectUuid))
      value(indexOf(objectUuid))
    else null
  }

  def sequence(systemContext: SystemComposite) = {
    val seq = Actors(systemContext).actorFromClassName(ClassName(classOf[ObjectsSequenceActor])).asInstanceOf[ObjectsSequenceActor]
    seq.objectIdentifiers = this
    seq
  }

  override def toString = "Object Identifiers"
}

class ObjectsSequenceActor(systemContext: SystemComposite, uuid: String)
  extends AsynchronousActor(systemContext, uuid) with SequenceConvenience {
  var objectIdentifiers: ObjectIdentifiers = null

  final override def messageHandler = {
    case msg: CurrentMsg => current(msg)
    case msg: NextMsg => next(msg)
    case msg => unexpectedMsg(msg)
  }

  private def key(i: Int) = objectIdentifiers.uuid(i) + 5.asInstanceOf[Char] + objectIdentifiers.value(i)

  private def uuid(key: String) = {
    var j = key.indexOf(5.asInstanceOf[Char])
    if (j == -1) j = key.indexOf(6.asInstanceOf[Char])
    key.substring(0, j)
  }

  private def ndx(key: String) = objectIdentifiers.map.get(uuid(key))

  private def current(msg: CurrentMsg) {
    var tmp: String = null
    if (!objectIdentifiers.list.isEmpty) {
      if (msg.key == null) {
        if (lastResult == null) {
          tmp = key(0)
        } else tmp = lastResult
      } else {
        tmp = key(ndx(msg.key))
      }
    }
    if (tmp == null) msg.requester ! EndMsg(msg.header)
    else {
      lastResult = tmp
      msg.requester ! ResultMsg(msg.header, lastResult)
    }
  }

  private def next(msg: NextMsg) {
    var tmp: String = null
    if (!objectIdentifiers.list.isEmpty) {
      if (msg.key == null) {
        if (lastResult == null) {
          tmp = key(0)
        } else {
          val i = ndx(lastResult) + 1
          if (i < objectIdentifiers.list.size) tmp = key(i)
        }
      } else {
        val i = ndx(msg.key) + 1
        if (i < objectIdentifiers.list.size) tmp = key(i)
      }
    }
    if (tmp == null) msg.requester ! EndMsg(msg.header)
    else {
      lastResult = tmp
      msg.requester ! ResultMsg(msg.header, lastResult)
    }
  }
}
