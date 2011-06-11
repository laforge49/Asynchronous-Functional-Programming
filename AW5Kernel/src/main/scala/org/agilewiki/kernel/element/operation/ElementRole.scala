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
package element
package operation

import java.util.HashSet
import java.util.HashMap
import util.{Configuration, SystemComposite}
import util.jit.{Jits, JitRole}

object ElementRole {
  def apply(systemContext: SystemComposite, elementRoles: ElementRoles, roleNm: String, i: Int) = {
    val _role = new ElementRole(systemContext, roleNm)
    val kernelElementType = Configuration(systemContext).property("jitRole" + i + ".kernelElementType")
    if (kernelElementType != null) {
      _role.setKernelElementType(kernelElementType)
    }
    val jitSubRoleName = Configuration(systemContext).property("jitRole" + i + ".jitSubRole")
    if (jitSubRoleName != null) _role.setJitSubRoleName(jitSubRoleName)
    val inodeRoleName = Configuration(systemContext).property("jitRole" + i + ".inodeRole")
    if (inodeRoleName != null) _role.setINodeRoleName(inodeRoleName)
    val mapRoleName = Configuration(systemContext).property("jitRole" + i + ".mapRole")
    if (mapRoleName != null) _role.setMapRoleName(mapRoleName)
    var j = 0
    while (j > -1) {
      j += 1
      val prefix = "jitRole" + i + "." + j + "."
      val subRoleName = Configuration(systemContext).property(prefix + "include")
      if (subRoleName != null) {
        val subRole = elementRoles.loadRole(subRoleName).asInstanceOf[ElementRole]
        _role.include(subRole)
      } else {
        val opType = Configuration(systemContext).property(prefix + "type")
        if (opType != null) {
          val opClass = Configuration(systemContext).property(prefix + "class")
          _role.add(opType, opClass)
        } else {
          j = -1
        }
      }
    }
    _role
  }
}

/**
 * A role provides a tree-style hierarchical structure to organize operations.
 * <p>
 * The roles hierarchy is used to make all the operations of a descendent role 
 * available to all its ancestors in the tree.
 * <p>
 * Each role have a set of operations indexed by operation type.
 * @param _roleName The name of the role
 */
class ElementRole(systemContext: SystemComposite, _roleName: String) extends JitRole(systemContext, _roleName) {
  protected val roleTypes = new HashSet[String]
  private[kernel] val opStacks = new HashMap[String, ElementOpStack]
  protected var kernelType: String = null
  private var _inodeRoleName: String = _
  private var _mapRoleName: String = _

  roleTypes.add(roleName)

  def printRole {
    System.err.println("")
    System.err.println("jitRole name " + roleName)
    val it1 = opStacks.keySet.iterator
    while (it1.hasNext) {
      System.err.println("op " + it1.next)
    }
  }

  /**
   * Returns the element class name.
   * @return The element class name.
   */
  override def jitClassName = kernelElementType

  override def setJitClassName(jitClassName: String) {
    throw new UnsupportedOperationException
  }

  /**
   * Assigns a kernel element type to the role.
   * @param kernelElementType The kernel element type used when
   * instantiating an element using this role.
   */
  def setKernelElementType(kernelElementType: String) {
    kernelType = kernelElementType
  }

  /**
   * Returns the kernel element type assigned to the role.
   * If the kernel element type is not defined, the role name is returned.
   * @return The kernel element type
   */
  def kernelElementType = {
    kernelType
  }

  /**
   * Checks if this role includes a role with a given name.
   * @param roleType The given role name to be checked.
   * @return True when the role actually has
   */
  def isA(roleType: String) = roleTypes.contains(roleType)

  def opStack(operationType: String) = opStacks.get(operationType)

  def operation(operationType: String) = opStack(operationType).op

  /**
   * Includes a role with all its sub-roles in the current role, 
   * which makes the "isA" function transitive.
   * @param role The role being included in the current role
   */
  private[kernel] def include(role: ElementRole) {
    roleTypes.addAll(role.roleTypes)
    opStacks.putAll(role.opStacks)
  }

  /**
   * Add an operation to a role indexed by operation type.
   * @param opType The type of the operation being added
   * @param opClass The class name of the operation being added 
   */
  private[kernel] def add(opType: String, opClass: String) {
    val op = create(opClass)
    val oldTop = opStack(opType)
    val newTop = new ElementOpStack(roleName, op, oldTop)
    opStacks.put(opType, newTop)
  }

  /**
   * Creates an operation object using the operation name.
   * @param cn Class Name of the operation.
   */
  protected def create(cn: String): ElementOperation = {
    val cl = this.getClass().getClassLoader()
    var c: Class[ElementOperation] = null
    try {
      c = cl.loadClass(cn).asInstanceOf[Class[ElementOperation]]
    } catch {
      case ex: ClassNotFoundException =>
        throw new IllegalStateException(
          "Unable to load class " + cn, ex)
    }
    var op: ElementOperation = null
    try {
      op = c.newInstance()
    } catch {
      case ex: Exception => throw new IllegalStateException(
        "Unable to instantiate class " + cn, ex)
    }
    op
  }

    def setINodeRoleName(inodeRoleName: String) {
      _inodeRoleName = inodeRoleName
    }

    def inodeRoleName = _inodeRoleName

    def inodeRole = Jits(systemContext).jitRole(inodeRoleName)

    def createINode = inodeRole.createJit

    def setMapRoleName(mapRoleName: String) {
      _mapRoleName = mapRoleName
    }

    def mapRoleName = _mapRoleName

    def mapRole = Jits(systemContext).jitRole(mapRoleName)

    def createMap = mapRole.createJit
}
