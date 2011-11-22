package org.agilewiki.blip
package components

import org.specs.SpecificationWithJUnit
import bind._

case class Set(value: Int)

case class Get()

class Saver extends Actor {
  var i = 0

  bind(classOf[Set], setFunc)

  private def setFunc(msg: AnyRef, rf: Any => Unit) {
    i = msg.asInstanceOf[Set].value
    rf(null)
  }

  bind(classOf[Get], getFunc)

  private def getFunc(msg: AnyRef, rf: Any => Unit) {
    rf(i)
  }
}

case class Times2()

class DoubleComponent(saver: Saver) extends Component(saver) {
  bind(classOf[Times2], doubleFunc)

  private def doubleFunc(msg: AnyRef, rf: Any => Unit) {
    saver.i *= 2
    rf(null)
  }
}

class DoubleFactory extends Factory(null) {
  override protected def instantiate = {
    val saver = new Saver
    val doubleComponent = new DoubleComponent(saver)
    addComponent(doubleComponent)
    saver
  }
}

class ComponentTest extends SpecificationWithJUnit {
  "SimpleActor" should {
    "double" in {
      val systemServices = SystemServices()
      try {
        val doubleFactory = new DoubleFactory
        val double = doubleFactory.newActor(systemServices.newSyncMailbox)
        Future(double, Set(21))
        Future(double, Times2())
        println(Future(double, Get()))
      } finally {
        systemServices.close
      }
    }
  }
}
