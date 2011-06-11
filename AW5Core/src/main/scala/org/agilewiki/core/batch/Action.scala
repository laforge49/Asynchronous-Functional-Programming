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
import kernel.TransactionContext
import util.Timestamp
import rel.{GetObjectLinks, SetRelValue}

object Action extends Action {
  def apply() = new Action
}

class Action
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
  }

  protected def getRolon = {
    val uuid = target.getJitName
    TransactionContext().rolonRootElement(uuid)
  }

  protected def update(rolon: RolonRootElement) {
    val ait = target.attributes.iterator
    while (ait.hasNext) {
      var attName = ait.next
      if (attName.startsWith("@")) {
        var attValue = target.attributes.get(attName)
        attName = attName.substring(1)
        if (attValue == "$") attValue = null
        else attValue = attValue.substring(1)
        rolon.attributes.put(attName, attValue)
      }
    }

    var updateTimestamp = je.uuid
    val i = updateTimestamp.indexOf("_")
    updateTimestamp = updateTimestamp.substring(0, i)
    updateTimestamp = Timestamp.invert(updateTimestamp)
    rolon.attributes.put("updateTimestamp", updateTimestamp)

    val oldCount = rolon.attributes.getInt("childCount", 0)
    val inc = target.attributes.getInt("childInc", 0)
    val childCount = oldCount + inc
    rolon.attributes.putInt("childCount", childCount, 0)

    val relationshipsSpecs = target.asInstanceOf[TreeMapElement].contents.get("relationshipsSpecs").
      asInstanceOf[EmbeddedContainerElement]
    if (relationshipsSpecs != null) {
      val its = relationshipsSpecs.contents.iterator
      while (its.hasNext) {
        val relType = its.next
        val relationshipsSpec = relationshipsSpecs.contents.get(relType).asInstanceOf[EmbeddedElement]
        val it = relationshipsSpec.attributes.iterator
        while (it.hasNext) {
          var objUuid = it.next
          if (objUuid.startsWith("@")) {
            var value = relationshipsSpec.attributes.get(objUuid).substring(1)
            objUuid = objUuid.substring(1)
            SetRelValue(rolon, relType, objUuid, value)
          }
        }
      }
    }

    val reorderSpecs = target.asInstanceOf[TreeMapElement].contents.get("reorderSpecs").asInstanceOf[OrderedElement]
    if (reorderSpecs != null) {
      val its = reorderSpecs.contents.iterator
      while (its.hasNext) {
        val relType = its.next
        val reorderSpec = reorderSpecs.contents.get(relType).asInstanceOf[EmbeddedElement]
        val it = reorderSpec.attributes.iterator
        while (it.hasNext) {
          var moveObjUuid = it.next
          if (moveObjUuid.startsWith("$")) {
            var beforeObjUuid = reorderSpec.attributes.get(moveObjUuid)
            moveObjUuid = moveObjUuid.substring(1)
            val after = beforeObjUuid.startsWith("+")
            beforeObjUuid = beforeObjUuid.substring(1)
            val objectLinks = GetObjectLinks(rolon, relType)
            objectLinks.contents.move(moveObjUuid, beforeObjUuid, after)
          }
        }
      }
    }

    val document = target.asInstanceOf[TreeMapElement].contents.get("document").asInstanceOf[DocumentElement]
    if (document != null) {
      rolon.attributes.put("docJE", je.uuid)
      rolon.attributes.put("docA", target.getJitName)
    }
  }

  override def operationType = classOf[Action].getName
}
