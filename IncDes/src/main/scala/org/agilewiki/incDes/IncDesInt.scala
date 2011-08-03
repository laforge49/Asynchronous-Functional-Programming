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

class SubordinateIntFactory(id: FactoryId)
  extends SubordinateFactory(id) {
  override protected def instantiate = new IncDesInt
}

object IncDesInt {
  def apply(mailbox: Mailbox) = {
    new SubordinateIntFactory(INC_DES_INT_FACTORY_ID).newActor(mailbox).asInstanceOf[IncDesInt]
  }
}

class IncDesInt extends IncDesItem {
  private var i = 0

  override def value(msg: AnyRef, rf: Any => Unit) {
    if (dser) {
      rf(i)
      return
    }
    if (!isSerialized) throw new IllegalStateException
    i = data.mutable.readInt
    dser = true
    rf(i)
  }

  override def set(msg: AnyRef, rf: Any => Unit) {
    val s = msg.asInstanceOf[Set]
    val v = s.value.asInstanceOf[Int]
    val tc = s.transactionContext
    value(Value(), {
      rsp: Any => {
        if (i == v) {
          rf(null)
          return
        }
        this(Writable(tc)) {
          rsp1 => {
            i = v
            dser = true
            change(tc, 0, this, rf)
          }
        }
      }
    })
  }

  override def length = intLength

  override protected def serialize(_data: MutableData) {
    if (!dser) throw new IllegalStateException
    _data.writeInt(i)
  }

  override def load(_data: MutableData) {
    super.load(_data)
    _data.skip(length)
    dser = false
  }
}
