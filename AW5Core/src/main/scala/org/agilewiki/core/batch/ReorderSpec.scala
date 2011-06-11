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
import element.{RolonRootElement, OrderedElement, EmbeddedElement}

object ReorderSpec {
  def before(je: RolonRootElement,
            uuid: String,
            relType: String,
            moveObjUuid: String,
            beforeObjUuid: String) {
    apply(je,uuid, relType, moveObjUuid, beforeObjUuid, false)
  }

  def after(je: RolonRootElement,
            uuid: String,
            relType: String,
            moveObjUuid: String,
            afterObjUuid: String) {
    apply(je,uuid, relType, moveObjUuid, afterObjUuid, true)
  }

  private def apply(je: RolonRootElement,
            uuid: String,
            relType: String,
            moveObjUuid: String,
            beforeObjUuid: String,
            after: Boolean) {
    val action = BatchJE.update(je, uuid)
    if (action.jitRoleName == DELETE_ACTION_ROLE_NAME)
      throw new IllegalArgumentException("delete may not change order")
    val reorderSpecs = action.contents.make("reorderSpecs", ORDERED_ELEMENT_ROLE_NAME).
      asInstanceOf[OrderedElement]
    val reorderSpec = reorderSpecs.contents.make(relType, EMBEDDED_ELEMENT_ROLE_NAME).
      asInstanceOf[EmbeddedElement]
    if (after) reorderSpec.attributes.put("$" + moveObjUuid, "+" + beforeObjUuid)
    else reorderSpec.attributes.put("$" + moveObjUuid, "-" + beforeObjUuid)
  }
}
