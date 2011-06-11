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

import org.agilewiki.kernel.element.Element
import org.agilewiki.kernel.element.KernelINodeElement
import jits.KernelINodeHandleElement
import util.jit.{JitNamedJitTreeMap, JitNamedVariableJitTreeMap, Jits}
import util.jit.structure.JitElement

private[kernel] trait KernelBTreeContainerComponent
  extends _BTreeContainerComponent {
  this: Element =>

  override protected def defineContents = {
    new KernelBTreeContainer
  }

  override def contents: KernelBTreeContainer = {
    _contents.asInstanceOf[KernelBTreeContainer]
  }

  override protected def isINode = isInstanceOf[KernelINodeElement]

  private[kernel] class KernelBTreeContainer extends _BTreeContainer {

    override protected def createReferenceElement = Jits(systemContext).
      createJit(KERNEL_INODE_HANDLE_ELEMENT_ROLE_NAME).
      asInstanceOf[KernelINodeHandleElement]

    override protected def initializeReferenceElement(re: Reference) {
      re.asInstanceOf[KernelINodeHandleElement].set(iNodeType)
    }

    override protected def iNodeType = KERNEL_INODE_ELEMENT_ROLE_NAME

    override protected def createTerminatorElement = {
      if (leaf.getBoolean) createLeafTerminatorElement
      else createReferenceElement
    }

    protected def createLeafTerminatorElement = super.createTerminatorElement

    override protected def createEmbeddedElementsContainer(leaf: Boolean) = {
      if (leaf) createLeafElementsContainer
      else Jits(systemContext).createJit(NAKED_KERNEL_INODE_HANDLE_MAP_ROLE_NAME).
        asInstanceOf[JitNamedJitTreeMap]
    }

    protected def createLeafElementsContainer: JitNamedJitTreeMap = {
      JitNamedVariableJitTreeMap.createJit(systemContext)
    }
  }
}
