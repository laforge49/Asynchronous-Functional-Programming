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

import element._
import element.operation.{ElementConfig, _Elements}
import jits.{KernelINodeHandleElement, KernelBlockHandleElement}
import org.specs.SpecificationWithJUnit

import org.agilewiki.kernel.component.BlockTransientComponent
import org.agilewiki.kernel.component.BTreeTransientContainerComponent
import util.jit._
import structure.JitElement

/**
 * Specification for the _BTreeContainerComponent functionality
 */

class BTreeContainerComponentTest extends SpecificationWithJUnit {
  BTreeTransientContainerComponent.root = 10000
  BTreeTransientContainerComponent.leaf = 10000
  BTreeTransientContainerComponent.inode = 10000

  val properties = _Elements.defaultConfiguration("Master")

  new ElementConfig(properties) {
    role("BlockTransientElement")
    kernelElementType(classOf[BlockTransientElement] getName)

    role("EmbeddedTransientElement")
    kernelElementType(classOf[EmbeddedTransientElement] getName)

    role("EmbeddedTestElement")
    kernelElementType(classOf[EmbeddedTestElement] getName)

    role("ReferenceTestElement")
    kernelElementType(classOf[ReferenceTestElement] getName)

    role("INodeTransientElement")
    kernelElementType(classOf[INodeTransientElement] getName)
  }

  val systemContext = new _Elements(properties)

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

  val btccd = Jits(systemContext).createJit("BlockTransientElement").asInstanceOf[BlockTransientElement]
  btccd.partness(null, "btccd", null)

