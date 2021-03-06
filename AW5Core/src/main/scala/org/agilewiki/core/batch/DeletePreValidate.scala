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

import kernel.element.Element
import kernel.TransactionContext

class DeletePreValidate
  extends PreValidate {

  override def process(roleName: String,
                       target: Element) {
    super.process(roleName, target)
    val uuid = target.getJitName
    val rolon = TransactionContext().rolonRootElement(uuid)
    if (rolon == null) {
      System.err.println("Missing Rolon, UUID = " + uuid)
      throw MissingUuidException(uuid)
    }
    val queryTimestamp = je.attributes.get("queryTimestamp")
    val updateTimestamp = rolon.attributes.get("updateTimestamp")
    if (updateTimestamp != null && queryTimestamp < updateTimestamp) {
      throw SyncException(uuid)
    }
    val oldCount = rolon.attributes.getInt("childCount", 0)
    val inc = target.attributes.getInt("childInc", 0)
    val childCount = oldCount + inc
    if (childCount < 0) throw NegativeChildCountException(uuid)
    if (childCount != 0) throw DeleteWithChildrenException(uuid)
  }
}
