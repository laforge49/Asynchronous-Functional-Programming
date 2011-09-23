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
package rootString

import blip._
import incDes._
import blocks._

object SetRootStringRequest {
  def apply() = (new SetRootStringRequestFactory).newActor(null).
    asInstanceOf[IncDesString]

  def process(db: Actor, value: String) = {
    val je = apply()
    val chain = new Chain
    chain.op(je, Set(null, value))
    chain.op(db, TransactionRequest(je))
    chain
  }
}

class SetRootStringRequestFactory extends Factory(new FactoryId("SetRootStringRequest")) {
  override protected def instantiate = {
    val req = new IncDesString
    addComponent(new UpdateRequestComponent(req))
    addComponent(new SetRootStringRequestComponent(req))
    req
  }
}

class SetRootStringRequestComponent(actor: Actor)
  extends Component(actor) {
  bindSafe(classOf[Process], new ChainFactory(process))

  private def process(msg: AnyRef, chain: Chain) {
    val transactionContext = msg.asInstanceOf[Process].transactionContext
    chain.op(actor, Value(), "value")
    chain.op(systemServices, DbRoot(), "dbRoot")
    chain.op(Unit => chain("dbRoot"),
      MakeSet(transactionContext, INC_DES_STRING_FACTORY_ID), "incDesString")
    chain.op(Unit => chain("incDesString"), Unit => Set(transactionContext, chain("value")))
 }
}
