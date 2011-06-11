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

import org.agilewiki.kernel.element.operation.signature.ElementSig0
import kernel.element.operation.ElementOperation
import kernel.element._
import rel.GetRelValue
import kernel.TransactionContext

object PreValidate extends PreValidate {
  def apply() = new PreValidate
}

class PreValidate
  extends ElementOperation
  with ElementSig0[Unit] {

  protected var target: Element = _
  protected var actions: EmbeddedContainerElement = _
  protected var je: RolonRootElement = _

  override def process(roleName: String,
                       target: Element) {
    actions = target.getVisibleContainer.asInstanceOf[EmbeddedContainerElement]
    je = actions.getVisibleContainer.asInstanceOf[RolonRootElement]
    this.target = target
    reorder(relations)
  }

  def relations: java.util.Set[String] = {
    val list = new java.util.HashSet[String]
    val uuid = target.getJitName
    val subj = TransactionContext().rolonRootElement(uuid)
    val relationshipsSpecs = target.asInstanceOf[TreeMapElement].contents.get("relationshipsSpecs").
      asInstanceOf[EmbeddedContainerElement]
    if (relationshipsSpecs == null) return list
    val rit = relationshipsSpecs.contents.iterator
    while (rit.hasNext) {
      var relType = rit.next
      val relationshipsSpec = relationshipsSpecs.contents.get(relType).asInstanceOf[EmbeddedElement]
      if (relationshipsSpec != null) {
        val it = relationshipsSpec.attributes.iterator
        while (it.hasNext) {
          var objUuid = it.next
          if (objUuid.startsWith("@")) {
            val value = relationshipsSpec.attributes.get(objUuid)
            objUuid = objUuid.substring(1)
            if (value == "%" || value.startsWith("$")) {
              if (GetRelValue(subj, relType, objUuid) == null) {
                throw MissingRelationshipException(uuid, objUuid, relType)
              }
            }
            else {
              list.add(relType + " " + objUuid)
              if (GetRelValue(subj, relType, objUuid) != null) {
                throw DuplicateRelationshipException(uuid, objUuid, relType)
              }
            }
          }
        }
      }
    }
    list
  }

  def reorder(list: java.util.Set[String]) {
    val uuid = target.getJitName
    val subj = TransactionContext().rolonRootElement(uuid)
    val relationshipsSpecs = target.asInstanceOf[TreeMapElement].contents.get("relationshipsSpecs").
      asInstanceOf[EmbeddedContainerElement]
    val reorderSpecs = target.asInstanceOf[TreeMapElement].contents.get("reorderSpecs").asInstanceOf[OrderedElement]
    if (reorderSpecs == null) return
    val oit = reorderSpecs.contents.iterator
    while (oit.hasNext) {
      val relType = oit.next
      var relationshipSpecs: EmbeddedElement = null
      if (relationshipsSpecs != null) {
        relationshipSpecs = relationshipsSpecs.contents.get(relType).asInstanceOf[EmbeddedElement]
      }
      val reorderSpec = reorderSpecs.contents.get(relType).asInstanceOf[EmbeddedElement]
      val it = reorderSpec.attributes.iterator
      while (it.hasNext) {
        var moveObjUuid = it.next
        if (moveObjUuid.startsWith("$")) {
          val beforeObjUuid = reorderSpec.attributes.get(moveObjUuid).substring(1)
          moveObjUuid = moveObjUuid.substring(1)
          if (!list.contains(relType + " " + moveObjUuid))
            exists(relType, moveObjUuid, relationshipSpecs, subj)
          if (!list.contains(relType + " " + beforeObjUuid))
            exists(relType, beforeObjUuid, relationshipSpecs, subj)
        }
      }
    }
  }

  private def exists(relType: String, objUuid: String, relationshipSpecs: EmbeddedElement, subj: RolonRootElement) {
    if (relationshipSpecs != null) {
      val value = relationshipSpecs.attributes.get("@" + objUuid)
      if (value != null)
        if (value == "$") {
          throw MissingRelationshipException(subj.uuid, objUuid, relType)
        }
        else if (GetRelValue(subj, relType, objUuid) == null) {
          throw MissingRelationshipException(subj.uuid, objUuid, relType)
        }
    } else if (GetRelValue(subj, relType, objUuid) == null) {
      throw MissingRelationshipException(subj.uuid, objUuid, relType)
    }
  }

  override def operationType = classOf[PreValidate].getName
}
