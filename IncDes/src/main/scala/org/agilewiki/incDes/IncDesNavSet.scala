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
package incDes

import blip._
import seq._

class IncDesNavSet[V <: IncDes] extends IncDesCollection[V, V] {
  private var i = new java.util.TreeSet[V]
  private var len = 0
  private var navSetSeq: NavSetSeq[V] = null

  bind(classOf[Add[V]], add)

  override def isDeserialized = i != null

  override def load(_data: MutableData) {
    super.load(_data)
    len = _data.readInt
    if (len > 0) _data.skip(len)
    i = null
  }

  override protected def serialize(_data: MutableData) {
    if (i == null) throw new IllegalStateException
    else {
      _data.writeInt(len)
      val it = i.iterator
      while (it.hasNext) {
        val j = it.next
        j.save(_data)
      }
    }
  }

  override def length = intLength + len

  def deserialize {
    if (i != null) return
    i = new java.util.TreeSet[V]
    val m = data.mutable
    m.skip(intLength)
    val limit = m.offset + len
    while (m.offset < limit) {
      val sub = newValue
      sub.load(m)
      sub.partness(this, i.size - 1, this)
      i.add(sub)
    }
  }

  override def change(transactionContext: TransactionContext, lenDiff: Int, what: IncDes, rf: Any => Unit) {
    len += lenDiff
    changed(transactionContext, lenDiff, what, rf)
  }

  override def preprocess(tc: TransactionContext, v: V) {
    super.preprocess(tc, v)
    deserialize
  }

  def add(msg: AnyRef, rf: Any => Unit) {
    val s = msg.asInstanceOf[Add[V]]
    val tc = s.transactionContext
    val v = s.value
    preprocess(tc, v)
    this(Writable(tc)) {
      rsp => {
        i.add(v)
        change(tc, v.length, this, rf)
      }
    }
  }

  override def containsKey(msg: AnyRef, rf: Any => Unit) {
    deserialize
    val key = msg.asInstanceOf[ContainsKey[V]].key
    rf(i.contains(key))
  }

  override def size(msg: AnyRef, rf: Any => Unit) {
    deserialize
    rf(i.size)
  }

  override def remove(msg: AnyRef, rf: Any => Unit) {
    deserialize
    val s = msg.asInstanceOf[Remove[V]]
    val key = s.key
    if (!i.contains(key)) {
      rf(null)
      return
    }
    val tc = s.transactionContext
    this(Writable(tc)) {
      rsp => {
        val l = key.length
        i.remove(key)
        key.clearContainer
        change(tc, -l, this, {
          rsp => rf(key)
        })
      }
    }
  }

  override def seq(msg: AnyRef, rf: Any => Unit) {
    if (navSetSeq != null) {
      rf(navSetSeq)
      return
    }
    deserialize
    navSetSeq = new NavSetSeq[V](i)
    rf(navSetSeq)
  }
}