package org.agilewiki
package db
package transactions

import blip._
import incDes._
import blocks._

object GetRootRequest {
  def apply() = (new GetRootRequestFactory).newActor(null).
    asInstanceOf[IncDes]

  def process(db: Actor) = {
    val je = apply()
    val chain = new Chain
    chain.op(db, TransactionRequest(je))
    chain
  }
}

class GetRootRequestFactory extends Factory(new FactoryId("GetRootRequest")) {
  override protected def instantiate = {
    val req = new IncDes
    addComponent(new QueryRequestComponent(req))
    addComponent(new GetRootRequestComponent(req))
    req
  }
}

class GetRootRequestComponent(actor: Actor)
  extends Component(actor) {
  bindSafe(classOf[Process], new ChainFactory(process))

  private def process(msg: AnyRef, chain: Chain) {
    chain.op(systemServices, DbRoot(), "dbRoot")
    chain.op(Unit => chain("dbRoot"), Value())
  }
}
