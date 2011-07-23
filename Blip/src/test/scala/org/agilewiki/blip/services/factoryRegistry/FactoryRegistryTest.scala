package org.agilewiki.blip
package services
package factoryRegistry

import org.specs.SpecificationWithJUnit
import java.io.{BufferedReader, InputStreamReader, ByteArrayInputStream, File}

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

  override def configure(compositeFactory: Factory) {
    val factoryRegistryComponentFactory =
      compositeFactory.componentFactory(classOf[FactoryRegistryComponentFactory]).
        asInstanceOf[FactoryRegistryComponentFactory]
    factoryRegistryComponentFactory.registerFactory(new GreeterFactory)
  }
}

case class DoIt()

class Driver extends Actor(new Mailbox, null) {
  bind(classOf[DoIt], doit)

  def doit(msg: AnyRef, rf: Any => Unit) {
    systemServices(Instantiate(FactoryId("greeter"), null)) {rsp =>
      val greeter = rsp.asInstanceOf[Actor]
      greeter(Greet())(rf)
    }
  }
}

class FactoryRegistryTest extends SpecificationWithJUnit {
  "FactoryRegistryTest" should {
    "instantiate" in {
      val systemServices = SystemServices(new SomeComponentFactory)
      val driver = new Driver
      driver.setSystemServices(systemServices)
      Future(driver, DoIt())
    }
  }
}
