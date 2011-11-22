package org.agilewiki.blip
package gettingStarted

import org.specs.SpecificationWithJUnit
import bind._

case class Times2(value: Int)
case class Add(value1: Int, value2: Int)

class SimpleActor extends Actor {
  bind(classOf[Times2], times2)
  bind(classOf[Add], add)
  private def times2(msg: AnyRef, rf: Any => Unit) { rf(msg.asInstanceOf[Times2].value * 2) }
  private def add(msg: AnyRef, rf: Any => Unit) {
    val req = msg.asInstanceOf[Add]
    rf(req.value1 + req.value2)
  }
}

class Test extends SpecificationWithJUnit {
  "SimpleActor" should {
    "print" in {
      val simpleActor = new SimpleActor
      println(Future(simpleActor, Times2(21)))
      println(Future(simpleActor, Add(1, 2)))
    }
  }
}
