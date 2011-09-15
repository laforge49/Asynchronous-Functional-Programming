package org.agilewiki
package incDes
package blocks
package db

import blip._

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
  include(new SetRootStringRequestComponentFactory)

  override protected def instantiate = new IncDesString
}

class SetRootStringRequestComponentFactory extends ComponentFactory {
  addDependency(classOf[UpdateRequestComponentFactory])

  override def instantiate(actor: Actor) = new SetRootStringRequestComponent(actor)
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
