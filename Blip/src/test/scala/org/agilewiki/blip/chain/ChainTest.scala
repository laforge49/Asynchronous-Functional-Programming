package org.agilewiki.blip
package chain

import org.specs.SpecificationWithJUnit

case class Prnt(value: Any)

class SimpleActor extends Actor {
  bind(classOf[Prnt], prnt)
  private def prnt(msg: AnyRef, rf: Any => Unit) {
    val req = msg.asInstanceOf[Prnt]
    println(req.value)
    rf(null)
  }
}

class ChainTest extends SpecificationWithJUnit {
  "ChainTest" should {
    "print bunches" in {
      val chain = new Chain
      val simpleActor = new SimpleActor
      chain.add(simpleActor, Prnt(1))
      chain.add(simpleActor, Prnt(2))
      chain.add(simpleActor, Prnt(3))
      chain.add(simpleActor, Prnt("scadoo!"))
      println(Future(simpleActor, chain))
    }
  }
}
