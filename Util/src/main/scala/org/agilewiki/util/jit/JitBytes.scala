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
package org.agilewiki.util
package jit

object JitBytes {
  val defaultRoleName = JIT_BYTES_ROLE_NAME

  def createJit(context: SystemComposite) = Jits(context).createJit(defaultRoleName).asInstanceOf[JitBytes]
}

class JitBytes extends Jit {
  private var bs: Array[Byte] = null
  private var length = intByteLength

  override def isJitDeserialized = bs != null

  def contentLength = length

  override def loadJit(cursor: JitMutableCursor) {
    super.loadJit(cursor)
    length = cursor.readInt
    cursor.skip(length)
  }

  override def jitByteLength: Int = {
    intByteLength + length
  }

  override protected def serializeJit(cursor: JitMutableCursor) {
    if (bs == null) illegalState
    cursor.writeInt(length)
    cursor.writeBytes(bs)
    bs = null
  }

  def setBytes(bytes: Array[Byte]) {
    writeLock
    val oldLen = jitByteLength
    bs = bytes
    length = bs.length
    if (debugJit) System.err.println("bytes change " + (jitByteLength - oldLen))
    jitUpdated(jitByteLength - oldLen, this)
  }

  def setBytes(bytes: Array[Byte], offset: Int, length: Int) {
    writeLock
    val oldLen = jitByteLength
    bs = new Array[Byte](length)
    System.arraycopy(bytes, offset, bs, 0, length)
    this.length = length
    if (debugJit) System.err.println("bytes change " + (jitByteLength - oldLen))
    jitUpdated(jitByteLength - oldLen, this)
  }

  def getBytes: Array[Byte] = {
    if (bs != null) {
      val bytes = new Array[Byte](length)
      System.arraycopy(bs, 0, bytes, 0, length)
      return bytes
    }
    if (!isJitSerialized) illegalState
    val cursor = jitCursor.mutable
    cursor.skip(intByteLength)
    cursor.readBytes(length)
  }

  def getBytes(bytes: Array[Byte], offset: Int): Int = {
    if (bs != null) {
      System.arraycopy(bs, 0, bytes, offset, length)
      return length
    }
    if (!isJitSerialized) illegalState
    val cursor = jitCursor.mutable
    cursor.skip(intByteLength)
    cursor.readBytes(bytes, offset, length)
    length
  }
}
