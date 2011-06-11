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
package core
package batch

import kernel._
import kernel.operation.Eval
import util.jit.Jits
import kernel.{JournalEntryFactory, Kernel, TransactionContext}
import kernel.element._
import rel.SetRelValue
import util.{RolonName, Timestamp, SystemComposite}

object BatchJE {
  def apply(systemContext: SystemComposite,
            queryTimestamp: String,
            tagLine: String,
            userUuid: String): RolonRootElement = {
    val journalEntryRolon = Jits(systemContext).createJit(ROLON_ROOT_ELEMENT_ROLE_NAME).asInstanceOf[RolonRootElement]
    if (CoreNames.ANONYMOUS_UUID == userUuid) {
      return journalEntryRolon
    }
    journalEntryRolon.setRolonType(CoreNames.BATCH_TYPE)
    journalEntryRolon.attributes.put("queryTimestamp", queryTimestamp)
    journalEntryRolon.attributes.put("tagLine", tagLine)
    journalEntryRolon.contents.add("actions", ORDERED_ELEMENT_ROLE_NAME)
    val actions = journalEntryRolon.contents.get("actions").asInstanceOf[EmbeddedContainerElement]
    journalEntryRolon
  }

  def apply(systemContext: SystemComposite, bytes: Array[Byte]) = {
    val bje = Jits(systemContext).createJit(ROLON_ROOT_ELEMENT_ROLE_NAME).asInstanceOf[RolonRootElement]
    bje.loadJit(bytes)
    bje
  }

  def addRelationship(journalEntryRolon: RolonRootElement,
                      relType: String, objectUuid: String, value: String) {
    if (relType == CoreNames.PARENT_RELATIONSHIP)
      throw new IllegalArgumentException("journal entry may not have a parent")
    val i = objectUuid.indexOf("_")
    if (objectUuid.substring(0, i) != objectUuid.substring(i + 1))
      update(journalEntryRolon, objectUuid)
    val relationshipsSpecs = journalEntryRolon.contents.make("relationshipsSpecs", TREE_MAP_ELEMENT).
      asInstanceOf[TreeMapElement]
    val relationshipsSpec = relationshipsSpecs.contents.make(relType, EMBEDDED_ELEMENT_ROLE_NAME).
      asInstanceOf[EmbeddedElement]
    val rv = if (value == null) "" else value
    relationshipsSpec.attributes.put("@" + objectUuid, "$" + rv)
  }

  def delete(journalEntryRolon: RolonRootElement,
             uuid: String) {
    val actions = journalEntryRolon.contents.get("actions").asInstanceOf[EmbeddedContainerElement]
    val deleteAction = actions.contents.add(uuid, DELETE_ACTION_ROLE_NAME).asInstanceOf[TreeMapElement]
  }

  def update(journalEntryRolon: RolonRootElement,
             uuid: String) = {
    val actions = journalEntryRolon.contents.get("actions").asInstanceOf[EmbeddedContainerElement]
    val updateAction = actions.contents.make(uuid, UPDATE_ACTION_ROLE_NAME).asInstanceOf[TreeMapElement]
    updateAction
  }

  def create(journalEntryRolon: RolonRootElement, role: String): String = {
    val uuid = Kernel(journalEntryRolon.systemContext).generateUuid(role)
    val actions = journalEntryRolon.contents.get("actions").asInstanceOf[EmbeddedContainerElement]
    val createAction = actions.contents.add(uuid, CREATE_ACTION_ROLE_NAME).asInstanceOf[TreeMapElement]
    uuid
  }

  def create(journalEntryRolon: RolonRootElement, rolonName: RolonName) {
    val actions = journalEntryRolon.contents.get("actions").asInstanceOf[EmbeddedContainerElement]
    val createAction = actions.contents.add(rolonName.rolonUuid, CREATE_ACTION_ROLE_NAME).asInstanceOf[TreeMapElement]
  }

  def journalEntryFactory(journalEntryRolon: RolonRootElement, startingTime: String) = {
    new JournalEntryFactory {
      override def assignStartingTime = {
        val kernel = Kernel(journalEntryRolon.systemContext)
        if (kernel.oldStartingTime != null && kernel.oldStartingTime > startingTime) {
          throw new OutOfOrderException
        }
        kernel.oldStartingTime = startingTime
        startingTime
      }

      override def preValidate {
        val actions = journalEntryRolon.contents.get("actions").asInstanceOf[EmbeddedContainerElement]
        val it = actions.contents.iterator
        while (it.hasNext) {
          val actionName = it.next
          TransactionContext().actionName = actionName
          val action = actions.contents.get(actionName).asInstanceOf[Element]
          PreValidate(action)
        }
      }

      override def journalEntry = {
        TransactionContext().putJournalEntry(journalEntryRolon)
        journalEntryRolon
      }
    }
  }

  def processTransaction(journalEntryRolon: RolonRootElement) = {
    val kernel = Kernel(journalEntryRolon.systemContext)
    val jef = journalEntryFactory(journalEntryRolon, Timestamp.timestamp)
    kernel.transactionPhase1(jef)
    val jeUuid = kernel.transactionPhase2
    kernel.transactionPhase3
    jeUuid
  }
}

class BatchJE extends Eval {
  override def process(role: String, context: RolonRootElement, target: RolonRootElement) = {
    var tagLine = target.attributes.get("tagLine")
    if (tagLine != null) {
      val i = tagLine.indexOf(":")
      if (i > -1) tagLine = tagLine.substring(0, i)
      TransactionContext().effected(tagLine)
    }

    val actions = target.contents.get("actions").asInstanceOf[EmbeddedContainerElement]
    val it = actions.contents.iterator
    while (it.hasNext) {
      val actionName = it.next
      TransactionContext().actionName = actionName
      val action = actions.contents.get(actionName).asInstanceOf[Element]
      Action(action)
    }

    val relationshipsSpecs = target.contents.get("relationshipsSpecs").asInstanceOf[TreeMapElement]
    if (relationshipsSpecs != null) {
      val its = relationshipsSpecs.contents.iterator
      while (its.hasNext) {
        val relType = its.next
        val relationshipsSpec = relationshipsSpecs.contents.get(relType).asInstanceOf[EmbeddedElement]
        val it = relationshipsSpec.attributes.iterator
        while (it.hasNext) {
          var objUuid = it.next
          if (objUuid.startsWith("@")) {
            var value = relationshipsSpec.attributes.get(objUuid)
            value = if (value == "$") null else value.substring(1)
            objUuid = objUuid.substring(1)
            SetRelValue(target, relType, objUuid, value)
          }
        }
      }
    }
  }
}
