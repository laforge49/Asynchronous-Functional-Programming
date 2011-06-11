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
package signature

import org.agilewiki.kernel.element.RolonRootElement

abstract trait Sig2[R, A1, A2] {
  this: Operation =>

  def process(
          roleName: String,
          rolonContext: RolonRootElement,
          targetRolon: RolonRootElement,
          arg1: A1,
          arg2: A2): R = {
    throw new UnsupportedOperationException
  }

  def apply(
          targetRolon: RolonRootElement,
          arg1: A1,
          arg2: A2): R = {
    val t = operation("", targetRolon, operationType)
    val opStack = t._1
    opStack.op.asInstanceOf[Sig2[R, A1, A2]]
            .process(opStack.roleName, t._2, targetRolon, arg1, arg2)
  }

  def superOp(
          roleName: String,
          contextRolon: RolonRootElement,
          targetRolon: RolonRootElement,
          arg1: A1,
          arg2: A2): R = {
    val t = operation(roleName, contextRolon, operationType)
    var opStack = t._1
    if (roleName != opStack.roleName || opStack.stack == null)
      throw new UnsupportedOperationException
    opStack = opStack.stack
    opStack.op.asInstanceOf[Sig2[R, A1, A2]]
            .process(opStack.roleName, t._2, targetRolon, arg1, arg2)
  }

}
