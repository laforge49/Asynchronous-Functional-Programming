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
package org.agilewiki
package kernel
package operation

import java.util.HashMap
import util.SystemConfigurationComponent

/**
 * A kernal class used to maintain roles in a kernel.
 */
private[kernel] class Roles(systemConfigurationComponent: SystemConfigurationComponent) {
  protected val roleMap = new HashMap[String, Role]

  loadRole()

  def role(roleName: String) = roleMap.get(roleName)

  def contains(roleName: String) = roleMap.containsKey(roleName)

  def roleIterator = roleMap.values.iterator

  def roleSet = roleMap.values

  /**
   * Fetches a role with a given role name
   * @param roleName The name of the role to be fetched
   * @returns the role being fetched if possible and throw an IllegalArgumentException 
   * if no role is define with the given roleName.
   */
  private def irole(roleName: String): Role = {
    if (!roleMap.containsKey(roleName)) {
      loadRole(roleName)
    }
    roleMap.get(roleName)
  }

  private[kernel] def _role(roleName: String): Role = {
    if (!roleMap.containsKey(roleName)) {
      null
    }
    roleMap.get(roleName)
  }

  private def loadRole() {
    var i = 0
    while (i > -1) {
      i += 1
      val roleName = systemConfigurationComponent.configuration.property("role" + i + ".name")
      if (roleName == null) {
        i = -1
      }
      else if (roleMap.get(roleName) == null)
        loadRole(roleName)(i - 1)
    }
  }

  /**
   * Loads a role from the configuration. if the named role doesn't exist in the 
   * configuration, this function will throw an IllegalArgumentExecption. 
   * @param roleName The name of the role to be loaded
   */
  protected def loadRole(roleName: String)(implicit j: Int) = {
    var i = j
    while (i > -1) {
      i += 1
      val roleNm = systemConfigurationComponent.configuration.property("role" + i + ".name")
      if (roleNm == null) {
        throw new IllegalArgumentException("Unknown role name: " + roleName)
      } else if (roleName == roleNm) {
        val _role = new Role(roleName)
        roleMap.put(roleName, _role)
        val rootElementType = systemConfigurationComponent.configuration.property("role" + i + ".rootElementType")
        if (rootElementType != null) {
          _role.setRootElementType(rootElementType)
        }
        val arkManager = systemConfigurationComponent.configuration.property("role" + i + ".arkManager")
        if (arkManager != null) {
          _role.setArkManager(arkManager)
        }
        _role.initialize
        var j = 0
        var p = 1
        //var q = 0
        while (j > -1) {
          j += 1
          val prefix = "role" + i + "." + j + "."
          val subRoleName = systemConfigurationComponent.configuration.property(prefix + "include")
          if (subRoleName != null) {
            val subRole = irole(subRoleName)
            _role.include(subRole)
          } else {
            val opType = systemConfigurationComponent.configuration.property(prefix + "type")
            if (opType != null) {
              val opClass = systemConfigurationComponent.configuration.property(prefix + "class")
              _role.add(opType, opClass)
            } else {
              val propName = systemConfigurationComponent.configuration.property(prefix + "name")
              if (null != propName) {
                val propValue = systemConfigurationComponent.configuration.property(prefix + "value")
                _role.addProperty(propName, propValue)
              } else {
                val propSetName = systemConfigurationComponent.configuration.property(prefix + "propertySetName")
                if (null != propSetName) {
                  //p += 1
                  val reqPropValue = systemConfigurationComponent.configuration.property(prefix + "requiredPropertyName")
                  _role.addProperty(propSetName + ".requiredPropertyName", reqPropValue)
                  //p += 1
                  var k = 0
                  var q = 0
                  while (k > -1) {
                    k += 1
                    val propPrefix = propSetName + "." + p + "."

                    val propName = systemConfigurationComponent.configuration.property(prefix + k + ".propertyName")
                    if (null != propName) {
                      val propValue = systemConfigurationComponent.configuration.property(prefix + k + ".propertyValue")
                      _role.addProperty(propPrefix + propName, propValue)
                    } else {
                      var m = 0
                      while (m > -1) {
                        m += 1
                        val childPropPrefix = "role" + i + "." + j + "." + k + "." + m + "."
                        val childPropName = systemConfigurationComponent.configuration.property(childPropPrefix + "propertyName")
                        if (null != childPropName) {
                          q += 1
                          val childPropValue = systemConfigurationComponent.configuration.property(childPropPrefix + "propertyValue")
                          _role.addProperty(propSetName + "." + q + "." + childPropName, childPropValue)
                          m += 1
                          val childPropPrefix1 = "role" + i + "." + j + "." + k + "." + m + "."
                          val childPropName1 = systemConfigurationComponent.configuration.property(childPropPrefix1 + "propertyName")
                          if (null != childPropName1) {
                            val childPropValue1 = systemConfigurationComponent.configuration.property(childPropPrefix1 + "propertyValue")
                            _role.addProperty(propSetName + "." + q + "." + childPropName1, childPropValue1)
                          }
                        } else {
                          m = -1
                        }
                      }
                    }
                    if (systemConfigurationComponent.configuration.property(prefix + k + ".propertyName") == null &&
                            systemConfigurationComponent.configuration.property("role" + i + "." + j + "." + k + "." + 1 + "." + "propertyName") == null)
                      k = -1
                  }
                } else {
                  j = -1
                }
              }
            }
          }
        }
        i = -1
      }
    }
  }

  implicit val j = 0
}
