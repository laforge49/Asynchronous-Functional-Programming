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

import java.io.DataInput
import org.agilewiki.kernel.element.KernelRootElement
import util.jit.JitMutableCursor
import util.Timestamp

/**
 * Implements persistence for the @see KernelRootElement.  It is allocated 2
 * physical disk blocks located at the beginning of the database file.
 *
 * @author Bill La Forge
 */
private[kernel] trait KernelRootPersistenceComponent
        extends _BlockPersistenceComponent {
  this: KernelRootElement =>

  /**
   * @see PersistenceComponent
   */
  override private[kernel] def definePersistence = {new KernelRootPersistence}

  /**
   * @see PersistenceComponent
   */
  override private[kernel] def persistence: KernelRootPersistence = {
    _persistence.asInstanceOf[KernelRootPersistence]
  }

  /**
   * Concrete implementation of the @see PersistenceComponent inner class
   * definition.  This contains the persistence logic.
   */
  private[kernel] class KernelRootPersistence private[kernel] extends _BlockPersistence {
    private[kernel] var writeBlock0 = true

    private var _rootBlockLength0 = 0

    /**
     * Get the length of the first root physical disk block
     *
     * @return the length of the first root physical disk block
     */
    def rootBlockLength0 = _rootBlockLength0

    private var _rootBlockLength1 = 0

    /**
     * Get the length of the second root physical disk block
     *
     * @return the length of the second root physical disk block
     */
    def rootBlockLength1 = _rootBlockLength1

    /**
     * Get the manager of root physical disk blocks
     *
     * @return the manager of root physical disk blocks
     */
    override private[kernel] def diskBlockManager = {throw new UnsupportedOperationException()}

    /**
     * @see PersistenceComponent
     */
    override def markDirty = {
      dirty = true
      true
    }

    /**
     * Serializes the @see KernelRootElement and all embedded elements to disk.
     *
     * @throws IllegalStateException
     */
    def saveRootBlock {
      val startingTime = Timestamp.timestamp
      val length = checksumLength + stringByteLength(startingTime) + jitByteLength
      val bytes = new Array[Byte](length)
      val cursor = JitMutableCursor(bytes, checksumLength)
      cursor.writeString(startingTime)
      jitToBytes(cursor)
      if (length > maxRootBlockSize) {
        throw new IllegalStateException("Root block is too big: " + length)
      }
      var position: Long = if (writeBlock0) {
        _rootBlockLength0 = length
        0
      } else {
        _rootBlockLength1 = length
        maxRootBlockSize
      }
      randomAccessFile.seek(position)
      randomAccessFile.writeInt(length)
      writeBlock(cursor)
      writeBlock0 = !writeBlock0
    }

    /**
     * Reads the length of one of the kernel's root physical blocks at the
     * given position in the database file.
     *
     * @param position the position in database file to read from.
     * @return the length of one of the kernel's root physical blocks.
     */
    protected def readRootBlockLength(position: Long) = {
      randomAccessFile.seek(position)
      val rv = randomAccessFile.readInt
      rv
    }

    private[kernel] def readRootBlock0: (JitMutableCursor, String) = {
      var cursor: JitMutableCursor = null
      var timestamp0 = ""
      try {
        _rootBlockLength0 = readRootBlockLength(0)
        cursor = readBlock(_rootBlockLength0)
        timestamp0 = cursor.readString
      } catch {
        case unknown => {println("bad block 0 " + unknown)}
      }
      (cursor, timestamp0)
    }

    private[kernel] def readRootBlock1: (JitMutableCursor, String) = {
      var cursor: JitMutableCursor = null
      var timestamp1 = ""
      if (databaseFileLength > maxRootBlockSize) {
        try {
          _rootBlockLength1 = readRootBlockLength(maxRootBlockSize)
          cursor = readBlock(_rootBlockLength1)
          timestamp1 = cursor.readString
        } catch {
          case unknown => {println("bad block 1 " + unknown)}
        }
      }
      (cursor, timestamp1)
    }
  }
}
