/*
 * Copyright 2010 M.Naji
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

package object kernel {

  val EMPTY_JIT_ELEMENT_ROLE_NAME = "EJ1"
  val INODE_LINK_ELEMENT_ROLE_NAME = "EJ2"
  val KERNEL_BLOCK_HANDLE_ELEMENT_ROLE_NAME = "EJ3"
  val KERNEL_BLOCK_MANAGEMENT_ELEMENT_ROLE_NAME = "EJ4"
  val KERNEL_INODE_HANDLE_ELEMENT_ROLE_NAME = "EJ5"
  val NAKED_EMBEDDED_ELEMENT_ROLE_NAME = "EJ6"
  val NAKED_EMPTY_JIT_ELEMENT_MAP_ROLE_NAME = "EJ7"
  val NAKED_INODE_HANDLE_MAP_ROLE_NAME = "EJ8"
  val NAKED_KERNEL_BLOCK_MANAGEMENT_ELEMENT_MAP_ROLE_NAME = "EJ9"
  val NAKED_KERNEL_BLOCK_HANDLE_MAP_ROLE_NAME = "EJ10"
  val NAKED_KERNEL_INODE_HANDLE_MAP_ROLE_NAME = "EJ11"

  val ELEMENT_ROLE_NAME = "E1"
  val BTREE_ELEMENT_ROLE_NAME = "E2"
  val BYTES_ELEMENT_ROLE_NAME = "E3"
  val DOCUMENT_ELEMENT_ROLE_NAME = "E4"
  val EMBEDDED_ELEMENT_ROLE_NAME = "E5"
  val EMBEDDED_ELEMENT_TREE_MAP_ROLE_NAME = "E6"
  val INODE_ELEMENT_ROLE_NAME = "E7"
  val KERNEL_ADDRESS_MAP_ELEMENT_ROLE_NAME = "E8"
  val KERNEL_BLOCK_HANDLE_ELEMENT_INODE_ROLE_NAME = "E9"
  val KERNEL_DISK_BLOCK_MANAGER1_ELEMENT_ROLE_NAME = "E10"
  val KERNEL_DISK_BLOCK_MANAGER2_ELEMENT_ROLE_NAME = "E11"
  val KERNEL_DISK_BLOCK_MANAGER3_ELEMENT_ROLE_NAME = "E12"
  val KERNEL_EMPTY_JIT_ELEMENTS_INODE_ELEMENT_ROLE_NAME = "E15"
  val KERNEL_DISK_BLOCK_MANAGEMENT_ELEMENT_INODE1_ELEMENT_ROLE_NAME = "E16"
  val KERNEL_DISK_BLOCK_MANAGEMENT_ELEMENT_INODE2_ELEMENT_ROLE_NAME = "E17"
  val KERNEL_INODE1_ELEMENT_ROLE_NAME = "E18"
  val KERNEL_INODE2_ELEMENT_ROLE_NAME = "E19"
  val KERNEL_INODE_ELEMENT_ROLE_NAME = "E20"
  val KERNEL_PENDING_SEQUENSOR_ROLE_NAME = "E21"
  val KERNEL_PENDING_SEQUENSORS_ROLE_NAME = "E22"
  val KERNEL_ROOT_ELEMENT_ROLE_NAME = "E23"
  val KERNEL_TIMESTAMP_ELEMENT_ROLE_NAME = "E24"
  val ORDERED_ELEMENT_ROLE_NAME = "E25"
  val ROLON_ROOT_ELEMENT_ROLE_NAME = "E26"
  val TREE_MAP_ELEMENT = "E27"

  val DATABASE_PATHNAME = "databasePathname"

  /**
   * The parameter name used to specify the access mode when opening a
   * database file.
   * <p>
   * Sample usage:
   * <pre>
   *   properties.put(DATABASE_ACCESS_RELATIONSHIP_MODE,"rw")
   * </pre>
   */
  val DATABASE_ACCESS_RELATIONSHIP_MODE = "databaseAccessMode" //r, rw, rws or rwd

  /**
   * Each database file begins with 2 fixed-size areas for a root block.
   * This is the parameter name used to specify the size of these areas.
   * Note that this parameter can not be changed once a database file has
   * been created, so a generous size should be used.
   * <p>
   * Sample usage:
   * <pre>
   *   properties.put(MAX_ROOT_BLOCK_SIZE_PARAMETER,""+100000)
   * </pre>
   */
  val MAX_ROOT_BLOCK_SIZE_PARAMETER = "maxRootBlockSize"

  /**
   * The maximum number of elements held by a b-tree root node
   */
  val MAX_BTREE_ROOT_SIZE_PARAMETER = "maxBTreeRootSize"

  /**
   * The maximum number of elements held by a b-tree leaf node
   */
  val MAX_BTREE_LEAF_SIZE_PARAMETER = "maxBTreeLeafSize"

  /**
   * The maximum number of elements held by a b-tree internal node
   */
  val MAX_BTREE_INODE_SIZE_PARAMETER = "maxBTreeINodeSize"

  val MAX_KERNEL_INODE_CACHE_SIZE_PARAMETER = "maxKernelINodeCacheSize"

  val JOURNAL_ENTRY_LOGGER_CLASS_PARAMETER = "JournalEntryLoggerClass"

  val MAX_QUERY_NAME_CACHE_SIZE_PARAMETER = "maxQueryNameCacheSize"

  val MAX_QUERY_CACHE_SIZE_PARAMETER = "maxQueryCacheSize"

  val MAX_COPY_CACHE_SIZE_PARAMETER = "maxCopyCacheSize"
}