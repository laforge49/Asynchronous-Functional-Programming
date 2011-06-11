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

object JitRole {
  def apply(systemContext: SystemComposite, roleNm: String, i: Int): JitRole = {
    val jitClassName = Configuration(systemContext).property("jitRole" + i + ".jitClass")
    if (jitClassName == null) return null
    val _role = new JitRole(systemContext, roleNm)
    _role.setJitClassName(jitClassName)
    val jitSubRoleName = Configuration(systemContext).property("jitRole" + i + ".jitSubRole")
    if (jitSubRoleName != null) _role.setJitSubRoleName(jitSubRoleName)
    _role
  }
}

class JitRole(_systemContext: SystemComposite, _roleName: String) {
  private var className: String = _
  private var jitClass: Class[Jit] = _
  private var jitSubRoleName: String = _

  def systemContext = _systemContext

  def roleName = _roleName

  def setJitClassName(jitClassName: String) {
    className = jitClassName
  }

  def jitClassName = className

  protected def getJitClass = {
    if (jitClass == null) {
      val cl = this.getClass().getClassLoader()
      try {
        jitClass = cl.loadClass(jitClassName).asInstanceOf[Class[Jit]]
      } catch {
        case ex: ClassNotFoundException =>
          throw new IllegalStateException(
            "Unable to load class " + jitClassName, ex)
      }
    }
    jitClass
  }

  def createJit = {
    var jit: Jit = null
    try {
      jit = getJitClass.newInstance()
    } catch {
      case ex: Exception => {
        throw new IllegalStateException(
          "Unable to instantiate jit from role " + _roleName + " class " + getJitClass.getName, ex)
      }
    }
    jit.jitRole = this
    jit
  }

  def setJitSubRoleName(subRoleName: String) {
    jitSubRoleName = subRoleName
  }

  def subRoleName = jitSubRoleName

  def subRole = Jits(systemContext).jitRole(subRoleName)

  def createSubJit = subRole.createJit
}