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

object RecordLock {
  def apply(batch: IncDes, recordKey: String) = {
    val jef = new RecordLockFactory
    jef.configure(batch.systemServices)
    val je = jef.newActor(null).asInstanceOf[IncDes]
    je.setSystemServices(batch.systemServices)
    val chain = new Chain
    chain.op(je, Set(null, recordKey))
    chain.op(batch.systemServices, TransactionRequest(je), "lockTimestamp")
    chain.op(batch, Unit => ValidateTimestamp(recordKey, chain("lockTimestamp").asInstanceOf[Long]))
    chain
  }
}

class RecordLockFactory
  extends IncDesStringFactory(DBT_RECORD_LOCK) {
  override protected def instantiate = {
    val req = super.instantiate
    addComponent(new QueryRequestComponent(req))
    addComponent(new RecordLockComponent(req))
    req
  }
}

class RecordLockComponent(actor: Actor)
  extends Component(actor) {
  bindSafe(classOf[Process], new ChainFactory(process))

  private def process(msg: AnyRef, chain: Chain) {
    chain.op(actor, Value(), "recordKey")
    chain.op(systemServices, Unit => GetRecord(chain("recordKey").asInstanceOf[String]), "record")
    chain.op(Unit => chain("record"), GetTimestamp())
  }
}
