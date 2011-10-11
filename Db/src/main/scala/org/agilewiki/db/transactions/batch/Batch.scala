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
import seq._
import incDes._
import blocks._

object Batch {
  def apply(db: Actor) = {
    val jef = new BatchFactory
    jef.configure(db)
    val je = jef.newActor(null).asInstanceOf[IncDes]
    je.setSystemServices(db.systemServices)
    je
  }
}

class BatchFactory
  extends IncDesIncDesListFactory(DBT_BATCH) {
  override protected def instantiate = {
    val req = super.instantiate
    addComponent(new UpdateRequestComponent(req))
    addComponent(new BatchComponent(req))
    req
  }
}

class BatchComponent(actor: Actor)
  extends Component(actor) {
  private var validateTimestamps: IncDes = null

  bind(classOf[Process], process)
  bind(classOf[BatchItem], batchItem)
  bind(classOf[ValidateTimestamp], validateTimestamp)

  private def process(msg: AnyRef, rf: Any => Unit) {
    val tc = msg.asInstanceOf[Process].transactionContext
    actor(ValuesSeq()) {
      rsp1 => {
        val seq = rsp1.asInstanceOf[Sequence[IncDes, IncDes]]
        seq(LoopSafe(new BatchSafe(tc)))(rf)
      }
    }
  }

  private def validateTimestamp(msg: AnyRef, rf: Any => Unit) {
    if (validateTimestamps == null) {
      validateTimestamps = ValidateTimestamps(actor)
      _batchItem(BatchItem(validateTimestamps), {
        rsp => validateTimestamps(msg)(rf)
      })
    } else validateTimestamps(msg)(rf)
  }

  private def batchItem(msg: AnyRef, rf: Any => Unit) {
    if (validateTimestamps == null) {
      validateTimestamps = ValidateTimestamps(actor)
      _batchItem(BatchItem(validateTimestamps), {
        rsp => _batchItem(msg, rf)
      })
    } else _batchItem(msg, rf)
  }

  private def _batchItem(msg: AnyRef, rf: Any => Unit) {
    val req = msg.asInstanceOf[BatchItem]
    val batchItem = req.batchItem
    val incDesIncDes = IncDesIncDes(null)
    val chain = new Chain
    chain.op(actor, Add[IncDesIncDes, IncDes](null, incDesIncDes))
    chain.op(incDesIncDes, Set(null, batchItem))
    actor(chain)(rf)
  }
}

class BatchSafe(tc: TransactionContext) extends Safe {
  override def func(target: Actor, msg: AnyRef, rf: Any => Unit)(implicit sender: ActiveActor) {
    val kvPair = msg.asInstanceOf[KVPair[Int, IncDes]]
    kvPair.value(Process(tc)) {
      rsp => rf(true)
    }
  }
}
