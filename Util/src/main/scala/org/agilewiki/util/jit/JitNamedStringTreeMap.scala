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

object JitNamedStringTreeMap {
  val defaultRoleName = JIT_NAMED_STRING_TREE_MAP_ROLE_NAME

  def createJit(context: SystemComposite) =
    Jits(context).createJit(defaultRoleName).asInstanceOf[JitNamedStringTreeMap]
}

class JitNamedStringTreeMap
        extends JitNamedNakedJitTreeMap {

  override def get(name: String) = super.get(name).asInstanceOf[JitString]

  override def remove(name: String) = super.remove(name).asInstanceOf[JitString]

  def getString(name: String) = {
    val rv = get(name)
    if (rv == null) null
    else rv.getString
  }

  def removeString(name: String): String = {
    val rv = remove(name)
    if (rv == null) null
    else rv.getString
  }

  def putString(name: String, value: String) {
    writeLock
    if (value == "" || value == null) {
      remove(name)
      return
    }
    val old = get(name)
    if (old == null) {
      val js = JitString.createJit(systemContext)
      js.setString(value)
      put(name, js)
    }
    else old.setString(value)
  }
}
