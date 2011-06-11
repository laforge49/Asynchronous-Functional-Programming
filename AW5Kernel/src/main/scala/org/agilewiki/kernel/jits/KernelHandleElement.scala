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
package jits

import util.jit.structure.JitElement
import element._BlockElement
import java.io.IOException
import util.jit.{Jits, JitMutableCursor, Jit}

abstract class KernelHandleElement
        extends JitElement
                with Reference {
  private var dser = true
  private var _size = 0
  private var _offset = 0L

  override def jitByteLength: Int = intByteLength + longByteLength

  override def isJitDeserialized = dser

  override def loadJit(cursor: JitMutableCursor) {
    super.loadJit(cursor)
    cursor.skip(jitByteLength)
    dser = false
  }

  override protected def serializeJit(cursor: JitMutableCursor) {
    if (!dser) illegalState
    cursor.writeInt(_size)
    cursor.writeLong(_offset)
  }

  private def deserialize {
    if (!dser) {
      if (!isJitSerialized) illegalState
      val mutable = jitCursor.mutable
      _size = mutable.readInt
      _offset = mutable.readLong
      dser = true
    }
  }

  def getBlockSize: Int = {
    deserialize
    _size
  }

  def getBlockOffset: Long = {
    deserialize
    _offset
  }

  def setBlockSize(size: Int) {
    if ((isJitSerialized || dser) && getBlockSize == size) return
    writeLock
    _size = size
    dser = true
    jitUpdated(0, this)
  }

  def setBlockOffset(offset: Long) {
    if ((isJitSerialized || dser) && getBlockOffset == offset) return
    writeLock
    _offset = offset
    dser = true
    jitUpdated(0, this)
  }

  def reference: _BlockElement

  def hasReference: Boolean

  def clearReference

  def setReference(be: _BlockElement)

  def empty = !hasReference && getBlockSize == 0

  override def resolve: _BlockElement = {
    var rv: _BlockElement = null
    val ref = reference
    if (ref != null) {
      rv = ref
    } else if (getBlockSize > 0) {
      rv = read
    }
    rv
  }

  def read = {
    val kernelRootElement = Kernel(systemContext).kernelRootElement
    kernelRootElement.position(getBlockOffset)
    val cursor = kernelRootElement.readBlock(getBlockSize)
    var cn = ""
    try {
      cn = cursor.readString
    } catch {
      case ex: IOException => throw new IllegalStateException("Unable to read class name", ex)
    }
    val e = Jits(systemContext).createJit(cn).asInstanceOf[_BlockElement]
    e.partness(null, getJitName, null)
    e.loadJit(cursor)
    setReference(e)
    e.persistence.kernelHandleElement = this
    e
  }

  def set(elementType: String) = {
    val blockElement = Jits(systemContext).createJit(elementType).asInstanceOf[_BlockElement]
    blockElement.partness(null, getJitName, null)
    writeLock
    blockElement.persistence.kernelHandleElement = this
    setReference(blockElement)
    jitUpdated(0, this) //lock in memory to preserve the weak reference
    blockElement
  }

  def _set(blockElement: _BlockElement) {
    if (!empty) {
      throw new IllegalStateException("The handle is not empty")
    }
    blockElement.partness(null, getJitName, null)
    blockElement.persistence.kernelHandleElement = this
    setReference(blockElement)
  }

  override def deleting {
    if (!empty) {
      val blockElement = resolve
      val persistence = blockElement.persistence
      writeLock
      val o = getBlockOffset
      val s = getBlockSize
      blockElement._delete
      clearReference
      jitUpdated(0, this)
    }
  }
}