  "_BTreeContainerComponent" should {

    "Add a name/value pair" in {
      BlockTransientComponent.clear
      BlockTransientComponent.writeLocks.size must be equalTo (0)
      BlockTransientComponent.dirtyBlocks.size must be equalTo (0)
      btccd.contents.add("test", "EmbeddedTransientElement")
      btccd.contents.size must be greaterThan (0)
      BlockTransientComponent.writeLocks.size must be equalTo (1)
      BlockTransientComponent.dirtyBlocks.size must be equalTo (1)
      BlockTransientComponent.clear
      BlockTransientComponent.writeLocks.size must be equalTo (0)
      BlockTransientComponent.dirtyBlocks.size must be equalTo (0)
    }

    "Return the value of element" in {
      BlockTransientComponent.clear
      val emElem = btccd.contents.add("test", "EmbeddedTransientElement")
      btccd.contents.get("test") must be equalTo (emElem)
      BlockTransientComponent.writeLocks.size must be equalTo (1)
      BlockTransientComponent.dirtyBlocks.size must be equalTo (1)
    }

    "checking existence of element" in {
      BlockTransientComponent.clear
      btccd.contents.add("test", "EmbeddedTransientElement")
      btccd.contents.contains("test") must be equalTo (true)
      BlockTransientComponent.writeLocks.size must be equalTo (1)
      BlockTransientComponent.dirtyBlocks.size must be equalTo (1)
    }

    "Delete an element value" in {
      BlockTransientComponent.clear
      btccd.contents.add("test", "EmbeddedTransientElement")
      btccd.contents.delete("test")
      //      println("btccd.contents.size = "+btccd.contents.size)
      //      println("btccd.contents.sequence.position(null) = "+btccd.contents.sequence.position(null))
      //      println("BlockTransientComponent.writeLocks.size = "+BlockTransientComponent.writeLocks.size)
      //      println("BlockTransientComponent.dirtyBlocks.size = "+BlockTransientComponent.dirtyBlocks.size)
      btccd.contents.size must be equalTo (0)
      btccd.contents.sequence.current == null must be equalTo true
      BlockTransientComponent.writeLocks.size must be equalTo (1)
      BlockTransientComponent.dirtyBlocks.size must be equalTo (1)
    }

    "Return the current size" in {
      BlockTransientComponent.clear
      btccd.contents.size must be equalTo (0)
      btccd.contents.add("first", "EmbeddedTransientElement")
      btccd.contents.add("second", "EmbeddedTransientElement")
      btccd.contents.size must be equalTo (2)
      BlockTransientComponent.writeLocks.size must be equalTo (1)
      BlockTransientComponent.dirtyBlocks.size must be equalTo (1)
      var count = 0
      var it = btccd.contents.iterator
      while (it.hasNext) {
        it.next
        count += 1
      }
      count must be equalTo (2)
      btccd.contents.delete("first")
      btccd.contents.size must be equalTo (1)
      count = 0
      it = btccd.contents.iterator
      while (it.hasNext) {
        count += 1
        it.next
      }
      count must be equalTo (1)
    }

    "Root Split Test" in {
      BlockTransientComponent.clear
      val n = 4
      BTreeTransientContainerComponent.root = n
      var i = 0
      while (i < n) {
        i += 1
        btccd.contents.add("" + i, "EmbeddedTransientElement")
      }
      btccd.contents.size must be equalTo (n)
      var count = 0
      var it = btccd.contents.iterator
      while (it.hasNext) {
        count += 1
        val key = it.next
        key must be equalTo ("" + count)
      }
      count must be equalTo (n)
      BlockTransientComponent.writeLocks.size must be equalTo (3)
      BlockTransientComponent.dirtyBlocks.size must be equalTo (3)
    }

    "Remove Test" in {
      BlockTransientComponent.clear
      val n = 4
      BTreeTransientContainerComponent.root = n
      var i = 0
      while (i < n) {
        i += 1
        btccd.contents.add("" + i, "EmbeddedTransientElement")
      }
      btccd.contents.size must be equalTo (n)
      var count = 0
      var it = btccd.contents.iterator
      while (it.hasNext) {
        count += 1
        val key = it.next
        key must be equalTo ("" + count)
      }
      count must be equalTo (n)
      BlockTransientComponent.writeLocks.size must be equalTo (3)
      BlockTransientComponent.dirtyBlocks.size must be equalTo (3)
      BlockTransientComponent.clear
      i = 0
      while (i < n) {
        i += 1
        btccd.contents.remove("" + i)
        btccd.contents.size must be equalTo (n - i)
        if (i < 2) {
          BlockTransientComponent.writeLocks.size must be equalTo (2)
          BlockTransientComponent.dirtyBlocks.size must be equalTo (2)
        } else {
          BlockTransientComponent.writeLocks.size must be equalTo (3)
          BlockTransientComponent.dirtyBlocks.size must be equalTo (3)
        }
        count = i
        it = btccd.contents.iterator
        while (it.hasNext) {
          count += 1
          val key = it.next
          key must be equalTo ("" + count)
        }
        count must be equalTo (n)
      }
    }

    "Reverse Remove Test" in {
      BlockTransientComponent.clear
      val n = 4
      BTreeTransientContainerComponent.root = n
      var i = 0
      while (i < n) {
        i += 1
        btccd.contents.add("" + i, "EmbeddedTransientElement")
      }
      btccd.contents.size must be equalTo (n)
      var count = 0
      var it = btccd.contents.iterator
      while (it.hasNext) {
        count += 1
        val key = it.next
        key must be equalTo ("" + count)
      }
      count must be equalTo (n)
      BlockTransientComponent.writeLocks.size must be equalTo (3)
      BlockTransientComponent.dirtyBlocks.size must be equalTo (3)
      BlockTransientComponent.clear
      i = 0
      while (i < n) {
        i += 1
        btccd.contents.remove("" + (n + 1 - i))
        btccd.contents.size must be equalTo (n - i)
        count = 0
        it = btccd.contents.iterator
        while (it.hasNext) {
          count += 1
          val key = it.next
          key must be equalTo ("" + count)
        }
        count + i must be equalTo (n)
      }
    }

    "PositionTo Test" in {
      BlockTransientComponent.clear
      BTreeTransientContainerComponent.root = 4
      btccd.contents.add("1", "EmbeddedTransientElement")
      btccd.contents.add("2", "EmbeddedTransientElement")
      btccd.contents.add("3", "EmbeddedTransientElement")
      btccd.contents.add("5", "EmbeddedTransientElement")
      btccd.contents.add("7", "EmbeddedTransientElement")
      btccd.contents.size must be equalTo (5)
      BlockTransientComponent.writeLocks.size must be equalTo (3)
      BlockTransientComponent.dirtyBlocks.size must be equalTo (3)
      btccd.contents.remove("2")
      btccd.contents.size must be equalTo (4)
      val seq = btccd.contents.sequence
      //seq.current("6") mustNot be equalTo null
      seq.current("6") must be equalTo "7"
      seq.current must be equalTo "7"
    }

  }
}
