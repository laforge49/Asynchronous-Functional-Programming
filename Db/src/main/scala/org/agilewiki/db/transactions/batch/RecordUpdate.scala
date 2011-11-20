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

object RecordUpdate {
  def apply(batch: IncDes, recordKey: String, pathname: String, value: IncDes) = {
    val chain = new Chain
    var pn = pathname
    if (pn.startsWith("/")) pn = pn.substring(1)
    val i = pn.lastIndexOf("/")
    if (i != -1) chain.op(
      batch.systemServices,
      RecordExists(batch.systemServices, batch, recordKey, pn.substring(0, i)),
      "exists")
    val vidid = IncDesIncDes(null)
    val jef = new RecordUpdateFactory
    jef.configure(batch.systemServices)
    val je = jef.newActor(null).asInstanceOf[IncDes]
    je.setSystemServices(batch.systemServices)
    chain.op(Unit => {
      val exists = chain("exists").asInstanceOf[Boolean]
      if (i != -1 && !exists) throw new IllegalStateException("does not exist " + recordKey + " " + pathname)
      je
    }, PutString(null, "recordKey", recordKey))
    chain.op(je, PutString(null, "pathname", pn))
    chain.op(je, Put[String, IncDesIncDes, IncDes](null, "value", vidid))
    chain.op(value, Copy(null), "copy")
    chain.op(vidid, Unit => Set(null, chain("copy").asInstanceOf[IncDes]))
    chain.op(batch, BatchItem(je))
    chain
  }
}

class RecordUpdateFactory
  extends IncDesStringIncDesMapFactory(DBT_RECORD_UPDATE) {
  override protected def instantiate = {
    val req = super.instantiate
    addComponent(new RecordUpdateComponent(req))
    req
  }
}

class RecordUpdateComponent(actor: Actor)
  extends Component(actor) {
  bindMessageLogic(classOf[Process], new ChainFactory(process))

  private def process(msg: AnyRef, chain: Chain) {
    val tc = msg.asInstanceOf[Process].transactionContext.asInstanceOf[UpdateContext]
    val ts = tc.timestamp
    var key = ""
    chain.op(actor, GetValue2("recordKey"), "recordKey")
    chain.op(systemServices, Unit => MakeRecord(tc, chain("recordKey").asInstanceOf[String]), "record")
    chain.op(actor, GetValue2("pathname"), "pathname")
    chain.op(actor, GetValue("value"), "value")
    chain.op(Unit => chain("value"), Copy(null), "copy")
    chain.op(systemServices, RecordsPathname(), "recordsPathname")
    chain.op(
      Unit => {
        val record = chain("record")
        if (record == null) throw new IllegalArgumentException(
          "no such record: " + chain("recordKey").asInstanceOf[String])
        record
      },
      Unit => {
        val p = chain("pathname").asInstanceOf[String]
        Resolve(p)
      }, "tuple")
    chain.op(
      Unit => {
        val (incDes, k) = chain("tuple").asInstanceOf[(IncDes, String)]
        key = k
        incDes
      }, Unit => Assign(tc, key, chain("copy").asInstanceOf[IncDes]))
    chain.op(Unit => chain("record"), SetTimestamp(tc, ts))
  }
}
