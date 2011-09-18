package org.agilewiki
package db

import blip._
import incDes._
import blocks._

object GetRootStringRequest {
  def apply() = (new GetRootStringRequestFactory).newActor(null).
    asInstanceOf[IncDes]

  def process(db: Actor) = {
    val je = apply()
    val chain = new Chain
    chain.op(db, TransactionRequest(je))
    chain
  }
}

class GetRootStringRequestFactory extends Factory(new FactoryId("GetRootStringRequest")) {
  override protected def instantiate = {
    val req = new IncDes
    addComponent(new QueryRequestComponent(req))
    addComponent(new GetRootStringRequestComponent(req))
    req
  }
}

class GetRootStringRequestComponent(actor: Actor)
  extends Component(actor) {
  bindSafe(classOf[Process], new ChainFactory(process))

  private def process(msg: AnyRef, chain: Chain) {
    chain.op(systemServices, DbRoot(), "dbRoot")
    chain.op(Unit => chain("dbRoot"), Value(), "incDesString")
    chain.op(Unit => chain("incDesString"), Value())
 }
}
