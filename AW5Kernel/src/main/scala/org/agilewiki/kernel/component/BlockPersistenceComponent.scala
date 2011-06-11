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

import org.agilewiki.util.Timestamp
import org.agilewiki.kernel.element.BlockElement
import jits.KernelBlockHandleElement

private[kernel] trait BlockPersistenceComponent
  extends _BlockPersistenceComponent {
  this: BlockElement =>

  override private[kernel] def definePersistence = {
    new BlockPersistence
  }

  override private[kernel] def persistence: BlockPersistence =
    _persistence.asInstanceOf[BlockPersistence]

  private[kernel] class BlockPersistence extends _BlockPersistence {
    override private[kernel] def diskBlockManager = kernelRootElement.diskBlockManager3

    /**
     * Locks the element for writing, when applicable
     */
    override def writeLock {
      if (getJitName == null) return
      if (transactionContexts.query) throw new IllegalStateException("queries may not write")
      if (transactionContexts.writeOnly != null
        && !getBlockElement.rolonUuid.startsWith(transactionContexts.writeOnly)) {
        if (transactionContexts.writeOnly.length == 0) {
          throw new IllegalStateException("Write lock can not be set in read-only mode")
        } else {
          System.err.println("writeOnly = " + transactionContexts.writeOnly)
          System.err.println("rolonUuid = " + getBlockElement.rolonUuid)
          throw new IllegalStateException("Write lock can only be set for the journal entry rolon")
        }
      }
      if (!transactionContexts.isCurrentTime) {
        throw new IllegalStateException("Write lock can not be set in past time")
      }
    }

    override def markDirty: Boolean = {
      if (getJitName == null) return false
      if (!dirty && super.markDirty) {
        val invertedStartingTime =
          Timestamp.invert(Kernel(systemContext).startingTime)
        if (timestamp != invertedStartingTime) {
          if (kernelHandleElement != null) {
            kernelHandleElement.clearReference /////////transitional
          }
          val name = uuid + 1.asInstanceOf[Char] + invertedStartingTime
          val handle = kernelRootElement.addressMap.contents.add(name, KERNEL_BLOCK_HANDLE_ELEMENT_ROLE_NAME)
            .asInstanceOf[KernelBlockHandleElement]
          handle._set(BlockPersistenceComponent.this)
        }
      }
      dirty
    }
  }
}
