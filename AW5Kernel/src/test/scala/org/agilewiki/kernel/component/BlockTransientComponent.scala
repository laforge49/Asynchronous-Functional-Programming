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
package component

import java.util.HashSet

import org.agilewiki.kernel.element.BlockTransientElement

trait BlockTransientComponent
        extends _BlockPersistenceComponent {
  this: BlockTransientElement =>

  /**
   * @see PeristenceComponent
   */
  override private[kernel] def definePersistence = new BlockTransient

  /**
   * @see PersistenceComponent
   */
  override private[kernel] def persistence = _persistence.asInstanceOf[BlockTransient]

  class BlockTransient extends _BlockPersistence {
    var _accessed = false
    var _dirty = false
    var _writeLock = false

    override private[kernel] def diskBlockManager = null

    override def write {}

    override def writeLock {
      _writeLock = true
      BlockTransientComponent.writeLocks.add(BlockTransientComponent.this.asInstanceOf[BlockTransientElement])
    }

    override def markDirty = {
      _dirty = true
      BlockTransientComponent.dirtyBlocks.add(BlockTransientComponent.this.asInstanceOf[BlockTransientElement])
      true
    }

    /**
     * Get the current value of accessed
     * @return the value of accessed
     */
    def getAccessed = _accessed

    /**
     * Get the current value of writeLock
     * @return the value of writeLock
     */
    def getWriteLock = _writeLock

    /**
     * Get the current value of dirty
     * @return the value of dirty
     */
    def getDirty = _dirty

  }
}

object BlockTransientComponent {
  var writeLocks = new HashSet[BlockTransientElement]
  var dirtyBlocks = new HashSet[BlockTransientElement]

  def clear {
    val wit = writeLocks.iterator
    while (wit.hasNext) {
      wit.next
      wit.remove
    }
    val dit = dirtyBlocks.iterator
    while (dit.hasNext) {
      dit.next.persistence.write
      dit.remove
    }
  }
}