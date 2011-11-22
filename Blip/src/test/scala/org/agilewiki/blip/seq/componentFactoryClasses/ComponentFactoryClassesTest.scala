package org.agilewiki.blip
package seq
package componentFactoryClasses

import org.specs.SpecificationWithJUnit
import bind._

case class Set(value: Int)

case class Get()

class SaverComponentFactory extends ComponentFactory {
  override def instantiate(actor: Actor) = new SaverComponent(actor)
}

class SaverComponent(actor: Actor) extends Component(actor) {
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

  override def instantiate(actor: Actor) = new DoubleComponent(actor)
}

class DoubleComponent(actor: Actor) extends Component(actor) {
  val saver = actor.component(classOf[SaverComponentFactory]).asInstanceOf[SaverComponent]

  bind(classOf[Times2], doubleFunc)

  private def doubleFunc(msg: AnyRef, rf: Any => Unit) {
    saver.i *= 2
    rf(null)
  }
}

class DoubleFactory extends Factory(null) {
  include(new DoubleComponentFactory)
}

class ComponentFactoryClassesTest extends SpecificationWithJUnit {
  "ComponentFactoryClassesTest" should {
    "print component factory classes" in {
      val systemServices = SystemServices()
      try {
        val doubleFactory = new DoubleFactory
        val double = doubleFactory.newActor(systemServices.newSyncMailbox)
        val componentFactoryClasses = double.componentFactoryClasses
        Future(componentFactoryClasses,
          Loop((key: Class[_ <: ComponentFactory], value: Class[_ <: ComponentFactory]) => println(value)))
      } finally {
        systemServices.close
      }
    }
  }
}
