/*
 * Copyright 2011 Bill La Forge
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

object Jit {
  val defaultRoleName = JIT_ROLE_NAME
}

class Jit
  extends Cloneable
  with SystemContext {
  protected val debugJit = false
  protected var jitCursor: JitImmutableCursor = _
  private var _jitContainer: Jit = _
  val booleanByteLength = 1
  val intByteLength = 4
  val longByteLength = 8
  var jitRole: JitRole = _

  def displayJit {
    System.err.println(jitRoleName)
  }

  def jitRoleName: String = {
    if (jitRole == null) throw new IllegalStateException("no role for jit of class " + getClass.getName)
    jitRole.roleName
  }

  override def systemContext = jitRole.systemContext

  def stringByteLength(length: Int): Int = intByteLength + 2 * length

  def stringByteLength(string: String): Int = stringByteLength(string.length)

  def loadJit(cursor: JitMutableCursor) {
    jitCursor = cursor.immutable
  }

  def loadJit(bytes: Array[Byte]) {
    val cursor = new JitMutableCursor(bytes, 0)
    loadJit(cursor)
  }

  def jitContainer = _jitContainer

  def partness(container: Jit, name: String, visibleContainer: Jit) {
    if (container == this) throw new IllegalArgumentException
    this._jitContainer = container
  }

  def clearJitContainer {
    this._jitContainer = null
  }

  def getVisibleElement: Jit = {
    if (jitContainer != null) jitContainer.getVisibleElement
    else null
  }

  protected def isJitSerialized = jitCursor != null

  def isJitDeserialized = true

  def writeLock {
    if (jitContainer != null) jitContainer.writeLock
  }

  def jitUpdated(lenDiff: Int, source: Jit) {
    if (jitContainer != null) jitContainer.jitUpdater(lenDiff, source)
    jitCursor = null
  }

  def jitUpdater(lenDiff: Int, source: Jit) {
    if (debugJit) {
      System.err.println()
      System.err.println("jit updater " + lenDiff + " " + source.getClass.getName)
      System.err.println("this " + this)
      System.err.println("this bytelen " + jitByteLength)
    }
    jitUpdated(lenDiff, source)
  }

  def jitToBytes(cursor: JitMutableCursor) {
    if (isJitSerialized) {
      val ic = cursor.immutable
      cursor.write(jitCursor, jitByteLength)
      jitCursor = ic
    } else {
      jitCursor = cursor.immutable
      serializeJit(cursor)
    }
    if (jitCursor.offset + jitByteLength != cursor.offset) {
      System.err.println(getClass.getName)
      System.err.println("" + jitCursor.offset + " + " + jitByteLength + " != " + cursor.offset)
      throw new IllegalStateException
    }
  }

  def jitToBytes: Array[Byte] = {
    val bytes = new Array[Byte](jitByteLength)
    val jmc = new JitMutableCursor(bytes, 0)
    jitToBytes(jmc)
    bytes
  }

  def illegalState = throw new IllegalStateException("neither serialized nor deserialized")

  protected def serializeJit(cursor: JitMutableCursor) {}

  def jitByteLength = 0

  def validateByteLength {}

  final override def clone = {
    val c = this.getClass.newInstance.asInstanceOf[Jit]
    c.jitRole = jitRole
    val data = jitToBytes
    c.loadJit(data)
    c
  }
}
