package org.agilewiki
package db
package transactions

import blip._
import incDes._
import blocks._

object SetRootRequest {
  def apply() = (new SetRootRequestFactory).newActor(null).
    asInstanceOf[IncDesIncDes]

  def process(db: Actor, value: IncDes) = {
    val je = apply()
    je.setSystemServices(db.systemServices)
    val chain = new Chain
    chain.op(je, Set(null, value))
    chain.op(db, TransactionRequest(je))
    chain
  }
}

class SetRootRequestFactory extends Factory(new FactoryId("SetRootRequest")) {
  override protected def instantiate = {
    val req = new IncDesIncDes
    addComponent(new UpdateRequestComponent(req))
    addComponent(new SetRootRequestComponent(req))
    req
  }
}

class SetRootRequestComponent(actor: Actor)
  extends Component(actor) {
  bindSafe(classOf[Process], new ChainFactory(process))

  private def process(msg: AnyRef, chain: Chain) {
    val transactionContext = msg.asInstanceOf[Process].transactionContext
    chain.op(actor, Value(), "value")
    chain.op(Unit => chain("value"), Copy(null), "copy")
    chain.op(systemServices, DbRoot(), "dbRoot")
    chain.op(Unit => chain("dbRoot"), Unit => Set(transactionContext, chain("copy")))
 }
}
