package org.agilewiki.blip
package services
package actorRegistry

import org.specs.SpecificationWithJUnit
import bind._

case class Greet()

class Greeter
  extends Id_Actor {
  bind(classOf[Greet], greet)

  def greet(msg: AnyRef, rf: Any => Unit) {
    println("Hello world!")
    rf(null)
  }
}

class GreeterFactory
  extends Factory(new FactoryId("greeter")) {
  override def instantiate = new Greeter
}

class SomeComponentFactory
  extends ComponentFactory {
  addDependency(classOf[FactoryRegistryComponentFactory])
  addDependency(classOf[ActorRegistryComponentFactory])

  override def configure(compositeFactory: Factory) {
    FactoryRegistryComponentFactory.register(compositeFactory, new GreeterFactory)
  }
}

case class DoIt1()

case class DoIt2()

class Driver extends Actor {
  bind(classOf[DoIt1], doit1)
  bind(classOf[DoIt2], doit2)

  def doit1(msg: AnyRef, rf: Any => Unit) {
    val chain = new Chain()
    chain.op(systemServices, Instantiate(FactoryId("greeter"), null), "a")
    chain.op(systemServices, Unit => {
      val a = chain("a").asInstanceOf[Id_Actor]
      a.id(ActorId("a"))
      Register(a)
    })
    chain.op(systemServices, Instantiate(FactoryId("greeter"), null), "b")
    chain.op(systemServices, Unit => {
      val b = chain("b").asInstanceOf[Id_Actor]
      b.id(ActorId("b"))
      Register(b)
    })
    systemServices(chain)(rf)
  }

  def doit2(msg: AnyRef, rf: Any => Unit) {
    val chain = new Chain()
    chain.op(systemServices, Unregister(ActorId("a")))
    chain.op(systemServices, ResolveName(ActorId("b"), null), "b")
    chain.op(chain("b"), Greet())
    systemServices(chain)(rf)
  }
}

class ActorRegistryTest extends SpecificationWithJUnit {
  "ActorRegistryTest" should {
    "register" in {
      val systemServices = SystemServices(new SomeComponentFactory)
      try {
        val driver = new Driver
        driver.setExchangeMessenger(systemServices.newSyncMailbox)
        Future(driver, DoIt1())
        Future(driver, DoIt2())
      } finally {
        systemServices.close
      }
    }
  }
}
