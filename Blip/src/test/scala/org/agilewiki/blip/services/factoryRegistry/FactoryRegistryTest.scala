package org.agilewiki.blip
package services
package factoryRegistry

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

  override def configure(compositeFactory: Factory) {
    val factoryRegistryComponentFactory =
      compositeFactory.componentFactory(classOf[FactoryRegistryComponentFactory]).
        asInstanceOf[FactoryRegistryComponentFactory]
    factoryRegistryComponentFactory.registerFactory(new GreeterFactory)
  }
}

case class DoIt()

class Driver extends Actor {
  bind(classOf[DoIt], doit)

  def doit(msg: AnyRef, rf: Any => Unit) {
    systemServices(Instantiate(FactoryId("greeter"), null)) {
      rsp =>
        val greeter = rsp.asInstanceOf[Actor]
        greeter(Greet())(rf)
    }
  }
}

class FactoryRegistryTest extends SpecificationWithJUnit {
  "FactoryRegistryTest" should {
    "instantiate" in {
      val systemServices = SystemServices(new SomeComponentFactory)
      try {
        val driver = new Driver
        driver.setSystemServices(systemServices)
        driver.setMailbox(systemServices.newSyncMailbox)
        Future(driver, DoIt())
      } finally {
        systemServices.close
      }
    }
  }
}
