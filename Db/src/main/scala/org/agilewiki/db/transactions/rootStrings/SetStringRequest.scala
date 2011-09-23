package org.agilewiki
package db
package transactions
package rootStrings

import blip._
import incDes._
import blocks._

object SetStringRequest {
  def apply() = (new SetStringRequestFactory).newActor(null).
    asInstanceOf[IncDesNavMap[String, IncDesString, String]]

  def process(db: Actor, key: String, value: String) = {
    val je = apply()
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
