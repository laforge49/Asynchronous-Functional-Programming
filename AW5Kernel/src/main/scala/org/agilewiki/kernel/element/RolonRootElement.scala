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

import org.agilewiki.kernel.operation.Role
import org.agilewiki.kernel.element.operation.Deleting

import java.util.Map
import component.TreeMapContainerComponent
import util.jit.structure.{JitElement, _JitRolon}

class RolonRootElement extends BlockElement
                with TreeMapContainerComponent with _JitRolon {

  override def getRolonElement = this

  def setRolonType(rolonType: String) {
    attributes.put("rolonType", rolonType)
  }

  def rolonType = attributes.get("rolonType")

  private def _rolonType = attributes._get("rolonType")

  private var _rolonRole: Role = null

  def rolonRole = {
    deserialize
    if (_rolonRole == null) {
      _rolonRole = Kernel(systemContext).roles.role(rolonType)
    }
    _rolonRole
  }

  def locateOpStack(opType: String) = {
    rolonRole.opStack(opType)
  }

  def rolonIsA(roleType: String) = {
    rolonRole.isA(roleType)
  }

  override protected def load {
    _rolonRole = null
  }

  override def deleting {
    Deleting(this)
  }
}
