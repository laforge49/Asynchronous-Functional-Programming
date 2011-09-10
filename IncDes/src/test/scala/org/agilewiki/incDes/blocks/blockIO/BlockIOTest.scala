package org.agilewiki
package incDes
package blocks
package blockIO

import blip._
import services._
import org.specs.SpecificationWithJUnit

case class Readit()

case class Writeit()

class Driver extends Actor {
  bind(classOf[Readit], readit)
  bind(classOf[Writeit], writeit)

  def writeit(msg: AnyRef, rf: Any => Unit) {
    val block = Block(new Mailbox)
    block.setSystemServices(systemServices)
    val incDesString = IncDesString(null)
    val blockLength = IncDesInt(null)
    val results = new Results
    val chain = new Chain(results)
    chain.add(block, Set(null, incDesString))
    chain.add(incDesString, Set(null, "abc"))
    chain.add(block, Bytes(), "bytes")
    chain.addFuncs(Unit => blockLength,
      Unit => {
        val blkLen = results("bytes").asInstanceOf[Array[Byte]].length
        Set(null, blkLen)
      })
    chain.add(blockLength, Bytes(), "header")
    chain.addFuncs(Unit => systemServices,
      Unit => WriteBytes(0L, results("header").asInstanceOf[Array[Byte]]))
    chain.addFuncs(Unit => systemServices,
      Unit => WriteBytes(4L, results("bytes").asInstanceOf[Array[Byte]]))
    this(chain)(rf)
  }

  def readit(msg: AnyRef, rf: Any => Unit) {
    val block = Block(new Mailbox)
    block.setSystemServices(systemServices)
    val blockLength = IncDesInt(null)
    val results = new Results
    val chain = new Chain(results)
    chain.add(systemServices, ReadBytes(0L, 4), "header")
    chain.addFuncs(Unit => blockLength,
      Unit => {
        blockLength.load(results("header").asInstanceOf[Array[Byte]])
        Value()
      }, "length")
    chain.addFuncs(Unit => systemServices,
      Unit => {
        val blkLen = results("length").asInstanceOf[Int]
        ReadBytes(4L, blkLen)
      }, "bytes")
    chain.addFuncs(Unit => block,
      Unit => {
        block.load(results("bytes").asInstanceOf[Array[Byte]])
        Value()
      }, "incDesString")
    chain.addFuncs(Unit => results("incDesString").asInstanceOf[Actor], Unit => Value())
    this(chain)(rf)
  }
}

class BlockIOComponentFactory extends ComponentFactory {
  addDependency(classOf[RandomIOComponentFactory])
  addDependency(classOf[BlocksComponentFactory])
}

class BlockIOTest extends SpecificationWithJUnit {
  "BlockIOTest" should {
    "write" in {
      val dbName = "BlockIOTest.db"
      val file = new java.io.File(dbName)
      file.delete
      val properties = new Properties
      properties.put("dbPathname", dbName)
      val systemServices = SystemServices(new BlockIOComponentFactory, properties = properties)
      val driver = new Driver
      driver.setMailbox(new Mailbox)
      driver.setSystemServices(systemServices)
      Future(driver, Writeit())
      systemServices.close
    }
    "read" in {
      val dbName = "BlockIOTest.db"
      val properties = new Properties
      properties.put("dbPathname", dbName)
      val systemServices = SystemServices(new BlockIOComponentFactory, properties = properties)
      val driver = new Driver
      driver.setMailbox(new Mailbox)
      driver.setSystemServices(systemServices)
      Future(driver, Readit()) must be equalTo("abc")
      systemServices.close
    }
  }
}
