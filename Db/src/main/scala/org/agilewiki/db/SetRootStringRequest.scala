package org.agilewiki
package db

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
  bind(classOf[Process], process)

  private def process(msg: AnyRef, rf: Any => Unit) {
    val transactionContext = msg.asInstanceOf[Process].transactionContext
    val results = new Results
    val chain = new Chain(results)
    chain.op(actor, Value(), "value")
    chain.op(systemServices, DbRoot(), "dbRoot")
    chain.op(Unit => {
      results("dbRoot")
    }, MakeSet(transactionContext, INC_DES_STRING_FACTORY_ID), "incDesString")
    chain.op(Unit => results("incDesString"), Unit => Set(transactionContext, results("value")))
    actor(chain)(rf)
 }
}
