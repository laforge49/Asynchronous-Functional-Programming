/*
 * Copyright 2009 Bill La Forge
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
package kernel
package jits

import org.agilewiki.util.Timestamp
import element.{BlockElement, _BlockElement}

private[kernel] class KernelBlockHandleElement extends KernelHandleElement {
  def queryCache = Kernel(systemContext).queryCache

  def copyCache = TransactionContext().copyCache

  def timestamp = {
    val nm = getJitName
    nm.substring(nm.length - Timestamp.TIMESTAMP_LENGTH)
  }

  override def reference = queryCache.get(getJitName)

  override def hasReference = queryCache.has(getJitName)

  override def clearReference {queryCache.remove(getJitName)}

  override def setReference(_be: _BlockElement) {
    val be = _be.asInstanceOf[BlockElement]
    if (!TransactionContext().query && timestamp == TransactionContext().startingTime) {
      copyCache.put(getJitName, be)
    } else {
      queryCache.put(getJitName, be.asInstanceOf[BlockElement])
    }
  }

  override def resolve = {
    var be = super.resolve.asInstanceOf[BlockElement]
    if (!TransactionContext().query && timestamp < TransactionContext().startingTime) {
      be = be.clone.asInstanceOf[BlockElement]
      be.partness(null, getJitName, null)
      copyCache.put(be.uuid, be)
    }
    be
  }
}
