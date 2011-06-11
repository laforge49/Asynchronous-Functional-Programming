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

import org.agilewiki.kernel.element.BlockTransientElement
import org.agilewiki.kernel.element.INodeTransientElement
import org.agilewiki.kernel.element.EmbeddedTestElement
import org.agilewiki.kernel.element.EmbeddedElement
import org.agilewiki.kernel.element.ReferenceTestElement
import util.jit.Jits

object BTreeTransientContainerComponent {
  var leaf = 10000
  var root = 10000
  var inode = 10000
}

trait BTreeTransientContainerComponent extends _BTreeContainerComponent {
  this: BlockTransientElement =>

  override protected def defineContents = {
    new BTreeTransientContainer
  }

  override def contents = {
    _contents.asInstanceOf[BTreeTransientContainer]
  }

  protected override def isINode = isInstanceOf[INodeTransientElement]

  class BTreeTransientContainer extends _BTreeContainer {

    override protected def createTerminatorElement = {
      val te = Jits(systemContext).createJit("EmbeddedTestElement").asInstanceOf[EmbeddedTestElement]
      te
    }

    override protected def maxNodeSize = {
      var rv = 10000
      if (isINode) {
        if (isLeaf) {
          rv = BTreeTransientContainerComponent.leaf
        } else {
          rv = BTreeTransientContainerComponent.inode
        }
      } else {
        rv = BTreeTransientContainerComponent.root
      }
      rv
    }

    override protected def iNodeType = "INodeTransientElement"

    protected def createINode = Jits(systemContext).createJit("INodeTransientElement").
      asInstanceOf[INodeTransientElement]

    protected override def createReferenceElement = Jits(systemContext).createJit("ReferenceTestElement").
      asInstanceOf[ReferenceTestElement]

    protected def initializeReferenceElement(re: Reference) {
      val inodeElement = createINode
      re.asInstanceOf[ReferenceTestElement].set(inodeElement)
    }

    override def add(name: String, elementType: String) = {
      val embeddedElement = Jits(systemContext).createJit(elementType).asInstanceOf[EmbeddedElement]
      put(name, embeddedElement)
      embeddedElement
    }

  }

}
