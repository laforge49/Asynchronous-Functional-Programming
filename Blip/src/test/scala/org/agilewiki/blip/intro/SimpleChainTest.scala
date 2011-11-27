package org.agilewiki.blip
package intro

import org.specs.SpecificationWithJUnit
import bind._

case class Prnt(value: Any)

case class UltimateAnswer()

class SimpleActor extends Actor {
  bind(classOf[Prnt], prnt)
  bindMessageLogic(classOf[UltimateAnswer], new ConcurrentData(42))

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
      chain.op(simpleActor, Prnt("The answer to everything:"))
      chain.op(simpleActor, UltimateAnswer(), "answer")
      chain.op(simpleActor, Unit => Prnt(chain("answer")))
      Future(simpleActor, chain)
    }
    /*
    Output:
    The answer to everything:
    42
    */
  }
}
