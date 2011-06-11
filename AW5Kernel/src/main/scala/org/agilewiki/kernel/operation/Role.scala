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

import java.util.{UUID, TreeSet, Properties}

/**
 * A role provides a tree-style hierarchical structure to organize operations.
 * <p>
 * The roles hierarchy is used to make all the operations of a descendent role 
 * available to all its ancestors in the tree.
 * <p>
 * Each role have a set of operations indexed by operation type.
 * @param _roleName The name of the role
 */
class Role(_roleName: String) {
  protected var roleTypes = Set[String]()
  protected var opStacks = Map[String, OpStack]()
  protected val properties = new Properties
  protected var elementType: String = null
  protected var arkManager: String = null
  protected var arkMgrs: Set[String] = null
  protected var propertyNames: TreeSet[String] = null

  roleTypes += roleName

  def initialize = {
    arkMgrs = Set[String]()
    if (getArkManager != null) arkMgrs += getArkManager
  }

  /**
   * @return the actual name of the role
   */
  def roleName = _roleName

  def superRoles = roleTypes

  /**
   * Assigns an element type to the role.
   * The element type is used when instantiating the RolonRootElement.
   * @param rolonRootElementType The element type used when 
   * instantiating a rolon using this role.
   */
  def setRootElementType(rolonRootElementType: String) {
    elementType = rolonRootElementType
  }

  /**
   * Returns the rolon root element type assigned to the role.
   * If the element type is not defined, the role name is returned.
   * @return The rolon root element type
   */
  def rootElementType = {
    if (elementType == null) _roleName else elementType
  }

  /**
   * Checks if this role includes a role with a given name.
   * @param roleType The given role name to be checked.
   * @return True when the role actually has
   */
  def isA(roleType: String) = roleTypes.contains(roleType)

  def opStack(operationType: String) = opStacks.get(operationType) match {
    case None => null
    case Some(m) => m
  }

  def operation(operationType: String) = opStack(operationType).op

  def property(name: String) = properties.get(name)

  def hasProperty(name: String) = properties.containsKey(name)

  /**
   * Add a property to an indexed role
   * @param name The name of the property being added
   * @param value The value of the property being added 
   */
  private[kernel] def addProperty(name: String, value: String) {
    properties.put(name, value)
  }

  /**
   * Includes a role with all its sub-roles in the current role, 
   * which makes the "isA" function transitive.
   * @param role The role being included in the current role
   */
  private[kernel] def include(role: Role) {
    roleTypes ++= role.roleTypes
    opStacks ++= role.opStacks
    role.arkMgrs ++= getArkManagers
  }

  /**
   * Add an operation to a role indexed by operation type.
   * @param opType The type of the operation being added
   * @param opClass The class name of the operation being added 
   */
  private[kernel] def add(opType: String, opClass: String) {
    val op = create(opClass)
    val oldTop = opStack(opType)
    val newTop = new OpStack(roleName, op, oldTop)
    opStacks += (opType -> newTop)
  }

  /**
   * Creates an operation object using the operation name.
   * @param cn Class Name of the operation.
   */
  protected def create(cn: String): Operation = {
    val cl = this.getClass().getClassLoader()
    var c: Class[Operation] = null
    try {
      c = cl.loadClass(cn).asInstanceOf[Class[Operation]]
    } catch {
      case ex: ClassNotFoundException =>
        throw new IllegalStateException(
          "Unable to load class " + cn, ex)
    }
    var op: Operation = null
    try {
      op = c.newInstance()
    } catch {
      case ex: Exception => throw new IllegalStateException(
        "Unable to instantiate class " + cn, ex)
    }
    op
  }

  def getProperties = properties

  /**
   * Assigns an ark manager to the role.
   * @param arkHome
   */
  def setArkManager(manager: String) {
    arkManager = manager
  }

  /**
   * Returns the ark manager assigned to the role.
   * @return The arkManager
   */
  def getArkManager = arkManager

  def addArkMgr(manager: String) = {
    if (manager != null) arkMgrs += manager
  }

  /**
   * Returns the set of ark managers assigned to the role and sub roles.
   * @return The arkManagers
   */
  def getArkManagers = arkMgrs

  def propertyNameSet = {
    if (propertyNames == null) {
      propertyNames = new TreeSet[String] {
        var it = properties.keySet.iterator
        while (it.hasNext) {
          val k = String.valueOf(it.next)
          add(k)
        }
      }
    }
    propertyNames
  }
}
