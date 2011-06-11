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

import java.util.TreeMap
import util._

/**
 * Organizes adjacent blocks into a single block.
 */
private[kernel] class DiskMap {
  private val map = new TreeMap[Long, Long]

  /**
   * returns the number of non-adjacent blocks.
   */
  def size = map.size

  /**
   * Prints a list of the non-adjacent blocks
   */
  def print {
    val it = map.keySet.iterator
    while (it.hasNext) {
      val limit = it.next
      val len = map.get(limit)
      val offset = limit - len
      println("  (" + offset + ", " + len + ", " + (offset + len) + ")")
    }
  }

  /**
   * Add a rolon's block to the collection.
   */
  def add(offset: Long, len: Int) {
    val l = Fibonacci(len)
    _add(offset, l)
  }

  /**
   * Add an unused block to the collection.
   */
  def _add(offset: Long, len: Long) {
    //    System.err.println("_add "+offset+", "+len+", "+(offset+len))
    var limit = offset + len
    if (map.containsKey(limit)) {
      throw new IllegalArgumentException("duplicate block: (" + offset + ", " + len + ")")
    }
    var _offset = offset
    var _len = len
    while (map.containsKey(_offset)) {
      _len += map.get(_offset)
      map.remove(_offset)
      _offset = limit - _len
    }
    val tailMap = map.tailMap(limit, false)
    if (tailMap.size > 0) {
      val next = tailMap.firstKey
      var l = tailMap.get(next)
      if (limit == next - l) {
        _len += l
        limit = next
        map.remove(next)
      }
    }
    map.put(limit, _len)
  }
}
