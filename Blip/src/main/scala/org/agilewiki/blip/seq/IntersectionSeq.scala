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

import annotation.tailrec

class IntersectionSeq[K, V](seqList: java.util.List[Sequence[K, V]])
  extends Sequence[K, java.util.List[V]] {

  if (seqList.isEmpty) throw new IllegalArgumentException("at least one sequence is required")

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
    val seqs = new java.util.ArrayList[Cursor[K, V]]
    val sit = seqList.iterator
    while (sit.hasNext) {
      val cursor = new Cursor(sit.next)
      cursor.setExchangeMessenger(exchangeMessenger)
      seqs.add(cursor)
    }
    val processed = new java.util.ArrayList[Cursor[K, V]]
    val it = seqs.iterator
    while (it.hasNext) {
      val seq = it.next
      val rsps = new java.util.ArrayList[(Cursor[K, V], Any)]
      seq(msg) {
        rsp => {
          rsps.add((seq, rsp))
          _process(rsps, seqs, processed, rf)
        }
      }
    }
  }

  def aprocess(msgs: java.util.List[(Cursor[K, V], Any)],
               seqs: java.util.ArrayList[Cursor[K, V]],
               processed: java.util.List[Cursor[K, V]],
               rf: KVPair[K, java.util.List[V]] => Unit) {
    _process(msgs, seqs, processed, rf)
  }

  @tailrec
  private def _process(msgs: java.util.List[(Cursor[K, V], Any)],
                       seqs: java.util.ArrayList[Cursor[K, V]],
                       processed: java.util.List[Cursor[K, V]],
                       rf: KVPair[K, java.util.List[V]] => Unit) {
    var async = false
    val rsps = new java.util.ArrayList[(Cursor[K, V], Any)]
    val mit = msgs.iterator
    while (mit.hasNext) {
      val (seq, msg) = mit.next
      if (msg == null) {
        if (processed.isEmpty) {
          processed.add(null)
          rf(null)
          return
        }
        val aseq = processed.get(0)
        if (aseq == null) return
        processed.clear
        processed.add(null)
        rf(null)
        return
      }
      if (processed.isEmpty) {
        processed.add(seq)
        if (processed.size == seqs.size) fin(seqs, rf)
        return
      }
      val aseq = processed.get(0)
      val c = seq.compareTo(aseq)
      if (c == 0) {
        processed.add(seq)
        if (processed.size == seqs.size) {
          fin(seqs, rf)
          return
        }
      } else if (c < 0) {
        seq(Current(aseq.lastKVPair.key)) {
          rsp => {
            if (async) {
              val rs = new java.util.ArrayList[(Cursor[K, V], Any)]
              rs.add((seq, rsp))
              aprocess(rs, seqs, processed, rf)
            } else rsps.add((seq, rsp))
          }
        }
      } else {
        val old = new java.util.ArrayList[Cursor[K, V]](processed)
        processed.clear
        processed.add(seq)
        val current = Current(seq.lastKVPair.key)
        val it = old.iterator
        while (it.hasNext) {
          val s = it.next
          s(current) {
            rsp => {
              if (async) {
                val rs = new java.util.ArrayList[(Cursor[K, V], Any)]
                rs.add((s, rsp))
                aprocess(rs, seqs, processed, rf)
              } else rsps.add((s, rsp))
            }
          }
        }
      }
    }
    async = true
    if (!rsps.isEmpty) _process(rsps, seqs, processed, rf)
  }

  def fin(seqs: java.util.ArrayList[Cursor[K, V]],
          rf: KVPair[K, java.util.List[V]] => Unit) {
    val key = seqs.get(0).lastKVPair.key
    val list = new java.util.ArrayList[V]
    val it = seqs.iterator
    while (it.hasNext) {
      val seq = it.next
      list.add(seq.lastKVPair.value)
    }
    rf(new KVPair(key, list))
  }

  override protected def _comparator = seqList.get(0).comparator
}
