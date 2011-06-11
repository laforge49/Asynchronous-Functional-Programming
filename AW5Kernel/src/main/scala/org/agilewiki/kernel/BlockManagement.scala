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

import java.util.LinkedHashSet

import org.agilewiki.kernel.element._BlockElement
import org.agilewiki.kernel.element.Element

private[kernel] trait BlockManagement {
  this: Element =>

  protected val _dirty = new LinkedHashSet[_BlockElement]()

  def allocate(size: Int): Long

  def release(offset: Long, size: Int)

  def addDirty(blockElement: _BlockElement) = {
    _dirty.remove(blockElement)
    _dirty.add(blockElement)
    true
  }

  def dirtyCount = _dirty.size

  private[kernel] def validate(map: DiskMap) {
    val it = _dirty.iterator
    while (it.hasNext) {
      val be = it.next
      if (be.deleted) {
        val handle = be.persistence.kernelHandleElement
        val off = handle.getBlockOffset
        val sz = handle.getBlockSize
        if (sz != 0) {
          map.add(off, sz)
        }
      }
    }
  }
}
