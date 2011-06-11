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
package element
package operation

import java.util.{Properties}
import util.jit.JitConfig

class ElementConfig(properties: Properties)
        extends JitConfig(properties) {
  protected var associatedItems = Map[String, Int]()

  populateParameters

  protected def populateParameters: Unit = {
    if (lastRoleIndx < 1)
      return
    for (i <- 1 to lastRoleIndx) {
      val roleName: String = properties.get("jitRole" + i + ".name").asInstanceOf[String]
      roles += (roleName -> i)
      var idx = 0
      val temp: String = "jitRole" + i
      do {
        idx += 1
      } while (properties.containsKey(temp + "." + idx + ".include") ||
      properties.containsKey(temp + "." + idx + ".type"))
      associatedItems += (temp -> (idx - 1))
    }
  }

  override def jitClass(className: String) {
    throw new UnsupportedOperationException
  }

  /**
   * Defines the kernel element type for use when a new element is created
   * with a given element role.
   * @param kernelType The kernel element type.
   */
  def kernelElementType(kernelType: String) {
    properties.put("jitRole" + roleIndx + ".kernelElementType", kernelType)
  }

  /**
   * Includes another role identified by name in the current 
   * role being configured.
   * @param name The name of the role being included
   */
  def include(name: String) = {
    var idx = 1
    val indexedRoleName = "jitRole" + roleIndx
    if (associatedItems contains indexedRoleName) {
      val temp = associatedItems(indexedRoleName)
      idx = temp + 1
    }
    properties.put("jitRole" + roleIndx + "." + idx + ".include", name)
    associatedItems += ("jitRole" + roleIndx -> idx)
  }

  /**
   * Add an element operation to the current role being configured.
   * @param op An instance of the element operation being added 
   */
  def op(op: ElementOperation) = {
    var idx = 1
    val indexedRoleName = "jitRole" + roleIndx
    if (associatedItems contains indexedRoleName) {
      val temp = associatedItems(indexedRoleName)
      idx = temp + 1
    }
    properties.put("jitRole" + roleIndx + "." + idx + ".type", op.operationType)
    properties.put("jitRole" + roleIndx + "." + idx + ".class", op.getClass.getName)
    associatedItems += ("jitRole" + roleIndx -> idx)
  }
}