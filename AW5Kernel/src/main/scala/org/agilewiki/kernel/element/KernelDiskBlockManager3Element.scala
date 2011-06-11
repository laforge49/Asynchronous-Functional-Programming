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
package kernel
package element

import org.agilewiki.util.Timestamp
import util.jit.structure.JitElement
import util.jit.{JitNamedJitTreeMap, Jits}
import component.KernelBlockManagementElementBTreeContainer2Component

private[kernel] class KernelDiskBlockManager3Element
  extends KernelDiskBlockManagerElement
  with KernelBlockManagementElementBTreeContainer2Component {
  override protected def _getMore(suggestedSize: Int): (Long, Long) = {
    kernelRootElement.diskBlockManager2.getMore(suggestedSize)
  }

  override def getMore(suggestedSize: Int): (Long, Long) = {
    throw new UnsupportedOperationException()
  }

  override def addDirty(blockElement: _BlockElement) = {
    super.addDirty(blockElement)
    var rv = true
    if (blockElement.isInstanceOf[BlockElement]) {
      val be = blockElement.asInstanceOf[BlockElement]
      var uuid = be.uuid
      if (be.isInstanceOf[INodeElement]) {
        val ie = be.asInstanceOf[INodeElement]
        uuid = ie.rolonUuid
      }
      if (uuid == null) rv = false
      else {
        Kernel(systemContext).transactionContexts.effected(uuid)
      }
    }
    if (_dirty.size > 200) {
      while (_dirty.size > 100) {
        flushDirty
      }
    }
    rv
  }
}
