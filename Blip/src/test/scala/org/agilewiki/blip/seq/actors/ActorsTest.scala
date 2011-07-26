package org.agilewiki.blip
package seq
package actors

import org.specs.SpecificationWithJUnit
import services.{ActorRegistryComponentFactory, FactoryRegistryComponentFactory}
import services._

case class Greet()

class Greeter(mailbox: Mailbox, factory: Factory)
  extends Actor(mailbox, factory) {
  bind(classOf[Greet], greet)

  def greet(msg: AnyRef, rf: Any => Unit) {
    println("Hello world!")
    rf(null)
  }
}

class GreeterFactory
  extends Factory(new FactoryId("greeter")) {
  override def instantiate(mailbox: Mailbox) = new Greeter(mailbox, this)
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

class Driver extends Actor(new Mailbox, null) {
  bind(classOf[DoIt1], doit1)
  bind(classOf[DoIt2], doit2)

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
    systemServices(Actors()) {
      rsp =>
        val actors = rsp.asInstanceOf[Actor]
        actors(
          Loop((key: String, value: Actor) => println(key + " " + value.getClass.getName))) {
          rsp => rf(null)
        }
    }
  }
}

class ActorsTest extends SpecificationWithJUnit {
  "ActorsTest" should {
    "display actors" in {
      val systemServices = SystemServices(new SomeComponentFactory)
      val driver = new Driver
      driver.setSystemServices(systemServices)
      Future(driver, DoIt1())
      Future(driver, DoIt2())
    }
  }
}
