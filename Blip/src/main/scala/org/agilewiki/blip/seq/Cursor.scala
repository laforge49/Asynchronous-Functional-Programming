/*
 * Copyright 2011 Bill La Forge
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
package blip
package seq

class Cursor[K, V](seq: Sequence[K, V])
  extends Sequence[K, V]
  with Comparable[Cursor[K, V]] {
  var lastKVPair: KVPair[K, V] = null
  var _first = false
  var inited = false

  def first(msg: AnyRef, rf: Any => Unit) {
    if (inited && _first) {
      rf(lastKVPair)
      return
    }
    seq(msg) {
      rsp => {
        inited = true
        _first = true
        lastKVPair = rsp.asInstanceOf[KVPair[K, V]]
        rf(rsp)
      }
    }
  }

  def current(msg: AnyRef, rf: Any => Unit) {
    val key = msg.asInstanceOf[Current[K]].key
    if (inited && lastKVPair != null && key == lastKVPair.key) {
      rf(lastKVPair)
      return
    }
    seq(msg) {
      rsp => {
        inited = true
        _first = false
        lastKVPair = rsp.asInstanceOf[KVPair[K, V]]
        rf(rsp)
      }
    }
  }

  def next(msg: AnyRef, rf: Any => Unit) {
    seq(msg) {
      rsp => {
        inited = true
        _first = false
        lastKVPair = rsp.asInstanceOf[KVPair[K, V]]
        rf(rsp)
      }
    }
  }

  override protected def _comparator = seq.comparator

  override def equals(o: Any): Boolean = {
    if (!o.isInstanceOf[Cursor[K, V]]) throw new IllegalArgumentException(o.toString)
    val other = o.asInstanceOf[Cursor[K, V]]
    compareTo(other) == 0
  }

  override def compareTo(o: Cursor[K, V]): Int = {
    if (!inited || !o.inited) throw new IllegalStateException
    if (lastKVPair == null) {
      if (o.lastKVPair == null) return 0
      return 1
    }
    if (o.lastKVPair == null) return -1
    return comparator.compare(lastKVPair.key, o.lastKVPair.key)
  }
}
