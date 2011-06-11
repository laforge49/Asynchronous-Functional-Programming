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

object JitNamedVariableJitTreeMap {
  val defaultRoleName = JIT_NAMED_VARIABLE_JIT_TREE_MAP_ROLE_NAME

  def createJit(context: SystemComposite) = Jits(context).createJit(defaultRoleName).asInstanceOf[JitNamedVariableJitTreeMap]
}

class JitNamedVariableJitTreeMap extends JitNamedJitTreeMap {

  override protected def wrapper(cursor: JitMutableCursor, name:String) = {
    val jw = JitWrapper.createJit(systemContext)
    jw.partness(this, name, this)
    jw.loadJit(cursor)
    jw
  }

  override protected def wrapper(jit: Jit, name:String) = {
    val jw = JitWrapper.createJit(systemContext)
    jw.setJit(jit)
    jw
  }

  override def getWrapper(name: String) = {
    val w = treeMap.get(name).asInstanceOf[JitWrapper]
    w
  }

  override def get(name: String): Jit = {
    val w = getWrapper(name)
    if (w == null) return null
    val j = w.getJit
    j
  }

  override def removeWrapper(name: String): JitWrapper = {
    writeLock
    val container = treeMap.remove(name).asInstanceOf[JitWrapper]
    if (container == null) return null
    if (debugJit) System.err.println("remove")
    jitUpdater(-stringByteLength(name) - container.jitByteLength, this)
    container.clearJitContainer
    container
  }

  override def remove(name: String): Jit = {
    val container = removeWrapper(name)
    if (container == null) return null
    container.getJit
  }

  override def putWrapper(name: String, jit: Jit) = {
    if (!jit.isInstanceOf[JitWrapper]) throw new IllegalArgumentException("jit is not a wrapper: " + jit)
    super.putWrapper(name, jit)
  }
}
