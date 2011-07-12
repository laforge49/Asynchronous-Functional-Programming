package org.agilewiki.blip
package componentFactories

import org.specs.SpecificationWithJUnit

case class Set(value: Int)

case class Get()

class SaverComponentFactory extends ComponentFactory {
  override def instantiate(actor: Actor) = new SaverComponent(actor.asInstanceOf[Composite], this)
}

class SaverComponent(composite: Composite, cf: SaverComponentFactory) extends Component(composite, cf) {
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

class DoubleComponentFactory extends ComponentFactory {
  addDependency(classOf[SaverComponentFactory])

  override def instantiate(actor: Actor) = new DoubleComponent(actor.asInstanceOf[Composite], this)
}

class DoubleComponent(composite: Composite, cf: DoubleComponentFactory) extends Component(composite, cf) {
  val saver = composite.components.get(classOf[SaverComponent]).asInstanceOf[SaverComponent]

  bind(classOf[Times2], doubleFunc)

  private def doubleFunc(msg: AnyRef, rf: Any => Unit) {
    saver.i *= 2
    rf(null)
  }
}

class DoubleFactory extends CompositeFactory(null) {
  include(new DoubleComponentFactory)
}

class ComponentFactoryTest extends SpecificationWithJUnit {
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
