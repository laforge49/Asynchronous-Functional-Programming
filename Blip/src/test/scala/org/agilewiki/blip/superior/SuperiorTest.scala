package org.agilewiki.blip
package superior

import org.specs.SpecificationWithJUnit
import bind._

case class Times2(value: Int)
case class Add(value1: Int, value2: Int)

class SuperiorActor extends Actor {
  bind(classOf[Times2], times2)
  private def times2(msg: AnyRef, rf: Any => Unit) { rf(msg.asInstanceOf[Times2].value * 2) }
}

class InferiorActor extends Actor {
  bind(classOf[Add], add)
  private def add(msg: AnyRef, rf: Any => Unit) {
    val req = msg.asInstanceOf[Add]
    rf(req.value1 + req.value2)
  }
}

class Test extends SpecificationWithJUnit {
  "SimpleActor" should {
    "print" in {
      val superiorActor = new SuperiorActor
      val inferiorActor = new InferiorActor
      inferiorActor.setSuperior(superiorActor)
      println(Future(inferiorActor, Add(1, 2)))
      println(Future(superiorActor, Times2(21)))
    }
  }
}
