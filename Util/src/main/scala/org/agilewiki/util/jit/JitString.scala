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

object JitString {
  val defaultRoleName = JIT_STRING_ROLE_NAME

  def createJit(context: SystemComposite) = Jits(context).createJit(defaultRoleName).asInstanceOf[JitString]
}

class JitString extends Jit {
  private var dser = false
  private var s = ""
  private var length = 0

  override def isJitDeserialized = dser

  override def loadJit(cursor: JitMutableCursor) {
    super.loadJit(cursor)
    length = cursor.readInt
    cursor.skip(length * 2)
    dser = false
  }

  override def jitByteLength: Int = {
    if (dser) stringByteLength(s)
    else stringByteLength(length)
  }

  override protected def serializeJit(cursor: JitMutableCursor) {
    if (!dser) illegalState
    length = s.length
    cursor.writeString(s)
  }

  def setString(string: String) {
    if ((isJitSerialized || dser) && getString == string) return
    writeLock
    val oldLen = jitByteLength
    s = string
    dser = true
    if (debugJit) System.err.println("string change "+(jitByteLength - oldLen))
    jitUpdated(jitByteLength - oldLen, this)
  }

  def getString: String = {
    if (dser) return s
    if (!isJitSerialized) illegalState
    val cursor = jitCursor.mutable
    cursor.skip(intByteLength)
    s = cursor.readString(length)
    dser = true
    s
  }
}
