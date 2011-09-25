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

import blip._
import seq._
import incDes._
import blocks._

object SetRequest {
  def process(db: Actor, pathname: String, value: IncDes) = {
    var pn = pathname
    if (pn.startsWith("/")) pn = pn.substring(1)
    val pnid = IncDesString(null)
    val pnidid = IncDesIncDes(null)
    val vidid = IncDesIncDes(null)
    val jef = new SetRequestFactory
    jef.configure(db)
    val je = jef.newActor(null).asInstanceOf[IncDes]
    je.setSystemServices(db.systemServices)
    val chain = new Chain
    chain.op(je, Put[String, IncDesIncDes, IncDes](null, "pathname", pnidid))
    chain.op(pnidid, Set(null, pnid))
    chain.op(pnid, Set(null, pn))
    chain.op(je, Put[String, IncDesIncDes, IncDes](null, "value", vidid))
    chain.op(vidid, Set(null, value))
    chain.op(db, TransactionRequest(je))
    chain
  }
}

class SetRequestFactory
  extends IncDesStringIncDesMapFactory(DBT_SET) {
  override protected def instantiate = {
    val req = super.instantiate
    addComponent(new UpdateRequestComponent(req))
    addComponent(new SetRequestComponent(req))
    req
  }
}

class SetRequestComponent(actor: Actor)
  extends Component(actor) {
  bindSafe(classOf[Process], new ChainFactory(process))

  private def process(msg: AnyRef, chain: Chain) {
    val transactionContext = msg.asInstanceOf[Process].transactionContext
    var key = ""
    chain.op(actor, GetValue("pathname"), "pathnameId")
    chain.op(Unit => chain("pathnameId"), Value(), "pathname")
    chain.op(actor, GetValue("value"), "value")
    chain.op(Unit => chain("value"), Copy(null), "copy")
    chain.op(systemServices, DbRoot(), "dbRoot")
    chain.op(Unit => chain("dbRoot"),
      Unit => Resolve(chain("pathname").asInstanceOf[String]), "tuple")
    chain.op(Unit => {
      val (incDes, k) = chain("tuple").asInstanceOf[(IncDes, String)]
      key = k
      incDes
    }, Unit => Assign(transactionContext, key, chain("copy").asInstanceOf[IncDes]))
  }
}
