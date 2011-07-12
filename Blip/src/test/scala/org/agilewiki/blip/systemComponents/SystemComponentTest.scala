package org.agilewiki.blip
package systemComponents

import org.specs.SpecificationWithJUnit

class SC1Factory extends ComponentFactory {
  addDependency(classOf[SC2Factory])

  override protected def instantiate(actor: Actor) = new SC1(actor, this)
}

class SC1(actor: Actor, componentFactory: ComponentFactory) extends SystemComponent(actor, componentFactory) {
  override def open {
    println("SC1 open")
  }

  override def close {
    println("SC1 close")
  }
}

class SC2Factory extends ComponentFactory {
  override protected def instantiate(actor: Actor) = new SC2(actor, this)
}

class SC2(actor: Actor, componentFactory: ComponentFactory) extends SystemComponent(actor, componentFactory) {
  override def open {
    println("SC2 open")
  }

  override def close {
    println("SC2 close")
  }
}


class SystemComponentTest extends SpecificationWithJUnit {
  "system components" should {
    "print open and close" in {
      val systemServices = SystemServices(new SC1Factory)
      systemServices.open
      systemServices.close
    }
  }
}
