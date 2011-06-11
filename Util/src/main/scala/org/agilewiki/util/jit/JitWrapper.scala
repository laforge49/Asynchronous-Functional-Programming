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

object JitWrapper {
  val defaultRoleName = JIT_WRAPPER_ROLE_NAME

  def createJit(context: SystemComposite) = Jits(context).createJit(defaultRoleName).asInstanceOf[JitWrapper]
}

class JitWrapper extends NamedJit {
  protected var length = 0
  protected var dser = false
  protected var j: Jit = null

  override def partness(container: Jit, name: String, visibleContainer: Jit) {
    super.partness(container, name, visibleContainer)
    if (j != null) j.partness(this, name, this)
  }

  override def isJitDeserialized = dser

  override def jitByteLength: Int = {
    validateByteLength
    val rv = intByteLength + length
    rv
  }

  override def loadJit(cursor: JitMutableCursor) {
    super.loadJit(cursor)
    length = cursor.readInt
    if (length < 1) throw new IllegalStateException("invalid wrapper length")
    cursor.skip(length)
    dser = false
  }

  override def validateByteLength {
    if (!debugJit) return
    val wrapped = getJit
    wrapped.validateByteLength
    val jitRoleName = wrapped.jitRoleName
    if (length != stringByteLength(jitRoleName) + wrapped.jitByteLength) {
      System.err.println(length)
      System.err.println(stringByteLength(jitRoleName))
      System.err.println(wrapped.jitByteLength)
      System.err.println(jitRoleName)
      throw new IllegalStateException("wrapper length is wrong")
    }
  }

  override def jitUpdater(lenDiff: Int, source: Jit) {
    if (debugJit) {
      System.err.println()
      System.err.println("wrapper updater " + lenDiff + " " + source.jitRoleName)
      System.err.println("this " + this)
    }
    length += lenDiff
    if (debugJit) {
      System.err.println("this bytelen " + jitByteLength)
      validateByteLength
    }
    jitUpdated(lenDiff, source)
  }

  override protected def serializeJit(cursor: JitMutableCursor) {
    if (!dser) illegalState
    validateByteLength
    cursor.writeInt(length)
    val roleName = j.jitRoleName
    cursor.writeString(roleName)
    j.jitToBytes(cursor)
  }

  def getJit: Jit = {
    if (dser) return j
    if (!isJitSerialized) illegalState
    if (debugJit) {
      System.err.println()
      System.err.println()
      System.err.println("deserialized")
      System.err.println()
      System.err.println()
    }
    val cursor = jitCursor.mutable
    cursor.skip(intByteLength)
    val roleName = cursor.readString
    j = Jits(systemContext).createJit(roleName)
    j.partness(this, getJitName, this)
    j.loadJit(cursor)
    dser = true
    j
  }

  def setJit(jit: Jit) {
    writeLock
    if (debugJit) {
      System.err.println()
      System.err.println("wrapper set " + jit.jitRoleName)
      System.err.println("this " + this)
    }
    var oldLen = length
    j = jit
    length = stringByteLength(j.jitRoleName) + j.jitByteLength
    if (debugJit) {
      System.err.println("wrapped role name length" + stringByteLength(j.jitRoleName))
      System.err.println("wrapped jit length" + j.jitByteLength)
      System.err.println("oldLen " + oldLen)
      System.err.println("new len " + length)
    }
    if (jitContainer != null) j.partness(this, getJitName, this)
    dser = true
    validateByteLength
    jitUpdated(length - oldLen, this)
  }
}
