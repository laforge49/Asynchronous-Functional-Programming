package org.agilewiki.blip
package lifecycle

import org.specs.SpecificationWithJUnit

case class DoIt()

class SC1Factory extends ComponentFactory {
  addDependency(classOf[SC2Factory])

  override protected def instantiate(actor: Actor) = new SC1(actor)
}

class SC1(actor: Actor)
  extends Component(actor) {

  override def open {
    println("SC1 open")
  }

  override def close {
    println("SC1 close")
  }
}

class SC2Factory extends ComponentFactory {
  override protected def instantiate(actor: Actor) = new SC2(actor)
}

class SC2(actor: Actor)
  extends Component(actor) {
  bind(classOf[DoIt], doit)

  override def open {
    println("SC2 open")
  }

  override def close {
    println("SC2 close")
  }

  def doit(msg: AnyRef, rf: Any => Unit) {
    println("Done it!")
    rf(null)
  }
}

class LifecycleTest extends SpecificationWithJUnit {
  "components" should {
    "print open and close" in {
      val actor = (new CompositeFactory(null, new SC1Factory)).newActor(null)
      Future(actor, DoIt())
      actor.close
    }
  }
}
