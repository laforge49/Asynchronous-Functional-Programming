package org.agilewiki.blip
package chain

import org.specs.SpecificationWithJUnit

case class Prnt(value: Any)

case class UltimateAnswer()

class SimpleActor extends Actor {
  bind(classOf[Prnt], prnt)
  bind(classOf[UltimateAnswer], ultimateAnswer)

  private def prnt(msg: AnyRef, rf: Any => Unit) {
    val req = msg.asInstanceOf[Prnt]
    println(req.value)
    rf(null)
  }

  private def ultimateAnswer(msg: AnyRef, rf: Any => Unit) {
    rf(42)
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
      Future(simpleActor, chain)
    }
    "pass results" in {
      val results = new Results
      val chain = new Chain(results)
      val simpleActor = new SimpleActor
      chain.add(simpleActor, UltimateAnswer(), "ultimateAnswer")
      chain.addFuncs(
        Unit => simpleActor,
        Unit => Prnt("The Ultimate Answer to Everything: " + results("ultimateAnswer"))
      )
      Future(simpleActor, chain)
    }
  }
}
