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
    val rkid = IncDesString(null)
    val rkidid = IncDesIncDes(null)
    var pn = pathname
    if (pn.startsWith("/")) pn = pn.substring(1)
    val pnid = IncDesString(null)
    val pnidid = IncDesIncDes(null)
    val vidid = IncDesIncDes(null)
    val jef = new RecordUpdateFactory
    jef.configure(batch.systemServices)
    val je = jef.newActor(null).asInstanceOf[IncDes]
    je.setSystemServices(batch.systemServices)
    val chain = new Chain
    chain.op(je, Put[String, IncDesIncDes, IncDes](null, "recordKey", rkidid))
    chain.op(rkidid, Set(null, rkid))
    chain.op(rkid, Set(null, recordKey))
    chain.op(je, Put[String, IncDesIncDes, IncDes](null, "pathname", pnidid))
    chain.op(pnidid, Set(null, pnid))
    chain.op(pnid, Set(null, pn))
    chain.op(je, Put[String, IncDesIncDes, IncDes](null, "value", vidid))
    chain.op(value, Copy(null), "copy")
    chain.op(vidid, Unit => Set(null, chain("copy").asInstanceOf[IncDes]))
    chain.op(batch, BatchItem(je))
    chain
  }
}

class RecordUpdateFactory
  extends IncDesStringIncDesMapFactory(DBT_SET) {
  override protected def instantiate = {
    val req = super.instantiate
    addComponent(new UpdateRequestComponent(req))
    addComponent(new RecordUpdateComponent(req))
    req
  }
}

class RecordUpdateComponent(actor: Actor)
  extends Component(actor) {
  bindSafe(classOf[Process], new ChainFactory(process))

  private def process(msg: AnyRef, chain: Chain) {
    val tc = msg.asInstanceOf[Process].transactionContext.asInstanceOf[UpdateContext]
    val ts = tc.timestamp
    var key = ""
    chain.op(actor, GetValue("recordKey"), "recordKeyId")
    chain.op(Unit => chain("recordKeyId"), Value(), "recordKey")
    chain.op(actor, GetValue("pathname"), "pathnameId")
    chain.op(Unit => chain("pathnameId"), Value(), "pathname")
    chain.op(actor, GetValue("value"), "value")
    chain.op(Unit => chain("value"), Copy(null), "copy")
    chain.op(systemServices,
      Unit => GetRecord(chain("recordKey").asInstanceOf[String]), "record")
    chain.op(
      Unit => {
        val r = chain("record")
        if (r == null)
          throw new IllegalStateException("no such record: " + chain("recordKey").asInstanceOf[String])
        r
      },
      Unit => Resolve(chain("pathname").asInstanceOf[String]), "tuple")
    chain.op(Unit => {
      val (incDes, k) = chain("tuple").asInstanceOf[(IncDes, String)]
      key = k
      incDes
    }, Unit => Assign(tc, key, chain("copy").asInstanceOf[IncDes]))
    chain.op(Unit => chain("record"), SetTimestamp(tc, ts))
  }
}
