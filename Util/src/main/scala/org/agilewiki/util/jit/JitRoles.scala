/*
 * Copyright 2010 Alex K.
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
package util
package jit

import java.util.HashMap
import util.{Configuration, SystemComposite}

class JitRoles(systemContext: SystemComposite) {
  protected val roleMap = new HashMap[String, JitRole]
  private var i = 0

  while (i > -1) {
    i += 1
    val roleNm = Configuration(systemContext).property("jitRole" + i + ".name")
    if (roleNm == null) i = -1
    else if (!roleMap.containsKey(roleNm)) {
      val role = createRole(roleNm, i)
      roleMap.put(roleNm, role)
    }
  }

  def role(roleName: String) = {
    val role = roleMap.get(roleName)
    if (role == null) throw new IllegalArgumentException("Unknown jit role name: " + roleName)
    role
  }

  protected def createRole(roleNm: String, i: Int) = {
    var role = JitRole(systemContext, roleNm, i)
    if (role == null) throw new IllegalStateException("invalid jit role: " + roleNm)
    role
  }
}
