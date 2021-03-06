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
package component

import util.jit.{Jits, JitNamedJitTreeMap}
import util.jit.structure.JitElement
import element.Element

private[kernel] trait KernelBlockManagementElementBTreeContainer2Component
  extends KernelBTreeContainer2Component {
  this: Element =>

  override protected def defineContents = new KernelEmptyElementsBTreeContainer2

  override def contents = _contents.asInstanceOf[KernelEmptyElementsBTreeContainer2]

  private[kernel] class KernelEmptyElementsBTreeContainer2 extends KernelBTreeContainer2 {

    override protected def iNodeType = KERNEL_DISK_BLOCK_MANAGEMENT_ELEMENT_INODE2_ELEMENT_ROLE_NAME

    override protected def createLeafTerminatorElement = {
      Jits(systemContext).createJit(KERNEL_BLOCK_MANAGEMENT_ELEMENT_ROLE_NAME).
        asInstanceOf[JitElement]
    }

    override protected def createLeafElementsContainer = {
      Jits(systemContext).createJit(NAKED_KERNEL_BLOCK_MANAGEMENT_ELEMENT_MAP_ROLE_NAME).
        asInstanceOf[JitNamedJitTreeMap]
    }
  }

}
