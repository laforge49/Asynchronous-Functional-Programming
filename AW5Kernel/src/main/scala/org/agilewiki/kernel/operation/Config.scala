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
package kernel
package operation

import java.util.{Collections, Enumeration, Properties}


/**
 * Config class for rolon properties configuration
 *
 * @author Alex K.
 */
class Config(properties: Properties) {
  protected var roleIndx = 0
  var lastRoleIndx = 0
  private var roles = Map[String, Int]()
  protected var associatedItems = Map[String, Int]()

  require(properties != null, "Properties can't be null")

  populateParameters

  private def populateParameters: Unit = {
    setLastRoleIdx
    for (i <- 1 to lastRoleIndx) {
      val roleName: String = properties.get("role" + i + ".name").asInstanceOf[String]
      roles += (roleName -> i)
      val temp: String = "role" + i
      var idx = 0
      do {
        idx += 1
      } while (properties.containsKey(temp + "." + idx + ".type")
              || properties.containsKey(temp + "." + idx + ".include")
              || properties.containsKey(temp + "." + idx + ".name")
      )
      associatedItems += (temp -> (idx - 1))
    }
  }

  /**
   * Starts a new role configuration
   * @param roleName The name of the new role
   */
  def role(roleName: String): Unit = {
    if (roles contains roleName)
    // to point index on existing role
      roleIndx = roles(roleName) else {
      // to add new role and point index
      lastRoleIndx += 1
      roleIndx = lastRoleIndx
      roles += (roleName -> roleIndx)
      properties.put("role" + roleIndx + ".name", roleName)
    }
  }

  /**
   * Defines the rolon root element type for use when a new rolon is created 
   * with a given role.
   * @param rolonRootElementType The kernel element type.
   */
  def rootElementType(rolonRootElementType: String) {
    properties.put("role" + roleIndx + ".rootElementType", rolonRootElementType)
  }

  def jitSubRole(subRoleName: String) {
    properties.put("role" + roleIndx + ".jitSubRole", subRoleName)
  }

  /**
   * Includes another role identified by name in the current 
   * role being configured.
   * @param name The name of the role being included
   */
  def include(name: String) = {
    var idx = 1
    val indexedRoleName = "role" + roleIndx
    if (associatedItems.contains(indexedRoleName)) {
      val temp = associatedItems(indexedRoleName)
      idx = temp + 1
    }
    properties.put("role" + roleIndx + "." + idx + ".include", name)
    associatedItems += ("role" + roleIndx -> idx)
  }

  /**
   * Add an operation to the current role being configured.
   * @param op An instance of the operation being added 
   */
  def op(op: Operation) = {
    var idx = 1
    val indexedRoleName = "role" + roleIndx
    if (associatedItems.contains(indexedRoleName)) {
      val temp = associatedItems(indexedRoleName)
      idx = temp + 1
    }
    properties.put("role" + roleIndx + "." + idx + ".type", op.operationType)
    properties.put("role" + roleIndx + "." + idx + ".class", op.getClass.getName)
    associatedItems += ("role" + roleIndx -> idx)
  }

  /**
   * Add an property to the current role being configured.
   * @param name The name of the property being added.
   * @param value The value of the property being added. The default value is "".
   */
  def property(name: String)(implicit value: String) = {
    var idx = 1
    val indexedRoleName = "role" + roleIndx
    if (associatedItems.contains(indexedRoleName)) {
      val temp = associatedItems(indexedRoleName)
      idx = temp + 1
    }
    properties.put("role" + roleIndx + "." + idx + ".name", name)
    properties.put("role" + roleIndx + "." + idx + ".value", value)
    associatedItems += ("role" + roleIndx -> idx)
  }

  implicit val value = ""

  /**
   * Add ark manager to the current role being configured.
   * @param manager The name of the home.
   */

  def arkManager(roleName: String, manager: String) = {
    role(roleName)
    properties.put("role" + roleIndx + ".arkManager", manager)
  }

  private def setLastRoleIdx: Unit = {
    val enums = properties.keys.asInstanceOf[Enumeration[String]]
    val props = Collections.list(enums)
    var i = props.size() - 1
    while (i > -1) {
      val item = props.get(i)
      if (item.startsWith("role")) {
        val x = item.indexOf(".")
        val s = item.substring(4, x)
        val j = Integer.parseInt(s).intValue()
        if (j > lastRoleIndx) lastRoleIndx = j
      }
      i -= 1
    }
  }
}