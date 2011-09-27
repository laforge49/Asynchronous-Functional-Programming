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
import java.util.ArrayList

class IncDesList[V <: IncDesItem[V1], V1]
  extends IncDesValueCollection[Int, V, V1] {
  private var i = new ArrayList[V]
  private var len = 0
  private var listSeq: ListSeq[V] = null

  bind(classOf[Add[V, V1]], add)
  bind(classOf[Insert[V, V1]], insert)

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
    i = new ArrayList[V]
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
    val s = msg.asInstanceOf[Add[V, V1]]
    val tc = s.transactionContext
    val v = s.value
    preprocess(tc, v)
    writable(tc) {
      rsp => {
        i.add(v)
        v.partness(this, i.size - 1, this)
        change(tc, v.length, this, rf)
      }
    }
  }

  def insert(msg: AnyRef, rf: Any => Unit) {
    val s = msg.asInstanceOf[Insert[V, V1]]
    val tc = s.transactionContext
    val v = s.value
    preprocess(tc, v)
    val index = s.index
    if (index < 0 || index > i.size) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + i.size)
    writable(tc) {
      rsp => {
        i.add(index, v)
        v.partness(this, i.size - 1, this)
        seqOutdated
        change(tc, v.length, this, rf)
      }
    }
  }

  override def get(msg: AnyRef, rf: Any => Unit) {
    deserialize
    val key = msg.asInstanceOf[Get[Int]].key
    if (key < 0 || key >= i.size) rf(null)
    else {
      rf(i.get(key))
    }
  }

  override def resolve(msg: AnyRef, rf: Any => Unit) {
    val pathname = msg.asInstanceOf[Resolve].pathname
    if (pathname.length == 0) {
      rf((this, ""))
      return
    }
    if (pathname.startsWith("/"))
      throw new IllegalArgumentException("Unexpected pathname: " + pathname)
    val i = pathname.indexOf('/')
    if (i == -1) {
      rf((this, pathname))
      return
    }
    var newPathname = pathname.substring(i + 1)
    var key = pathname.substring(0, i)
    val k = key.toInt
    get(Get(k), {
      rsp => {
        if (rsp == null) {
          if (newPathname.length == 0) rf((null, ""))
          else throw new IllegalArgumentException("No match for pathname: " + pathname)
        } else {
          val incDes = rsp.asInstanceOf[IncDes]
          incDes(Resolve(newPathname))(rf)
        }
      }
    })
  }

  override def getValue(msg: AnyRef, rf: Any => Unit) {
    deserialize
    val key = msg.asInstanceOf[GetValue[Int]].key
    if (key < 0 || key >= i.size) {
      rf(null)
      return
    }
    val item = i.get(key)
    if (item == null) {
      rf(null)
      return
    }
    item(Value())(rf)
  }

  override def containsKey(msg: AnyRef, rf: Any => Unit) {
    deserialize
    val key = msg.asInstanceOf[ContainsKey[Int]].key
    rf(key >= 0 && key < i.size)
  }

  override def size(msg: AnyRef, rf: Any => Unit) {
    deserialize
    rf(i.size)
  }

  override def remove(msg: AnyRef, rf: Any => Unit) {
    deserialize
    val s = msg.asInstanceOf[Remove[Int]]
    val key = s.key
    if (key < 0 || key >= i.size) {
      rf(null)
      return
    }
    val tc = s.transactionContext
    writable(tc) {
      rsp => {
        val r = i.remove(key)
        val l = r.length
        r.clearContainer
        seqOutdated
        change(tc, -l, this, {
          rsp => rf(r)
        })
      }
    }
  }

  def seqOutdated {
    if (listSeq == null) return
    listSeq.outdated
    listSeq = null
  }

  override def seq(msg: AnyRef, rf: Any => Unit) {
    if (listSeq != null) {
      rf(listSeq)
      return
    }
    deserialize
    listSeq = new ListSeq[V](i)
    listSeq.setMailbox(mailbox)
    listSeq.setSystemServices(systemServices)
    rf(listSeq)
  }
}