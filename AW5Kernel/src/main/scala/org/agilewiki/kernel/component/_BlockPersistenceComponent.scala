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
package component

import org.agilewiki.kernel.element._BlockElement
import util.jit.JitMutableCursor
import jits.KernelHandleElement
import util.Timestamp

private[kernel] trait _BlockPersistenceComponent
        extends PersistenceComponent {
  this: _BlockElement =>

  override private[kernel] def persistence: _BlockPersistence = {
    _persistence.asInstanceOf[_BlockPersistence]
  }

  private[kernel] abstract class _BlockPersistence extends Persistence {
    private[kernel] var kernelHandleElement: KernelHandleElement = null
    private[kernel] var dirty = false

    private[kernel] def diskBlockManager: BlockManagement

    /**
     * Locks the element for writing, when applicable, but in this case is a noop.
     */
    override def writeLock {
    }

    override def markDirty = {
      if (!dirty) {
        dirty = diskBlockManager.addDirty(_BlockPersistenceComponent.this)
        if (dirty) {
          kernelHandleElement.getBlockElement //----------------debug
          kernelHandleElement.jitUpdater(0,_BlockPersistenceComponent.this)
        }
      }
      dirty
    }

    def write {
      dirty = false
      var size = kernelHandleElement.getBlockSize
      var blockOffset = kernelHandleElement.getBlockOffset
      if (size > 0) {
        kernelHandleElement.setBlockSize(0)
        kernelHandleElement.setBlockOffset(0L)
        diskBlockManager.release(blockOffset, size)
      }
      if (!deleted) {
        val cn = _BlockPersistenceComponent.this.jitRoleName
        val length = kernelRootElement.checksumLength + stringByteLength(cn) + jitByteLength
        val bytes = new Array[Byte](length)
        val cursor = JitMutableCursor(bytes, kernelRootElement.checksumLength)
        cursor.writeString(cn)
        jitToBytes(cursor)
        blockOffset = diskBlockManager.allocate(length)
        kernelHandleElement.setBlockSize(length)
        kernelHandleElement.setBlockOffset(blockOffset)
        kernelRootElement.position(blockOffset)
        kernelRootElement.writeBlock(cursor)
      } else kernelHandleElement.clearReference
    }
  }
}
