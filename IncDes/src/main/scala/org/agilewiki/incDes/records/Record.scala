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
package records

import blip._
import services._

class RecordFactory(id: FactoryId)
  extends IncDesFactory(id) {
  override protected def instantiate = new Record
}

object Record {
  def apply(mailbox: Mailbox) = {
    new RecordFactory(INC_DES_RECORD_FACTORY_ID).newActor(mailbox).asInstanceOf[Record]
  }
}

class Record extends IncDesIncDes {
  private var ts = 0L

  bind(classOf[GetTimestamp], getTimestamp)
  bind(classOf[SetTimestamp], setTimestamp)

  override def length = if (len == -1) intLength + longLength else intLength + longLength + len

  override def load(_data: MutableData) {
    super.load(_data)
    _data.skip(longLength)
  }

  override protected def serialize(_data: MutableData) {
    if (!dser) throw new IllegalStateException
    saveLen(_data)
    _data.writeLong(ts)
    if (len < 0) return
    val incDesFactoryId = i.factoryId.value
    _data.writeString(incDesFactoryId)
    i.save(_data)
  }

  override protected def deserialize(rf: Any => Unit) {
    if (dser) {
      rf(null)
      return
    }
    if (!isSerialized) throw new IllegalStateException
    val m = data.mutable
    skipLen(m)
    ts = m.readLong
    if (len == -1) {
      dser = true
      rf(null)
      return
    }
    val incDesFactoryId = FactoryId(m.readString)
    systemServices(Instantiate(incDesFactoryId, mailbox)) {
      rsp => {
        i = rsp.asInstanceOf[IncDes]
        i.load(m)
        i.partness(this, key, this)
        dser = true
        rf(null)
      }
    }
  }

  override def set(msg: AnyRef, rf: Any => Unit) {
    if (!dser) {
      val m = data.mutable
      skipLen(m)
      ts = m.readLong
    }
    super.set(msg, rf)
  }

  def getTimestamp(msg: AnyRef, rf: Any => Unit) {
    deserialize {
      rsp => rf(ts)
    }
  }

  def setTimestamp(msg: AnyRef, rf: Any => Unit) {
    val req = msg.asInstanceOf[SetTimestamp]
    val timestamp = req.timestamp
    val tc = req.transactionContext
    deserialize {
      rsp => {
        writable(tc) {
          rsp => {
            ts = timestamp
            changed(tc, 0, this, rf)
          }
        }
      }
    }
  }
}