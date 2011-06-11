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
package util
package sequence
package basic

import java.util.List


/**
 * Wraps a SequenceSource around a List of Strings.
 * Note that null values, and duplicates are not allowed in the wrapped list.
 * @param list The List being wrapped.
 */
class ListSequence(list: List[String], reverse: Boolean) extends SequenceSource {
  require(list != null, "The wrapped list cannot be null")
  require(!list.contains(null), "The list cannot contain null value")
  require(!hasDuplicates, "The list cannot contain duplicates")

  def this(list: List[String]) = this (list, false)

  override def isReverse = reverse

  private var ndx = if (isReverse && list.size > 0) list.size - 1 else 0

  private def hasDuplicates: Boolean = {
    var chk = false
    var it = list.iterator
    while (!chk && it.hasNext) {
      val key = it.next
      if (list.indexOf(key) != list.lastIndexOf(key))
        chk = true
    }
    chk
  }

  override def current = super.current

  override def current(key: String): String = {
    if (key != null) {
      ndx = if (list contains key)
        list indexOf key
      else ndx
    }
    if (key != null && !list.contains(key))
      null
    else if (-1 < ndx && ndx < list.size)
      list get ndx
    else null
  }

  override def next(key: String): String = {
    if (key != null) {
      ndx = if (list contains key)
        if (isReverse)
          (list indexOf key) - 1
        else (list indexOf key) + 1
      else ndx
    }
    if (key != null && !list.contains(key)) {
      null
    }
    else if (-1 < ndx && ndx < list.size) {
      val rv = list get ndx
      rv
    }
    else {
      null
    }
  }
}
