package org.agilewiki.blip
package components

import org.specs.SpecificationWithJUnit

case class Set(value: Int)

case class Get()

class Saver(mailbox: Mailbox, factory: Factory) extends Actor(mailbox, factory) {
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

class DoubleComponent(saver: Saver) extends Component(saver, null) {
  bind(classOf[Times2], doubleFunc)

  private def doubleFunc(msg: AnyRef, rf: Any => Unit) {
    saver.i *= 2
    rf(null)
  }
}

class DoubleFactory extends Factory(null) {
  protected def instantiate(mailbox: Mailbox) = {
    val saver = new Saver(mailbox, this)
    val doubleComponent = new DoubleComponent(saver)
    addComponent(saver, doubleComponent)
    saver
  }
}

class ComponentTest extends SpecificationWithJUnit {
  "SimpleActor" should {
    "double" in {
      val doubleFactory = new DoubleFactory
      val double = doubleFactory.newActor(new Mailbox)
      Future(double, Set(21))
      Future(double, Times2())
      println(Future(double, Get()))
    }
  }
}
