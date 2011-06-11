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

import java.util.{Collections, Enumeration, Properties}

class JitConfig(properties: Properties) {
  var roleIndx = 0
  var lastRoleIndx = 0
  protected var roles = Map[String, Int]()
  protected var roleName: String = _

  require(properties != null, "Properties can't be null")

  setLastRoleIdx

  /**
   * Starts a new role configuration
   * @param roleName The name of the new role
   */
  def role(roleName: String): Unit = {
    this.roleName = roleName
    // to point index on existing role
    if (roles contains roleName)
      roleIndx = roles(roleName)
    else {
      // to add new role and point index
      lastRoleIndx += 1
      roleIndx = lastRoleIndx
      roles += (roleName -> roleIndx)
      properties.put("jitRole" + roleIndx + ".name", roleName)
    }
  }

  private def setLastRoleIdx: Unit = {
    val enums = properties.keys.asInstanceOf[Enumeration[String]]
    val props = Collections.list(enums)
    var i = props.size() - 1
    while (i > -1) {
      val item = props.get(i)
      if (item.startsWith("jitRole")) {
        val x = item.indexOf(".")
        val s = item.substring(7, x)
        val j = Integer.parseInt(s).intValue()
        if (j > lastRoleIndx) lastRoleIndx = j
      }
      i -= 1
    }
  }

  def jitProperty(pnam: String, pval: String) {
    properties.put("jitRole" + roleIndx + "." + pnam, pval)
  }

  def jitClass(className: String) {
    jitProperty("jitClass", className)
  }

  def inode(inodeRoleName: String) {
    jitProperty("inodeRole", inodeRoleName)
  }

  def map(mapRoleName: String) {
    jitProperty("mapRole", mapRoleName)
  }

  def jitSubRole(subRoleName: String) {
    jitProperty("jitSubRole", subRoleName)
  }
}