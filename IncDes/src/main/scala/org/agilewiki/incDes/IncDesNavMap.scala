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

class IncDesNavMap[K, V <: IncDes, V1]
  extends IncDesKeyedCollection[K, V, V1] {
  private var i = new java.util.TreeMap[K, V]
  private var len = 0
  private var navMapSeq: NavMapSeq[K, V] = null

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
      val it = i.keySet.iterator
      while (it.hasNext) {
        val key = it.next
        keyFactory.write(_data, key)
        val value = i.get(key)
        value.save(_data)
      }
    }
  }

  override def length = IncDes.intLength + len

  def deserialize {
    if (i != null) return
    i = new java.util.TreeMap[K, V]
    val m = data.mutable
    m.skip(IncDes.intLength)
    val limit = m.offset + len
    while (m.offset < limit) {
      val key = keyFactory.read(m)
      val value = newValue
      value.load(m)
      value.partness(this, key, this)
      i.put(key, value)
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

  def put(msg: AnyRef, rf: Any => Unit) {
    val s = msg.asInstanceOf[Put[K, V]]
    val tc = s.transactionContext
    val k = s.key
    val v = s.value
    preprocess(tc, v)
    this(Writable(tc)) {
      rsp => {
        val old = i.put(k, v)
        var ol = 0
        if (old != null) {
          old.clearContainer
          ol = old.length
        }
        v.partness(this, k, this)
        change(tc, keyFactory.length(k) + v.length - ol, this, {
          rsp1: Any => {
            rf(old)
          }
        })
      }
    }
  }

  override def makePut(msg: AnyRef, rf: Any => Unit) {
    val s = msg.asInstanceOf[MakePut[K]]
    val tc = s.transactionContext
    val k = s.key
    deserialize
    var v = i.get(k)
    if (v != null) {
      rf(v)
      return
    }
    this(Writable(tc)) {
      r1 => {
        v = newValue
        i.put(k, v)
        v.partness(this, k, this)
        change(tc, keyFactory.length(k) + v.length, this, {
          r2: Any => {
            rf(v)
          }
        })
      }
    }
  }

  override def makePutSet(msg: AnyRef, rf: Any => Unit) {
    val s = msg.asInstanceOf[MakePutSet[K, V1]]
    val tc = s.transactionContext
    val k = s.key
    val v = s.value
    this(MakePut(tc, k)) {
      r1 => {
        val item = r1.asInstanceOf[V]
        item(Set(tc, v))(rf)
      }
    }
  }

  override def makePutMakeSet(msg: AnyRef, rf: Any => Unit) {
    val s = msg.asInstanceOf[MakePutMakeSet[K]]
    val tc = s.transactionContext
    val k = s.key
    val fid = s.factoryId
    this(MakePut(tc, k)) {
      r1 => {
        val incDesIncDes = r1.asInstanceOf[V]
        incDesIncDes(MakeSet(tc, fid))(rf)
      }
    }
  }

  override def get(msg: AnyRef, rf: Any => Unit) {
    deserialize
    val key = msg.asInstanceOf[Get[K]].key
    rf(i.get(key))
  }

  override def getValue(msg: AnyRef, rf: Any => Unit) {
    deserialize
    val key = msg.asInstanceOf[GetValue[K]].key
    val item = i.get(key)
    if (item == null) {
      rf(null)
      return
    }
    item(Value())(rf)
  }

  override def containsKey(msg: AnyRef, rf: Any => Unit) {
    deserialize
    val key = msg.asInstanceOf[ContainsKey[K]].key
    rf(i.containsKey(key))
  }

  override def size(msg: AnyRef, rf: Any => Unit) {
    deserialize
    rf(i.size)
  }

  override def remove(msg: AnyRef, rf: Any => Unit) {
    deserialize
    val s = msg.asInstanceOf[Remove[K]]
    val key = s.key
    if (!i.containsKey(key)) {
      rf(null)
      return
    }
    val tc = s.transactionContext
    this(Writable(tc)) {
      rsp => {
        val r = i.remove(key)
        val l = r.length
        r.clearContainer
        change(tc, -keyFactory.length(key) - l, this, {
          rsp => rf(r)
        })
      }
    }
  }

  override def seq(msg: AnyRef, rf: Any => Unit) {
    if (navMapSeq != null) {
      rf(navMapSeq)
      return
    }
    deserialize
    navMapSeq = new NavMapSeq[K, V](i)
    rf(navMapSeq)
  }
}