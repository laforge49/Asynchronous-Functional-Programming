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
package rootStrings

import blip._
import incDes._
import blocks._

object SetStringRequest {
  def process(db: Actor, key: String, value: String) = {
    val je = (new SetStringRequestFactory).newActor(null).
    asInstanceOf[IncDes]
    val chain = new Chain
    chain.op(je, MakePutSet(null, "key", key))
    chain.op(je, MakePutSet(null, "value", value))
    chain.op(db, TransactionRequest(je))
    chain
  }
}

class SetStringRequestFactory
  extends IncDesStringStringMapFactory(new FactoryId("SetStringRequest")) {
  override protected def instantiate = {
    val req = super.instantiate
    addComponent(new UpdateRequestComponent(req))
    addComponent(new SetStringRequestComponent(req))
    req
  }
}

class SetStringRequestComponent(actor: Actor)
  extends Component(actor) {
  bindSafe(classOf[Process], new ChainFactory(process))

  private def process(msg: AnyRef, chain: Chain) {
    val transactionContext = msg.asInstanceOf[Process].transactionContext
    chain.op(actor, GetValue("key"), "key")
    chain.op(actor, GetValue("value"), "value")
    chain.op(systemServices, DbRoot(), "dbRoot")
    chain.op(Unit => chain("dbRoot"),
      MakeSet(transactionContext, INC_DES_STRING_STRING_MAP_FACTORY_ID), "strings")
    chain.op(Unit => chain("strings"),
      Unit => MakePutSet(transactionContext, chain("key"), chain("value")))
 }
}
