package org.agilewiki.blip
package lifecycle

import org.specs.SpecificationWithJUnit

case class DoIt()

class SC1Factory extends ComponentFactory {
  addDependency(classOf[SC2Factory])

  override protected def instantiate(actor: Actor) = new SC1(actor, this)
}

class SC1(actor: Actor, componentFactory: ComponentFactory)
  extends Component(actor, componentFactory) {
  bind(classOf[DoIt], doit)

  override def open {
    println("SC1 open")
  }

  override def close {
    println("SC1 close")
  }

  def doit(msg: AnyRef, rf: Any => Unit) {
    rf(null)
  }
}

class SC2Factory extends ComponentFactory {
  override protected def instantiate(actor: Actor) = new SC2(actor, this)
}

class SC2(actor: Actor, componentFactory: ComponentFactory)
  extends Component(actor, componentFactory) {
  override def open {
    println("SC2 open")
  }

  override def close {
    println("SC2 close")
  }
}

class CompositeFactory extends Factory(null) {
  include(new SC1Factory)
}

class LifecycleTest extends SpecificationWithJUnit {
  "components" should {
    "print open and close" in {
      val actor = (new CompositeFactory).newActor(null)
      Future(actor, DoIt())
      actor.close
    }
  }
}
