package org.agilewiki.blip
package services
package properties

import org.specs.SpecificationWithJUnit

case class DoIt()

class Driver extends Actor {
  bind(classOf[DoIt], doIt)

  def doIt(msg: AnyRef, rf: Any => Unit) {
    println("a = " + GetProperty("a")) //set in p1
    println("b = " + GetProperty("b")) //reset in p2
    println("c = " + GetProperty("c")) //overridden in p2
    println("d = " + GetProperty("d")) //set in p2
    println("e = " + GetProperty("e")) //unspecified
    rf(null)
  }
}

class PropertiesTest extends SpecificationWithJUnit {
  "PropertiesTest" should {
    "use properties" in {
      val p1 = new Properties
      p1.put("a", "1")
      p1.put("b", "2")
      p1.put("c", "3")
      val systemServices = SystemServices(new PropertiesComponentFactory, properties = p1)
      val p2 = new Properties
      p2.put("b", null)
      p2.put("c", "11")
      p2.put("d", "12")
      val aSubsystem = Subsystem(systemServices, new PropertiesComponentFactory, properties = p2)
      val driver = new Driver
      driver.setSystemServices(aSubsystem)
      Future(driver, DoIt())
    }
  }
}
