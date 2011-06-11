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

import element.operation.{_Elements, ElementConfig}
import jits.{KernelBlockHandleElement, KernelINodeHandleElement}
import org.specs.SpecificationWithJUnit

import org.agilewiki.kernel.element.DiskBlockManagerTransientElement
import util.jit.structure.JitElement
import util.jit.{JitNamedNakedJitTreeMap, JitConfig, Jits}

/**
 * Specification for the _BTreeContainerComponent functionality
 */

class DiskBlockManagerTest extends SpecificationWithJUnit {
  val properties = _Elements.defaultConfiguration("Master")
  new JitConfig(properties) {
    role(EMPTY_JIT_ELEMENT_ROLE_NAME)
    jitClass(classOf[JitElement] getName)

    role(KERNEL_BLOCK_HANDLE_ELEMENT_ROLE_NAME)
    jitClass(classOf[KernelBlockHandleElement] getName)

    role(KERNEL_INODE_HANDLE_ELEMENT_ROLE_NAME)
    jitClass(classOf[KernelINodeHandleElement] getName)

    role(NAKED_KERNEL_INODE_HANDLE_MAP_ROLE_NAME)
    jitClass(classOf[JitNamedNakedJitTreeMap] getName)

    role(KERNEL_BLOCK_MANAGEMENT_ELEMENT_ROLE_NAME)
    jitClass(classOf[JitElement] getName)
  }
  new ElementConfig(properties) {
    role("DiskBlockManagerTransientElement")
    kernelElementType(classOf[DiskBlockManagerTransientElement] getName)
  }
  val systemContext = new _Elements(properties)

  val dbm = Jits(systemContext).createJit("DiskBlockManagerTransientElement").asInstanceOf[DiskBlockManagerTransientElement]

  "DiskBlockManager" should {

    "Be empty" in {
      dbm.contents.size must be equalTo (0)
      dbm.allocate(5)
      dbm.allocate(5)
      dbm.contents.size must be equalTo (0)
    }

    "hold 1 item" in {
      val a5 = dbm.allocate(5)
      dbm.extent must be equalTo (5)
      val a8 = dbm.allocate(8)
      dbm.extent must be equalTo (13)
      dbm.release(a8, 8)
      dbm.contents.size must be equalTo (1)
      dbm.recycle
      dbm.contents.size must be equalTo (2)
    }

    "combine with smaller" in {
      val a5 = dbm.allocate(5)
      dbm.extent must be equalTo (5)
      val a8 = dbm.allocate(8)
      dbm.extent must be equalTo (13)
      val a13 = dbm.allocate(13)
      dbm.extent must be equalTo (26)
      dbm.release(a8, 8)
      dbm.contents.size must be equalTo (1)
      dbm.recycle
      dbm.contents.size must be equalTo (2)
      dbm.release(a13, 13)
      dbm.contents.size must be equalTo (3)
      dbm.recycle
      dbm.contents.size must be equalTo (2)
    }

    "combine with larger" in {
      val a5 = dbm.allocate(5)
      dbm.extent must be equalTo (5)
      val a8 = dbm.allocate(8)
      dbm.extent must be equalTo (13)
      val a13 = dbm.allocate(13)
      dbm.extent must be equalTo (26)
      dbm.release(a13, 13)
      dbm.contents.size must be equalTo (1)
      dbm.recycle
      dbm.contents.size must be equalTo (2)
      dbm.release(a8, 8)
      dbm.contents.size must be equalTo (3)
      dbm.recycle
      dbm.contents.size must be equalTo (2)
    }

    "combine with smaller and larger" in {
      val a5 = dbm.allocate(5)
      dbm.extent must be equalTo (5)
      val a8 = dbm.allocate(8)
      dbm.extent must be equalTo (13)
      val a13 = dbm.allocate(13)
      dbm.extent must be equalTo (26)
      val a21 = dbm.allocate(21)
      dbm.extent must be equalTo (47)
      dbm.release(a8, 8)
      dbm.release(a21, 21)
      dbm.contents.size must be equalTo (2)
      dbm.recycle
      dbm.contents.size must be equalTo (4)
      dbm.release(a13, 13)
      dbm.contents.size must be equalTo (5)
      dbm.recycle
      dbm.contents.size must be equalTo (2)
    }

    "reallocate all" in {
      val a5 = dbm.allocate(5)
      dbm.extent must be equalTo (5)
      var a8 = dbm.allocate(8)
      dbm.extent must be equalTo (13)
      dbm.release(a8, 8)
      dbm.contents.size must be equalTo (1)
      dbm.recycle
      dbm.contents.size must be equalTo (2)
      a8 = dbm.allocate(8)
      dbm.extent must be equalTo (13)
      dbm.contents.size must be equalTo (0)
    }

    "reallocate partial" in {
      var a5 = dbm.allocate(5)
      dbm.extent must be equalTo (5)
      var a8 = dbm.allocate(8)
      dbm.extent must be equalTo (13)
      dbm.release(a8, 8)
      dbm.contents.size must be equalTo (1)
      dbm.recycle
      dbm.contents.size must be equalTo (2)
      a5 = dbm.allocate(5)
      dbm.extent must be equalTo (13)
      dbm.contents.size must be equalTo (2)
    }

    "getMore" in {
      val a5 = dbm.allocate(5)
      a5 must be equalTo (0)
      dbm.extent must be equalTo (5)
      dbm.contents.size must be equalTo (0)
      val a5_10 = dbm.getMore(10)
      a5_10._1 must be equalTo (5)
      a5_10._2 must be equalTo (10)
      dbm.extent must be equalTo (15)
      dbm.contents.size must be equalTo (0)
      var a8 = dbm.allocate(8)
      a8 must be equalTo (15)
      dbm.extent must be equalTo (23)
      dbm.contents.size must be equalTo (0)
      dbm.release(a8, 8)
      dbm.extent must be equalTo (23)
      dbm.contents.size must be equalTo (1)
      dbm.recycle
      dbm.extent must be equalTo (23)
      dbm.contents.size must be equalTo (2)
      val a15_8 = dbm.getMore(123)
      a15_8._1 must be equalTo (15)
      a15_8._2 must be equalTo (8)
      dbm.extent must be equalTo (23)
      dbm.contents.size must be equalTo (0)
    }

  }

}
