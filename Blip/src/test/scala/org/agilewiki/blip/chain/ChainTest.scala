package org.agilewiki
package blip
package chain

import org.specs.SpecificationWithJUnit
import bind._

case class Prnt(value: Any)

case class DoIt()

case class PrntChain(value: Any)

case class UltimateAnswer()

class SimpleActor extends Actor {
  bind(classOf[Prnt], prnt)
  bind(classOf[DoIt], doIt)
  bindMessageLogic(classOf[PrntChain], new ChainFactory(chainFunction))
  bind(classOf[UltimateAnswer], ultimateAnswer)

  private def prnt(msg: AnyRef, rf: Any => Unit) {
    val req = msg.asInstanceOf[Prnt]
    println(req.value)
    rf(null)
  }

  private def doIt(msg: AnyRef, rf: Any => Unit) {
    this(PrntChain())(rf)
  }

  private def chainFunction(msg: AnyRef, chain: Chain) {
    chain.op(this, Prnt(1))
    chain.op(this, Prnt(2))
    chain.op(this, Prnt(3))
    chain.op(this, Prnt("scadoo!"))
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
      chain.op(simpleActor, Prnt(1))
      chain.op(simpleActor, Prnt(2))
      chain.op(simpleActor, Prnt(3))
      chain.op(simpleActor, Prnt("scadoo!"))
      Future(simpleActor, chain)
    }
    "print chain" in {
      val simpleActor = new SimpleActor
      Future(simpleActor, DoIt())
    }
    "pass results" in {
      val results = new Results
      val chain = new Chain(results)
      val simpleActor = new SimpleActor
      chain.op(simpleActor, UltimateAnswer(), "ultimateAnswer")
      chain.op(
        simpleActor,
        Unit => Prnt("The Ultimate Answer to Everything: " + results("ultimateAnswer"))
      )
      Future(simpleActor, chain)
    }
    "simplified" in {
      val chain = new Chain
      val simpleActor = new SimpleActor
      chain.op(simpleActor, UltimateAnswer(), "ultimateAnswer")
      chain.op(
        simpleActor,
        Unit => Prnt("The Ultimate Answer to Everything: " + chain("ultimateAnswer"))
      )
      Future(simpleActor, chain)
    }
  }
}
