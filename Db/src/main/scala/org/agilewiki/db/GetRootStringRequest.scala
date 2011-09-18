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
  bind(classOf[Process], process)

  private def process(msg: AnyRef, rf: Any => Unit) {
    val transactionContext = msg.asInstanceOf[Process].transactionContext
    val results = new Results
    val chain = new Chain(results)
    chain.op(systemServices, DbRoot(), "dbRoot")
    chain.op(Unit => results("dbRoot"), Value(), "incDesString")
    chain.op(Unit => results("incDesString"), Value())
    actor(chain)(rf)
 }
}
