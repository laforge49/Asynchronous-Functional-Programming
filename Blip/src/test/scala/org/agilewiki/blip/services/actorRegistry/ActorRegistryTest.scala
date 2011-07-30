package org.agilewiki.blip
package services
package actorRegistry

import org.specs.SpecificationWithJUnit

case class Greet()

class Greeter
  extends Actor {
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
  setMailbox(new Mailbox)

  def doit1(msg: AnyRef, rf: Any => Unit) {
    systemServices(Instantiate(FactoryId("greeter"), null)) {rsp =>
      val greeter = rsp.asInstanceOf[Actor]
      greeter.id(ActorId("a"))
      systemServices(Register(greeter)) {rsp => {}}
    }
    systemServices(ResolveName(FactoryId("greeter"), null)) {rsp =>
      val greeter = rsp.asInstanceOf[Actor]
      greeter.id(ActorId("b"))
      systemServices(Register(greeter)) {rsp => {}}
    }
    rf(null)
  }

  def doit2(msg: AnyRef, rf: Any => Unit) {
    systemServices(Unregister(ActorId("a"))) {rsp => {}}
    systemServices(ResolveName(ActorId("b"), null)) {rsp =>
      val actor = rsp.asInstanceOf[Actor]
      actor(Greet())(rf)
    }
  }
}

class ActorRegistryTest extends SpecificationWithJUnit {
  "ActorRegistryTest" should {
    "register" in {
      val systemServices = SystemServices(new SomeComponentFactory)
      val driver = new Driver
      driver.setSystemServices(systemServices)
      Future(driver, DoIt1())
      Future(driver, DoIt2())
    }
  }
}
