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

import java.util.zip.Adler32
import java.io.File
import java.io.RandomAccessFile

import util._
import jit.structure.JitElement
import jit.JitMutableCursor
import org.agilewiki.kernel.component.ContainerComponent
import org.agilewiki.kernel.component.TreeMapContainerComponent
import org.agilewiki.kernel.component.KernelRootPersistenceComponent
import org.agilewiki.kernel.journal.logging.Log
import util.{Configuration, Timestamp}
import jits.{KernelINodeHandleElement, KernelHandleElement}

private[kernel] class KernelRootElement
        extends _BlockElement
                with TreeMapContainerComponent
                with KernelRootPersistenceComponent {

  def inactiveFilter(invertedTimestamp: String) = {
    val startingTime = Timestamp.invert(invertedTimestamp)
    startingTime != Kernel(systemContext).startingTime
  }

  def extent = {
    attributes.getLong("dbExtent", 0)
  }

  def setExtent(newExtent: Long) {
    attributes.putLong("dbExtent", newExtent, 0)
  }

  protected var _diskBlockManager1: KernelDiskBlockManager1Element = null

  def diskBlockManager1 = {_diskBlockManager1}

  def diskBlockManager1Size = {_diskBlockManager1.contents.size}

  protected var _diskBlockManager2: KernelDiskBlockManager2Element = null

  def diskBlockManager2 = {_diskBlockManager2}

  def diskBlockManager2Size = {_diskBlockManager2.contents.size}

  protected var _diskBlockManager3: KernelDiskBlockManager3Element = null

  def diskBlockManager3 = {_diskBlockManager3}

  def diskBlockManager3Size = {_diskBlockManager3.contents.size}

  def validate {
    val map = new DiskMap
    validate(map)
    if (map.size > 1) {
      println()
      println(databasePathname + " does not have contiguous space:")
      map.print
      throw new IllegalStateException(databasePathname + " does not have contiguous space")
    }
  }

  def validate(map: DiskMap) {
    //    println("validate "+kernelRootElement)
    map._add(0, maxRootBlockSize)
    map._add(maxRootBlockSize, maxRootBlockSize)
    diskBlockManager1.validate(map)
    diskBlockManager2.validate(map)
    diskBlockManager3.validate(map)
    validateWalk(map, this, 0)
  }

  private def validateWalk(map: DiskMap, jitElement: JitElement, depth: Int) {
    if (jitElement == null) {
      throw new Exception("element is null")
    }
//        System.err.println("----"+depth+" validating "+element.name)
//        System.err.println("    "+element.getClass.getName)
    if (jitElement.isInstanceOf[KernelHandleElement]) {
      val handle = jitElement.asInstanceOf[KernelHandleElement]
      if (handle.getBlockSize > 0) {
//                System.err.println("    handle "+handle.getBlockSize)
        map.add(handle.getBlockOffset, handle.getBlockSize)
      } else {
//                System.err.println("    no block handle is empty: "+handle.empty)
      }
      if (handle.isInstanceOf[KernelINodeHandleElement] || !handle.empty) {
        validateWalk(map, handle.resolve, depth + 1)
      }
    } else if (jitElement.isInstanceOf[ContainerComponent]) {
      val contents = jitElement.asInstanceOf[ContainerComponent].contents
//      System.err.println("  contents "+contents)
//      System.err.println("   len"+element.jitByteLength)
//      System.err.println("  size = "+contents.size)
      val it = contents._iteratorActual
      while (it.hasNext) {
        val key = it.next
//        System.err.println(">>>"+key + " "+ depth)
        validateWalk(map, contents._getActual(key), depth + 1)
      }
    }
  }

  protected var _addressMap: KernelAddressMapElement = null

  def addressMapSize = {_addressMap.contents.size}

  def addressMap = {
    if (_addressMap == null) deserialize
    _addressMap
  }

  protected var _timestamps: KernelTimestampsElement = null

  def timestampsSize = {_timestamps.contents.size}

  def timestamps = {_timestamps}

  protected var _databasePathname: String = null

  def databasePathname = {_databasePathname}

  protected var databaseFile: File = null

  def databaseFileLength = databaseFile.length

  protected var randomAccessFile: RandomAccessFile = null

  def commits = attributes.getInt("commits", 0)

  protected def incCommits = attributes.putInt("commits", commits + 1, 0)

  def rewinds = attributes.getInt("rewinds", 0)

  protected def incRewinds = attributes.putInt("rewinds", rewinds + 1, 0)

  protected var _maxRootBlockSize = 0

  def maxRootBlockSize = _maxRootBlockSize

  def startup = {
    _maxRootBlockSize = Configuration(systemContext).requiredIntProperty(MAX_ROOT_BLOCK_SIZE_PARAMETER)
    Log initialize systemContext
    _databasePathname = Kernel(systemContext).databasePathname
    if (_databasePathname == null || _databasePathname.length == 0) {
      throw new IllegalStateException("Missing database pathname property")
    }
    databaseFile = new File(_databasePathname)
    var needRestart = false
    if (databaseFile.exists) {
      if (!databaseFile.isFile) {
        throw new IllegalStateException("Not an ordinary file: " + _databasePathname)
      }
      needRestart = databaseFileLength != 0
    }
    randomAccessFile = new RandomAccessFile(databaseFile, databaseAccessMode)
    if (!needRestart) {
      initialize
      commit
    }
    needRestart
  }

  def initialize {
    //println("initialize kre")
    setExtent(2 * maxRootBlockSize)
    partness(null, "RootElement", null)
    _diskBlockManager1 = contents.add("DiskBlockManager1", kernelDiskBlockManager1ElementType)
            .asInstanceOf[KernelDiskBlockManager1Element]
    _diskBlockManager2 = contents.add("DiskBlockManager2", kernelDiskBlockManager2ElementType)
            .asInstanceOf[KernelDiskBlockManager2Element]
    _diskBlockManager3 = contents.add("DiskBlockManager3", kernelDiskBlockManager3ElementType)
            .asInstanceOf[KernelDiskBlockManager3Element]
    _addressMap = contents.add("AddressMap", kernelAddressMapElementType)
            .asInstanceOf[KernelAddressMapElement]
    _timestamps = contents.add("Timestamps", kernelTimestampsElementType)
            .asInstanceOf[KernelTimestampsElement]
    if (_addressMap == null) throw new IllegalStateException
  }

  def recycle {
    diskBlockManager3.recycle
    diskBlockManager2.recycle
    diskBlockManager1.recycle
  }

  def commit {
    incCommits
    diskBlockManager3.flushAllDirty
    diskBlockManager2.flushAllDirty
    diskBlockManager1.flushAllDirty
    persistence.saveRootBlock
  }

  override def load {
    //println("load kre")
    super.load
    _diskBlockManager1 = contents.get("DiskBlockManager1")
            .asInstanceOf[KernelDiskBlockManager1Element]
    _diskBlockManager2 = contents.get("DiskBlockManager2")
            .asInstanceOf[KernelDiskBlockManager2Element]
    _diskBlockManager3 = contents.get("DiskBlockManager3")
            .asInstanceOf[KernelDiskBlockManager3Element]
    _addressMap = contents.get("AddressMap")
            .asInstanceOf[KernelAddressMapElement]
    _timestamps = contents.get("Timestamps")
            .asInstanceOf[KernelTimestampsElement]
    if (_addressMap == null) throw new IllegalStateException
  }

  def close {
    try {
      randomAccessFile.close
    } catch {
      case unknown => {}
    }
  }

  protected def kernelDiskBlockManager1ElementType = KERNEL_DISK_BLOCK_MANAGER1_ELEMENT_ROLE_NAME

  protected def kernelDiskBlockManager2ElementType = KERNEL_DISK_BLOCK_MANAGER2_ELEMENT_ROLE_NAME

  protected def kernelDiskBlockManager3ElementType = KERNEL_DISK_BLOCK_MANAGER3_ELEMENT_ROLE_NAME

  protected def kernelAddressMapElementType = KERNEL_ADDRESS_MAP_ELEMENT_ROLE_NAME

  protected def kernelTimestampsElementType = KERNEL_TIMESTAMP_ELEMENT_ROLE_NAME

  def databaseAccessMode = {
    Configuration(systemContext).requiredProperty(DATABASE_ACCESS_RELATIONSHIP_MODE)
  }

  def position(blockOffset: Long) {
    randomAccessFile.seek(blockOffset)
  }

  def readBlock(length: Int): JitMutableCursor = {
    val bytes = new Array[Byte](length)
    val cursor = JitMutableCursor(bytes, 0)
    randomAccessFile.readFully(bytes)
    val cs = cursor.readLong
    val adler32 = new Adler32
    adler32.update(bytes, cursor.offset, length - checksumLength)
    val csn = adler32.getValue
    if (csn != cs) {
      throw new IllegalStateException("Checksums do not match")
    }
    cursor
  }

  def rootBlockLength0 = persistence.rootBlockLength0

  def rootBlockLength1 = persistence.rootBlockLength1

  val checksumLength = longByteLength

  def writeBlock(cursor: JitMutableCursor) {
    val bytes = cursor.bytes
    val length = bytes.length
    val adler32 = new Adler32
    adler32.update(bytes, checksumLength, length - checksumLength)
    val cs = adler32.getValue
    cursor.offset = 0
    cursor.writeLong(cs)
    randomAccessFile.write(bytes)
  }
}
