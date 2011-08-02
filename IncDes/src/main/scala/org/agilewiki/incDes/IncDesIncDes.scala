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
import services._

class SubordinateIncDesFactory(id: FactoryId)
  extends SubordinateFactory(id) {
  override protected def instantiate = new IncDesIncDes
}

object IncDesIncDes {
  def apply(mailbox: Mailbox) = {
    new SubordinateIncDesFactory(null).newActor(mailbox).asInstanceOf[IncDesIncDes]
  }
}

class IncDesIncDes extends IncDesItem {
  private var i: IncDes = null
  private var len = -1

  override def partness(container: IncDes, _key: Any, visibleContainer: IncDes) {
    super.partness(container, _key, visibleContainer)
    if (i != null) i.partness(this, key, this)
  }

  override def length = if (i == null) intLength else intLength + len

  override def load(_data: MutableData) {
    super.load(_data)
    len = _data.readInt
    if (len > 0) _data.skip(len * 2)
    dser = false
  }

  override def change(transactionContext: TransactionContext, lenDiff: Int, what: IncDes, rf: Any => Unit) {
    len += lenDiff
    super.change(transactionContext, lenDiff, what, rf)
  }

  override protected def serialize(_data: MutableData) {
    if (!dser) throw new IllegalStateException
    _data.writeInt(len)
    if (len < 1) return
    val incDesFactoryId = i.factoryId.value
    _data.writeString(incDesFactoryId)
    i.save(_data)
  }

  override def value(msg: AnyRef, rf: Any => Unit) {
    if (dser) {
      rf(i)
      return
    }
    if (!isSerialized) throw new IllegalStateException
    val m = data.mutable
    m.skip(intLength)
    val incDesFactoryId = FactoryId(m.readString)
    systemServices(Instantiate(incDesFactoryId, mailbox)) {
      rsp => i = rsp.asInstanceOf[IncDes]
      dser = true
      rf(i)
    }
  }

  override def set(msg: AnyRef, rf: Any => Unit) {
    val s = msg.asInstanceOf[Set]
    val v = s.value.asInstanceOf[IncDes]
    val tc = s.transactionContext
    this(Writable(tc)) {
      rsp => {
        val olen = length
        i = v
        if (v == null) len = -1
        else len = stringLength(i.factoryId.value) + i.length
        if (container != null && i != null) i.partness(this, key, this)
        dser = true
        change(tc, length - olen, this, rf)
      }
    }
  }
}
