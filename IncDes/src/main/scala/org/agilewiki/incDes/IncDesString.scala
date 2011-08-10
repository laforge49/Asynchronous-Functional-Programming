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

object SubordinateStringFactory
  extends IncDesStringFactory {
  include(SubordinateComponentFactory())
}

class IncDesStringFactory
  extends IncDesKeyFactory[String](INC_DES_STRING_FACTORY_ID) {

  override protected def instantiate = new IncDesString

  override def read(data: MutableData) = {
    data.readString
  }

  override def write(data: MutableData, k: String) {
    data.writeString(k)
  }

  override def length(k: String) = IncDes.stringLength(k)
}

object IncDesString {
  def apply(mailbox: Mailbox) = {
    SubordinateStringFactory.newActor(mailbox).asInstanceOf[IncDesString]
  }
}

class IncDesString extends IncDesItem {
  private var i: String = null
  private var len = -1

  override def value(msg: AnyRef, rf: Any => Unit) {
    if (dser) {
      rf(i)
      return
    }
    if (!isSerialized) throw new IllegalStateException
    val m = data.mutable
    m.skip(IncDes.intLength)
    i = m.readString(len)
    dser = true
    rf(i)
  }

  override def set(msg: AnyRef, rf: Any => Unit) {
    val s = msg.asInstanceOf[Set]
    val v = s.value.asInstanceOf[String]
    val tc = s.transactionContext
    value(Value(), {
      rsp: Any => {
        if (i == v) {
          rf(null)
          return
        }
        this(Writable(tc)) {
          rsp1 => {
            val ol = IncDes.stringLength(i)
            i = v
            dser = true
            change(tc, IncDes.stringLength(i) - ol, this, rf)
          }
        }
      }
    })
  }

  override def length = if (dser) IncDes.stringLength(i) else IncDes.stringLength(len)

  override protected def serialize(_data: MutableData) {
    if (!dser) throw new IllegalStateException
    _data.writeString(i)
  }

  override def load(_data: MutableData) {
    super.load(_data)
    len = _data.readInt
    if (len > 0) _data.skip(len * 2)
    dser = false
  }
}
