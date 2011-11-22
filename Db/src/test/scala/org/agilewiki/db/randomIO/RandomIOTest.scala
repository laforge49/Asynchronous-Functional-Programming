package org.agilewiki
package db
package randomIO

import blip._
import bind._
import services._
import org.specs.SpecificationWithJUnit

case class DoIt()

class Driver extends Actor {
  bind(classOf[DoIt], doIt)

  def doIt(msg: AnyRef, rf: Any => Unit) {
    systemServices(WriteBytes(0, new Array[Byte](5000))) {
      rsp1 => {
        systemServices(ReadBytes(0, 5000)) {
          rsp2 => {
            rf(rsp2)
          }
        }
      }
    }
  }
}

class RandomIOTest extends SpecificationWithJUnit {
  "RandomIOTest" should {
    "read and write" in {
      val dbName = "RandomIOTest.db"
      val file = new java.io.File(dbName)
      file.delete
      val properties = new Properties
      properties.put("dbPathname", dbName)
      val systemServices = SystemServices(new RandomIOComponentFactory, properties = properties)
      try {
        val driver = new Driver
        driver.setExchangeMessenger(systemServices.newSyncMailbox)
        driver.setSystemServices(systemServices)
        val bytes = Future(driver, DoIt()).asInstanceOf[Array[Byte]]
        bytes.length must be equalTo (5000)
        systemServices.close
      } finally {
        systemServices.close
      }
    }
  }
}
