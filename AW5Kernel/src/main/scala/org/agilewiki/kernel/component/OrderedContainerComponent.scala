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
package component

import org.agilewiki.kernel.element.Element
import util.jit.JitNamedJitList
import util.jit.structure.JitElement

/**
 * Provides access to and functionlity based around name/value pair attributes
 *
 *
 * @author Alex K.
 */
trait OrderedContainerComponent extends ContainerComponent {
  this: Element =>

  override protected def defineContents = {
    new OrderedContainer
  }

  override def contents: OrderedContainer = {
    _contents.asInstanceOf[OrderedContainer]
  }

  /**
   * Supports get,put,remove methods for retrieving, assigning
   * and removing Element values in the form of name/value pairs.
   */
  class OrderedContainer extends Container {
    private var _embeddedElementsContainer: JitNamedJitList = _

    override def containerWrapper = embeddedElementsContainer

    override def embeddedElementsContainer = {
      deserialize
      _embeddedElementsContainer
    }

    override def builder = {
      _embeddedElementsContainer = JitNamedJitList.createJit(systemContext)
      _embeddedElementsContainer.partness(OrderedContainerComponent.this, null, OrderedContainerComponent.this)
      _embeddedElementsContainer.jitByteLength
    }

    override def isJitDeserialized(name: String) = containerWrapper.isJitDeserialized(name)

    def move(moveKey: String, beforeKey: String, after: Boolean) {
      val rv = embeddedElementsContainer.move(moveKey, beforeKey, after)
      rv
    }

    /**
     * Returns the name of the selected sub-element
     * @param ndx The index of the selected sub-element
     * @return The name of the selected sub-element
     */
    def get(ndx: Int) = {
      _get(ndx)
    }

    /**
     * Returns the name of the selected sub-element
     * @param ndx The index of the selected sub-element
     * @return The name of the selected sub-element
     */
    private[kernel] def _get(ndx: Int) = embeddedElementsContainer.get(ndx)

    /**
     * Returns the position of the named sub-element.
     * @param key The name of the sub-element.
     * @return The position, or -1 if not found.
     */
    def index(key: String): Int = {
      embeddedElementsContainer.indexOf(key)
    }
  }

}
