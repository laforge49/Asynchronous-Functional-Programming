package org.agilewiki.blip
package dependencies

import org.specs.SpecificationWithJUnit

case class DoIt()

class SC1Factory extends ComponentFactory {
  addDependency(classOf[SC2Factory])

  override protected def instantiate(actor: Actor) = new SC1(actor, this)
}

class SC1(actor: Actor, componentFactory: ComponentFactory)
  extends Component(actor, componentFactory) {
}

class SC2Factory extends ComponentFactory {
  override protected def instantiate(actor: Actor) = new SC2(actor, this)
}

class SC2(actor: Actor, componentFactory: ComponentFactory)
  extends Component(actor, componentFactory) {
  bind(classOf[DoIt], doit)

  def doit(msg: AnyRef, rf: Any => Unit) {
    println("Done it!")
    rf(null)
  }
}

class CompositeFactory extends Factory(null) {
  include(new SC1Factory)
}

class DependenciesTest extends SpecificationWithJUnit {
  "DependenciesTest" should {
    "include dependencies" in {
      val actor = (new CompositeFactory).newActor(null)
      Future(actor, DoIt())
    }
  }
}
