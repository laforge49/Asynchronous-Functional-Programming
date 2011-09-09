package org.agilewiki
package incDes
package blocks
package blockIO

import blip._
import services._
import org.specs.SpecificationWithJUnit

case class DoIt()

class Driver extends Actor {
  bind(classOf[DoIt], doIt)

  def doIt(msg: AnyRef, rf: Any => Unit) {
    val block = Block(new Mailbox)
    block.setSystemServices(systemServices)
    val incDesString = IncDesString(null)
    val chain = new Chain
    chain.add(block, Set(null, incDesString))
    chain.add(incDesString, Set(null, "abc"))
    this(chain) {
      rsp => {
        incDesString(Value())(rf)
      }
    }
  }
}

class BlockIOComponentFactory extends ComponentFactory {
  addDependency(classOf[RandomIOComponentFactory])
  addDependency(classOf[BlocksComponentFactory])
}

class BlockIOTest extends SpecificationWithJUnit {
  "BlockIOTest" should {
    "read and write" in {
      val dbName = "BlockIOTest.db"
      val file = new java.io.File(dbName)
      file.delete
      val properties = new Properties
      properties.put("dbPathname", dbName)
      val systemServices = SystemServices(new BlockIOComponentFactory, properties = properties)
      val driver = new Driver
      driver.setMailbox(new Mailbox)
      driver.setSystemServices(systemServices)
      println(Future(driver, DoIt()))
    }
  }
}
