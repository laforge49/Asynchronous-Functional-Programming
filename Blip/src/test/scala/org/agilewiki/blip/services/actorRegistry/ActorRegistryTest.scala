package org.agilewiki.blip
package services
package actorRegistry

import org.specs.SpecificationWithJUnit
import bind._

case class Greet()

class Greeter
  extends Actor
  with IdActor {
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
    val factoryRegistryComponentFactory =
      compositeFactory.componentFactory(classOf[FactoryRegistryComponentFactory]).
        asInstanceOf[FactoryRegistryComponentFactory]
    factoryRegistryComponentFactory.registerFactory(new GreeterFactory)
  }
}

case class DoIt1()

case class DoIt2()

class Driver extends Actor {
  bind(classOf[DoIt1], doit1)
  bind(classOf[DoIt2], doit2)

  def doit1(msg: AnyRef, rf: Any => Unit) {
    systemServices(Instantiate(FactoryId("greeter"), null)) {
      rsp =>
        val greeter = rsp.asInstanceOf[IdActor]
        greeter.id(ActorId("a"))
        systemServices(Register(greeter)) {
          rsp => {}
        }
    }
    systemServices(ResolveName(FactoryId("greeter"), null)) {
      rsp =>
        val greeter = rsp.asInstanceOf[IdActor]
        greeter.id(ActorId("b"))
        systemServices(Register(greeter)) {
          rsp => {}
        }
    }
    rf(null)
  }

  def doit2(msg: AnyRef, rf: Any => Unit) {
    systemServices(Unregister(ActorId("a"))) {
      rsp => {}
    }
    systemServices(ResolveName(ActorId("b"), null)) {
      rsp =>
        val actor = rsp.asInstanceOf[Actor]
        actor(Greet())(rf)
    }
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
