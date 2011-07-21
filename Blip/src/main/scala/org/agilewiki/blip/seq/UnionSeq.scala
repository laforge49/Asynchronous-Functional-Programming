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

class UnionSeq[K, V](mailbox: Mailbox, seqList: java.util.List[Sequence[K, V]])
  extends Sequence[K, java.util.List[V]](mailbox, null) {

  def first(msg: AnyRef, rf: Any => Unit) {
    process(msg, rf)
  }

  def current(msg: AnyRef, rf: Any => Unit) {
    process(msg, rf)
  }

  def next(msg: AnyRef, rf: Any => Unit) {
    process(msg, rf)
  }

  def process(msg: AnyRef, rf: KVPair[K, java.util.List[V]] => Unit) {
    if (seqList.isEmpty) {
      rf(null)
      return
    }
    var count = seqList.size
    var k: K = null.asInstanceOf[K]
    var first = true
    val vs = new java.util.ArrayList[V]
    val it = seqList.iterator
    while (it.hasNext) {
      val seq = it.next
      seq(msg) {
        rsp => {
          count -= 1
          if (rsp == null) {
            if (count == 0)
              if (first) rf(null)
              else rf(new KVPair(k, vs))
          } else {
            val kv = rsp.asInstanceOf[KVPair[K, V]]
            if (first) {
              first = false
              k = kv.key
              vs.add(kv.value)
              if (count == 0) rf(new KVPair(k, vs))
            } else {
              val c = comparator.compare(k, kv.key)
              if (c < 0) {
                if (count == 0) rf(new KVPair(k, vs))
              } else if (c == 0) {
                vs.add(kv.value)
                if (count == 0) rf(new KVPair(k, vs))
              } else {
                k = kv.key
                vs.clear
                vs.add(kv.value)
                if (count == 0) rf(new KVPair(k, vs))
              }
            }
          }
        }
      }
    }
  }

  override protected def _comparator = seqList.get(0).comparator
}
