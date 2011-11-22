package org.agilewiki.blip
package seq
package factories

import org.specs.SpecificationWithJUnit
import bind._
import services._

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
    systemServices(Factories()) {
      rsp =>
        val factories = rsp.asInstanceOf[Actor]
        factories(
          Loop((key: String, value: Factory) => println(key + " " + value.getClass.getName))) {
          rsp => rf(null)
        }
    }
  }
}

class FactoriesTest extends SpecificationWithJUnit {
  "FactoriesTest" should {
    "print factories" in {
      val systemServices = SystemServices(new SomeComponentFactory)
      try {
        val driver = new Driver
        driver.setSystemServices(systemServices)
        driver.setExchangeMessenger(systemServices.newSyncMailbox)
        Future(driver, DoIt())
      } finally {
        systemServices.close
      }
    }
  }
}
