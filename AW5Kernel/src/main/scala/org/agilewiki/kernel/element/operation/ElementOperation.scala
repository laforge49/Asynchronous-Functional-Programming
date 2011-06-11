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

import org.agilewiki.kernel.element.Element
import util.jit.Jits

/**
 * Base class for all element operations.
 * This class should be extended to define element operation's type and logic.
 */
abstract class ElementOperation {

  /**
   * Identifies the operation type
   */
  def operationType: String

  /**
   * Retrieves the ElementOpStack. And throws 
   * an Unsupported Operation Exception when the ElementOperation is not found.
   * @param roleType The name of the role implementing the operation, or null.                                                    
   * @param targetElement The Element targeted by the operation of type opType  
   * @param opType The type of the operation targeting the targetElement
   * @return An ElementOpStack.
   */
  protected def elementOpStack(
          roleType: String,
          targetElement: Element,
          opType: String) = {
    val t = if (roleType.length == 0) {
      _elementOpStack(targetElement, opType)
    } else {
      _elementOpStack(roleType, targetElement, opType)
    }
    if (t == null) {
      if (roleType.length == 0) {
        throw new UnsupportedOperationException(opType + " on ElementType " + targetElement.jitRoleName)
      } else {
        throw new UnsupportedOperationException(roleType + "." + opType + " on ElementType " + targetElement.jitRoleName)
      }
    }
    t
  }

  /**
   * Attempts to retrieves the ElementOpStack by trying to 
   * locate the operation of a given type, in the roles implemented by a 
   * given element.
   * @param targetElement The Element targeted by the operation of type opType  
   * @param opType The type of the operation targeting the targetElement
   * @return null if the operation of type opType is not found.
   */
  protected def _elementOpStack(
          targetElement: Element,
          opType: String) = {
    val role = Jits(targetElement.systemContext).jitRole(targetElement.jitRoleName).asInstanceOf[ElementRole]
    role.opStack(opType)
  }

  /**
   * Attempts to retrieves the ElementOpStack by trying to 
   * locate the given role type, in the  
   * given element.
   * @param roleType The required role.
   * @param targetElement The Element targeted by the operation of type opType  
   * @param opType The type of the operation targeting the targetElement
   * @return null if the operation of type opType is not found, Or 
   * an ElementOpStack.
   */
  protected def _elementOpStack(
          roleType: String,
          targetElement: Element,
          opType: String) = {
    var t: ElementOpStack = null
    val elementRole = Jits(targetElement.systemContext).jitRole(targetElement.jitRoleName).asInstanceOf[ElementRole]
    if (elementRole.isA(roleType)) {
      t = elementRole.opStack(opType)
    }
    t
  }
}
