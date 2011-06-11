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

object JitBoolean {
  val defaultRoleName = JIT_BOOLEAN_ROLE_NAME

  def createJit(context: SystemComposite) = Jits(context).createJit(defaultRoleName).asInstanceOf[JitBoolean]
}

class JitBoolean extends Jit {
  private var dser = true
  private var i = 0

  override def isJitDeserialized = dser

  override def loadJit(cursor: JitMutableCursor) {
    super.loadJit(cursor)
    cursor.skip(booleanByteLength)
    dser = false
  }

  override def jitByteLength: Int = {
    booleanByteLength
  }

  override protected def serializeJit(cursor: JitMutableCursor) {
    if (!dser) illegalState
    cursor.writeByte(i)
  }

  def setBoolean(b: Boolean) {
    if ((isJitSerialized || dser) && apply() == b) return
    writeLock
    if (b) i = 1 else i = 0
    dser = true
    jitUpdated(0, this)
  }

  def apply() = getBoolean

  def getBoolean: Boolean = {
    if (dser) return (i != 0)
    if (!isJitSerialized) illegalState
    i = jitCursor.mutable.readByte
    dser = true
    (i != 0)
  }
}
