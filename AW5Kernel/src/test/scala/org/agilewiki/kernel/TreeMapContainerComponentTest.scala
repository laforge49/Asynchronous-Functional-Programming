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

import element.operation.{_Elements, ElementConfig}
import jits.{KernelBlockHandleElement, KernelINodeHandleElement}
import util.jit.{JitConfig, Jits}
import operation.Config
import org.specs.SpecificationWithJUnit

import org.agilewiki.kernel.element.EmbeddedTransientElement
import org.agilewiki.kernel.element.TreeMapTransientElement
import java.util.Properties
import util.jit.structure.JitElement

/**
 * Specification for the TreeMapContainerComponent functionality
 */

class TreeMapContainerComponentTest extends SpecificationWithJUnit {
  val properties = _Elements.defaultConfiguration("Master")
  new JitConfig(properties) {
    role(EMPTY_JIT_ELEMENT_ROLE_NAME)
    jitClass(classOf[JitElement] getName)

    role(KERNEL_BLOCK_HANDLE_ELEMENT_ROLE_NAME)
    jitClass(classOf[KernelBlockHandleElement] getName)

    role(KERNEL_INODE_HANDLE_ELEMENT_ROLE_NAME)
    jitClass(classOf[KernelINodeHandleElement] getName)

    role(NAKED_KERNEL_INODE_HANDLE_MAP_ROLE_NAME)
    jitClass(classOf[KernelINodeHandleElement] getName)
  }

  new ElementConfig(properties) {
    role("TreeMapTransientElement")
    kernelElementType(classOf[TreeMapTransientElement] getName)

    role("EmbeddedTransientElement")
    kernelElementType(classOf[EmbeddedTransientElement] getName)
  }

  val systemContext = new _Elements(properties)

  val tmccd = Jits(systemContext).createJit("TreeMapTransientElement").asInstanceOf[TreeMapTransientElement]

  "TreeMapContainerComponent" should {

    "Add a name/value pair" in {
      tmccd.contents.add("test", "EmbeddedTransientElement")
      tmccd.contents.size must be greaterThan (0)
    }

    "Return the value of element" in {
      val emElem = tmccd.contents.add("test", "EmbeddedTransientElement")
      tmccd.contents.get("test").asInstanceOf[JitElement] must be equalTo (emElem)
    }

    "checking existence of element" in {
      tmccd.contents.add("test", "EmbeddedTransientElement")
      tmccd.contents.contains("test") must be equalTo (true)
    }

    "Delete an element value" in {
      tmccd.contents.add("test", "EmbeddedTransientElement")
      tmccd.contents.delete("test")
      tmccd.contents.size must be equalTo (0)
      tmccd.contents.sequence.current == null must be equalTo true
    }

    "Return the current size" in {
      tmccd.contents.size must be equalTo (0)
      tmccd.contents.add("first", "EmbeddedTransientElement")
      tmccd.contents.add("second", "EmbeddedTransientElement")
      tmccd.contents.size must be equalTo (2)
      var count = 0
      var it = tmccd.contents.iterator
      while (it.hasNext) {
        it.next
        count += 1
      }
      count must be equalTo (2)
      tmccd.contents.delete("first")
      tmccd.contents.size must be equalTo (1)
      count = 0
      it = tmccd.contents.iterator
      while (it.hasNext) {
        it.next
        count += 1
      }
      count must be equalTo (1)
    }

  }
}
