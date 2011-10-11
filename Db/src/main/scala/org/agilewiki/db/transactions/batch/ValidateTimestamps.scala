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
package db
package transactions
package batch

import blip._
import incDes._
import blocks._
import seq._

object ValidateTimestamps {
  def apply(batch: Actor) = {
    val jef = new ValidateTimestampsFactory
    jef.configure(batch.systemServices)
    val je = jef.newActor(null).asInstanceOf[IncDes]
    je.setSystemServices(batch.systemServices)
    je
  }
}

class ValidateTimestampsFactory
  extends IncDesStringLongMapFactory(DBT_VALIDATE_TIMESTAMPS) {
  override protected def instantiate = {
    val req = super.instantiate
    addComponent(new ValidateTimestampsComponent(req))
    req
  }
}

class ValidateTimestampsComponent(actor: Actor)
  extends Component(actor) {
  bind(classOf[Process], process)
  bind(classOf[ValidateTimestamp], validateTimestamp)

  private def process(msg: AnyRef, rf: Any => Unit) {
    actor(ValuesSeq()) {
      rsp1 => {
        val seq = rsp1.asInstanceOf[Sequence[String, IncDesLong]]
        seq(LoopSafe(ValidateSafe()))(rf)
      }
    }
  }

  private def validateTimestamp(msg: AnyRef, rf: Any => Unit) {
    val req = msg.asInstanceOf[ValidateTimestamp]
    val recordKey = req.recordKey
    val timestamp = req.timestamp
    actor(GetValue(recordKey)) {
      rsp => {
        val ts = rsp.asInstanceOf[Long]
        if (ts == 0) {
          val idL = IncDesLong(null)
          actor(Put[String, IncDesLong, Long](null, recordKey, idL)) {
            rsp => idL(Set(null, timestamp))(rf)
          }
        } else {
          if (timestamp != ts) throw new IllegalStateException
          rf(null)
        }
      }
    }
  }
}

case class ValidateSafe() extends Safe {
  override def func(target: Actor, msg: AnyRef, rf: Any => Unit)(implicit sender: ActiveActor) {
    val kvPair = msg.asInstanceOf[KVPair[String, Long]]
    val key = kvPair.key
    val ts1 = kvPair.value
    val chain = new Chain
    chain.op(target.systemServices, GetRecord(key), "record")
    chain.op(Unit => chain("record"), GetTimestamp())
    target(chain) {
      rsp => {
        val ts2 = rsp.asInstanceOf[Long]
        if (ts1 != ts2) throw new IllegalStateException
        rf(true)
      }
    }
  }
}
