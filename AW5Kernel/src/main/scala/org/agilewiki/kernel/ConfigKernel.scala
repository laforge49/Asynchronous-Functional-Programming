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
import element.operation.{ElementConfig, Deleting, Initialize}
import java.util.Properties

import jits._
import kernel.operation.{Eval, Config}
import util.jit.{JitNamedNakedJitTreeMap, JitConfig}
import util.jit.structure.JitElement
import util.UtilNames

/**
 * Configures the rolon and journalEntry roles with the operations defined in the kernel package.
 */
object ConfigKernel {
  val NAME = "Kernel"
  val VERSION = "1.0"

  def apply(properties: Properties) = {
    if (properties.get(NAME) == null) {
      properties.put(NAME, VERSION)
      init(properties)
    }
    VERSION
  }

  private def init(properties: Properties) {
    new JitConfig(properties) {
      role(EMPTY_JIT_ELEMENT_ROLE_NAME)
      jitClass(classOf[JitElement] getName)

      role(INODE_LINK_ELEMENT_ROLE_NAME)
      jitClass(classOf[INodeLinkElement] getName)

      role(KERNEL_BLOCK_HANDLE_ELEMENT_ROLE_NAME)
      jitClass(classOf[KernelBlockHandleElement] getName)

      role(KERNEL_BLOCK_MANAGEMENT_ELEMENT_ROLE_NAME)
      jitClass(classOf[JitElement] getName)

      role(KERNEL_INODE_HANDLE_ELEMENT_ROLE_NAME)
      jitClass(classOf[KernelINodeHandleElement] getName)

      role(NAKED_EMBEDDED_ELEMENT_ROLE_NAME)
      jitClass(classOf[JitNamedNakedJitTreeMap] getName)
      jitSubRole(EMBEDDED_ELEMENT_ROLE_NAME)

      role(NAKED_EMPTY_JIT_ELEMENT_MAP_ROLE_NAME)
      jitClass(classOf[JitNamedNakedJitTreeMap] getName)
      jitSubRole(EMPTY_JIT_ELEMENT_ROLE_NAME)

      role(NAKED_INODE_HANDLE_MAP_ROLE_NAME)
      jitClass(classOf[JitNamedNakedJitTreeMap] getName)
      jitSubRole(INODE_LINK_ELEMENT_ROLE_NAME)

      role(NAKED_KERNEL_BLOCK_MANAGEMENT_ELEMENT_MAP_ROLE_NAME)
      jitClass(classOf[JitNamedNakedJitTreeMap] getName)
      jitSubRole(KERNEL_BLOCK_MANAGEMENT_ELEMENT_ROLE_NAME)

      role(NAKED_KERNEL_BLOCK_HANDLE_MAP_ROLE_NAME)
      jitClass(classOf[JitNamedNakedJitTreeMap] getName)
      jitSubRole(KERNEL_BLOCK_HANDLE_ELEMENT_ROLE_NAME)

      role(NAKED_KERNEL_INODE_HANDLE_MAP_ROLE_NAME)
      jitClass(classOf[JitNamedNakedJitTreeMap] getName)
      jitSubRole(KERNEL_INODE_HANDLE_ELEMENT_ROLE_NAME)
    }

    new Config(properties) {
      role(UtilNames.PAGE_TYPE)
      rootElementType(ROLON_ROOT_ELEMENT_ROLE_NAME)

      role(KernelNames.CHANGE_TYPE)
      rootElementType(ROLON_ROOT_ELEMENT_ROLE_NAME)
      include(UtilNames.PAGE_TYPE)
      op(Eval())

      role(KernelNames.INITIALIZATION_CHANGE_TYPE)
      rootElementType(ROLON_ROOT_ELEMENT_ROLE_NAME)
      include(KernelNames.CHANGE_TYPE)
      op(new InitializationJE)

      role(KernelNames.HOME_TYPE)
      rootElementType(ROLON_ROOT_ELEMENT_ROLE_NAME)
      include(UtilNames.PAGE_TYPE)
    }

    new ElementConfig(properties) {
      role(ELEMENT_ROLE_NAME)
      kernelElementType(classOf[Element] getName)
      op(Deleting())

      role(BTREE_ELEMENT_ROLE_NAME)
      kernelElementType(classOf[BTreeElement] getName)
      include(ELEMENT_ROLE_NAME)

      role(BYTES_ELEMENT_ROLE_NAME)
      kernelElementType(classOf[BytesElement] getName)
      include(EMBEDDED_ELEMENT_ROLE_NAME)

      role(DOCUMENT_ELEMENT_ROLE_NAME)
      kernelElementType(classOf[DocumentElement] getName)
      include(BTREE_ELEMENT_ROLE_NAME)

      role(EMBEDDED_ELEMENT_ROLE_NAME)
      kernelElementType(classOf[EmbeddedElement] getName)
      include(ELEMENT_ROLE_NAME)

      role(EMBEDDED_ELEMENT_TREE_MAP_ROLE_NAME)
      kernelElementType(classOf[EmbddedElementTreeMapElement] getName)
      include(ELEMENT_ROLE_NAME)

      role(INODE_ELEMENT_ROLE_NAME)
      kernelElementType(classOf[INodeElement] getName)
      include(ELEMENT_ROLE_NAME)

      role(KERNEL_ADDRESS_MAP_ELEMENT_ROLE_NAME)
      kernelElementType(classOf[KernelAddressMapElement] getName)
      include(ELEMENT_ROLE_NAME)
      jitSubRole(KERNEL_BLOCK_HANDLE_ELEMENT_ROLE_NAME)
      inode(KERNEL_BLOCK_HANDLE_ELEMENT_INODE_ROLE_NAME)
      map(NAKED_KERNEL_BLOCK_HANDLE_MAP_ROLE_NAME)

      role(KERNEL_BLOCK_HANDLE_ELEMENT_INODE_ROLE_NAME)
      kernelElementType(classOf[KernelHomogeniousINodeElement] getName)
      include(ELEMENT_ROLE_NAME)
      jitSubRole(KERNEL_BLOCK_HANDLE_ELEMENT_ROLE_NAME)
      inode(KERNEL_BLOCK_HANDLE_ELEMENT_INODE_ROLE_NAME)
      map(NAKED_KERNEL_BLOCK_HANDLE_MAP_ROLE_NAME)

      role(KERNEL_DISK_BLOCK_MANAGER1_ELEMENT_ROLE_NAME)
      kernelElementType(classOf[KernelDiskBlockManager1Element] getName)
      include(ELEMENT_ROLE_NAME)

      role(KERNEL_DISK_BLOCK_MANAGER2_ELEMENT_ROLE_NAME)
      kernelElementType(classOf[KernelDiskBlockManager2Element] getName)
      include(ELEMENT_ROLE_NAME)

      role(KERNEL_DISK_BLOCK_MANAGER3_ELEMENT_ROLE_NAME)
      kernelElementType(classOf[KernelDiskBlockManager3Element] getName)
      include(ELEMENT_ROLE_NAME)

      role(KERNEL_EMPTY_JIT_ELEMENTS_INODE_ELEMENT_ROLE_NAME)
      kernelElementType(classOf[KernelHomogeniousINodeElement] getName)
      include(ELEMENT_ROLE_NAME)
      jitSubRole(EMPTY_JIT_ELEMENT_ROLE_NAME)
      inode(KERNEL_EMPTY_JIT_ELEMENTS_INODE_ELEMENT_ROLE_NAME)
      map(NAKED_EMPTY_JIT_ELEMENT_MAP_ROLE_NAME)

      role(KERNEL_DISK_BLOCK_MANAGEMENT_ELEMENT_INODE1_ELEMENT_ROLE_NAME)
      kernelElementType(classOf[KernelDiskBlockManagementElementINode1Element] getName)
      include(ELEMENT_ROLE_NAME)

      role(KERNEL_DISK_BLOCK_MANAGEMENT_ELEMENT_INODE2_ELEMENT_ROLE_NAME)
      kernelElementType(classOf[KernelDiskBlockManagementElementINode2Element] getName)
      include(ELEMENT_ROLE_NAME)

      role(KERNEL_INODE1_ELEMENT_ROLE_NAME)
      kernelElementType(classOf[KernelINode1Element] getName)
      include(ELEMENT_ROLE_NAME)

      role(KERNEL_INODE2_ELEMENT_ROLE_NAME)
      kernelElementType(classOf[KernelINode2Element] getName)
      include(ELEMENT_ROLE_NAME)

      role(KERNEL_INODE_ELEMENT_ROLE_NAME)
      kernelElementType(classOf[KernelINodeElement] getName)
      include(ELEMENT_ROLE_NAME)

      role(KERNEL_PENDING_SEQUENSOR_ROLE_NAME)
      kernelElementType(classOf[KernelPendingSequensor] getName)
      include(ELEMENT_ROLE_NAME)

      role(KERNEL_PENDING_SEQUENSORS_ROLE_NAME)
      kernelElementType(classOf[KernelPendingSequensors] getName)
      include(ELEMENT_ROLE_NAME)

      role(KERNEL_ROOT_ELEMENT_ROLE_NAME)
      kernelElementType(classOf[KernelRootElement] getName)
      include(ELEMENT_ROLE_NAME)

      role(KERNEL_TIMESTAMP_ELEMENT_ROLE_NAME)
      kernelElementType(classOf[KernelTimestampsElement] getName)
      include(ELEMENT_ROLE_NAME)
      jitSubRole (EMPTY_JIT_ELEMENT_ROLE_NAME)
      inode(KERNEL_EMPTY_JIT_ELEMENTS_INODE_ELEMENT_ROLE_NAME)
      map(NAKED_EMPTY_JIT_ELEMENT_MAP_ROLE_NAME)

      role(ORDERED_ELEMENT_ROLE_NAME)
      kernelElementType(classOf[OrderedElement] getName)
      include(ELEMENT_ROLE_NAME)

      role(ROLON_ROOT_ELEMENT_ROLE_NAME)
      kernelElementType(classOf[RolonRootElement] getName)
      include(ELEMENT_ROLE_NAME)
      op(Initialize())

      role(TREE_MAP_ELEMENT)
      kernelElementType(classOf[TreeMapElement] getName)
      include(ELEMENT_ROLE_NAME)
    }
  }
}
