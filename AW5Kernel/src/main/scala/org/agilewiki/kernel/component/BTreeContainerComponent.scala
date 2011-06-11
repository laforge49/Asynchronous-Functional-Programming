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

import element.Element
import element.INodeElement
import jits.INodeLinkElement
import util.jit.{JitNamedJitTreeMap, JitNamedVariableJitTreeMap, Jits}

private[kernel] trait BTreeContainerComponent extends _BTreeContainerComponent {
  this: Element =>

  override protected def defineContents = {
    new BTreeContainer
  }

  override def contents: BTreeContainer = {
    _contents.asInstanceOf[BTreeContainer]
  }

  protected override def isINode = isInstanceOf[INodeElement]

  private[kernel] class BTreeContainer extends _BTreeContainer {

    protected override def createReferenceElement = Jits(systemContext).createJit(INODE_LINK_ELEMENT_ROLE_NAME).
      asInstanceOf[INodeLinkElement]

    protected def initializeReferenceElement(re: Reference) {
      val rolonUuid = BTreeContainerComponent.this.asInstanceOf[Element].rolonUuid
      val inodeElement = transactionContexts.createINodeElement(rolonUuid, iNodeType)
      val inodeUuid = inodeElement.uuid
      re.asInstanceOf[INodeLinkElement].setReferenceUuid(inodeUuid)
    }

    override protected def iNodeType = INODE_ELEMENT_ROLE_NAME

    override protected def createTerminatorElement = {
      if (leaf.getBoolean) createLeafTerminatorElement
      else createReferenceElement
    }

    protected def createLeafTerminatorElement = super.createTerminatorElement

    override protected def createEmbeddedElementsContainer(leaf: Boolean) = {
      if (leaf) createLeafElementsContainer
      else Jits(systemContext).createJit(NAKED_INODE_HANDLE_MAP_ROLE_NAME).asInstanceOf[JitNamedJitTreeMap]
    }

    protected def createLeafElementsContainer: JitNamedJitTreeMap = {
      JitNamedVariableJitTreeMap.createJit(systemContext)
    }
  }
}
