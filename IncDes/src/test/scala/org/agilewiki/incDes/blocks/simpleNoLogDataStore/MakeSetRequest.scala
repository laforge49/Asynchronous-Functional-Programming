package org.agilewiki
package incDes
package blocks
package simpleNoLogDataStore

import blip._

class MakeSetRequestComponentFactory extends ComponentFactory {
  addDependency(classOf[UpdateRequestComponentFactory])

  override def instantiate(actor: Actor) = new MakeSetRequestComponent(actor)
}

class MakeSetRequestComponent(actor: Actor)
  extends Component(actor) {
  bind(classOf[Process], process)

  private def process(msg: AnyRef, rf: Any => Unit) {
    val transactionContext = msg.asInstanceOf[Process].transactionContext
    systemServices(DbRoot()) {
      rsp1 => {
        val dbRoot = rsp1.asInstanceOf[Block]
        dbRoot(MakeSet(transactionContext, INC_DES_STRING_FACTORY_ID)) {
          rsp2 => {
            val incDesString = rsp2.asInstanceOf[IncDesString]
            incDesString(Set(transactionContext, "Hello world!"))(rf)
          }
        }
      }
    }
  }
}
