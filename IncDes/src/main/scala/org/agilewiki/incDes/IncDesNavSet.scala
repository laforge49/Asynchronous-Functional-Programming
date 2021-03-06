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

class IncDesNavSet[K]
  extends IncDesCollection[K] {
  private var i = new java.util.TreeSet[K]
  private var len = 0
  private var navSetSeq: NavSetSeq[K] = null

  bind(classOf[AddValue[K]], addValue)

  def keyFactory = factory.asInstanceOf[IncDesNavSetFactory[K]].keyFactory

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
        val key = it.next
        keyFactory.write(_data, key)
      }
    }
  }

  override def length = intLength + len

  def deserialize {
    if (i != null) return
    i = new java.util.TreeSet[K]
    val m = data.mutable
    m.skip(intLength)
    val limit = m.offset + len
    while (m.offset < limit) {
      val key = keyFactory.read(m)
      i.add(key)
    }
  }

  override def change(transactionContext: TransactionContext, lenDiff: Int, what: IncDes, rf: Any => Unit) {
    len += lenDiff
    changed(transactionContext, lenDiff, what, rf)
  }

  override def containsKey(msg: AnyRef, rf: Any => Unit) {
    deserialize
    val key = msg.asInstanceOf[ContainsKey[K]].key
    rf(i.contains(key))
  }

  override def size(msg: AnyRef, rf: Any => Unit) {
    deserialize
    rf(i.size)
  }

  override def resolve(msg: AnyRef, rf: Any => Unit) {
    val pathname = msg.asInstanceOf[Resolve].pathname
    if (pathname.length == 0) {
      rf((this, ""))
      return
    }
    if (pathname.startsWith("/"))
      throw new IllegalArgumentException("Unexpected pathname: " + pathname)
    val ndx = pathname.indexOf('/')
    if (ndx == -1) {
      rf((this, pathname))
      return
    }
    val newPathname = pathname.substring(ndx + 1)
    if (newPathname != "")
      throw new IllegalArgumentException("Unexpected pathname: " + pathname)
    var key = pathname.substring(0, ndx)
    val k = keyFactory.convert(key).asInstanceOf[K]
    val b = IncDesBoolean(null)
    val r = i.contains(k)
    b(Set(null, r)){
      rsp => {
        rf((b, ""))
      }
    }
  }

  def addValue(msg: AnyRef, rf: Any => Unit) {
    deserialize
    val s = msg.asInstanceOf[AddValue[K]]
    val k = s.value
    if (i.contains(k)) {
      rf(null)
      return
    }
    val tc = s.transactionContext
    writable(tc) {
      rsp => {
        i.add(k)
        change(tc, keyFactory.length(k), this, {
          rsp1: Any => {
            rf(null)
          }
        })
      }
    }
  }

  override def assign(msg: AnyRef, rf: Any => Unit) {
    val s = msg.asInstanceOf[Assign]
    val tc = s.transactionContext
    val k = keyFactory.convert(s.key).asInstanceOf[K]
    val v = s.value.asInstanceOf[IncDesBoolean]
    v(Value()) {
      rsp => {
        val b = rsp.asInstanceOf[Boolean]
        if (b) addValue(AddValue[K](tc, k), rf)
        else remove(Remove[K](tc, k), rf)
      }
    }
  }

  override def remove(msg: AnyRef, rf: Any => Unit) {
    deserialize
    val s = msg.asInstanceOf[Remove[K]]
    val k = s.key
    if (!i.contains(k)) {
      rf(null)
      return
    }
    val tc = s.transactionContext
    writable(tc) {
      rsp => {
        i.remove(k)
        change(tc, -keyFactory.length(k), this, {
          rsp => rf(k)
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
    navSetSeq = new NavSetSeq[K](i)
    navSetSeq.setExchangeMessenger(exchangeMessenger)
    navSetSeq.setSystemServices(systemServices)
    rf(navSetSeq)
  }
}
