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

import org.agilewiki.kernel.element.RolonRootElement
import org.agilewiki.kernel.element.{BlockElement, _BlockElement}

/**
 * Base class for all operations.
 * This class should be extended to define operation's type and logic.
 */
abstract class Operation {

  /**
   * Identifies the operation type
   */
  def operationType: String

  /**
   * Retrieves the (OpStack,Rolon) tuple. And throws 
   * an Unsupported Operation Exception when the Operation is not found.
   * @param roleType The name of the role implementing the operation, or null.
   * @param targetRolon The Rolon targeted by the operation of type opType  
   * @param opType The type of the operation targeting the targetRolon
   * @return A Tuple2[OpStack, Rolon] containing the actual operation 
   * objects having the type opType, with the Rolon implementing it.
   */
  protected def operation(
          roleType: String,
          targetRolon: RolonRootElement,
          opType: String): (OpStack, RolonRootElement) = {
    val t: (OpStack, RolonRootElement) = if (roleType.length == 0) {
      _operation(targetRolon, opType)
    } else {
      _operation(roleType, targetRolon, opType)
    }
    if (t == null) {
      if (roleType.length == 0) {
        throw new UnsupportedOperationException(opType + " on RolonType " + targetRolon.rolonType)
      } else {
        throw new UnsupportedOperationException(roleType + "." + opType + " on RolonType " + targetRolon.rolonType)
      }
    }
    t
  }

  /**
   * Attempts to retrieves the (OpStack,Rolon) tuple by trying to 
   * locate the operation of a given type, in the roles implemented by a 
   * given rolon, or any of its ancestors.
   * @param targetRolon The Rolon targeted by the operation of type opType  
   * @param opType The type of the operation targeting the targetRolon
   * @return null if the operation of type opType is not found, Or 
   * a Tuple2[OpStack, Rolon] containing the actual operation 
   * objects having the type opType, with the Rolon implementing it.
   */
  protected def _operation(
          targetRolon: RolonRootElement,
          opType: String): (OpStack, RolonRootElement) = {
    var contextRolon = targetRolon
    var opStack = targetRolon.locateOpStack(opType)
    var t: (OpStack, RolonRootElement) = null
    if (opStack != null) {
      t = (opStack, targetRolon)
    }
    t
  }

  /**
   * Attempts to retrieves the (OpStack,Rolon) tuple by trying to 
   * locate the given role type, in the  
   * given rolon, or any of its ancestors.
   * @param roleType The required role.
   * @param targetRolon The Rolon targeted by the operation of type opType  
   * @param opType The type of the operation targeting the targetRolon
   * @return null if the operation of type opType is not found, Or 
   * a Tuple2[OpStack, Rolon] containing the actual operation 
   * objects having the type opType, with the Rolon implementing it.
   */
  protected def _operation(
          roleType: String,
          targetRolon: RolonRootElement,
          opType: String): (OpStack, RolonRootElement) = {
    var contextRolon = targetRolon
    var t: (OpStack, RolonRootElement) = null
    if (targetRolon.rolonIsA(roleType)) {
      val roles = Kernel(targetRolon.systemContext).roles
      val role = roles.role(roleType)
      val opStack = role.opStack(opType)
      t = (opStack, targetRolon)
    }
    t
  }
}
