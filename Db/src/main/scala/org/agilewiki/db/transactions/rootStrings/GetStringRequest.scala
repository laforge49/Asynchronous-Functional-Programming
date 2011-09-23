package org.agilewiki
package db
package transactions
package rootStrings

import blip._
import incDes._
import blocks._

object GetStringRequest {
  def apply() = (new GetStringRequestFactory).newActor(null).
    asInstanceOf[IncDesString]

  def process(db: Actor, key: String) = {
    val je = apply()
    val chain = new Chain
    chain.op(je, Set(null, key))
    chain.op(db, TransactionRequest(je))
    chain
  }
}

class GetStringRequestFactory
  extends IncDesStringFactory(new FactoryId("GetStringRequest")) {
  override protected def instantiate = {
    val req = super.instantiate
    addComponent(new QueryRequestComponent(req))
    addComponent(new GetStringRequestComponent(req))
    req
  }
}

class GetStringRequestComponent(actor: Actor)
  extends Component(actor) {
  bindSafe(classOf[Process], new ChainFactory(process))

  private def process(msg: AnyRef, chain: Chain) {
    chain.op(actor, Value(), "key")
    chain.op(systemServices, DbRoot(), "dbRoot")
    chain.op(Unit => chain("dbRoot"), Unit => GetValue(chain("key")), "value")
 }
}
