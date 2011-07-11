package org.agilewiki.blip
package componentFactories

import org.specs.SpecificationWithJUnit

case class Set(value: Int)

case class Get()

class SaverComponentFactory extends ComponentFactory {
  override def instantiate(actor: Actor) = new SaverComponent(actor, this)
}

class SaverComponent(actor: Actor, cf: SaverComponentFactory) extends Component(actor, cf) {
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

  override def instantiate(actor: Actor) = new DoubleComponent(actor, this)
}

class DoubleComponent(actor: Actor, cf: DoubleComponentFactory) extends Component(actor, cf) {
  bind(classOf[Times2], doubleFunc)

  private def doubleFunc(msg: AnyRef, rf: Any => Unit) {
    actor(Get()){ rsp =>
      val i = rsp.asInstanceOf[Int]
      actor(Set(i * 2)){ rsp2 =>
        rf(null)
      }
    }
  }
}

class DoubleFactory extends CompositeFactory(null) {
  protected def instantiate(mailbox: Mailbox) = {
    val actor = new Actor(mailbox, this)
    include(new SaverComponentFactory)
    actor
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
