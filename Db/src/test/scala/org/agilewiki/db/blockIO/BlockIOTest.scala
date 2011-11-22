package org.agilewiki
package db
package blockIO

import blip._
import bind._
import services._
import incDes._
import blocks._
import org.specs.SpecificationWithJUnit

case class Readit()

case class Writeit()

class Driver extends Actor {
  bind(classOf[Readit], readit)
  bind(classOf[Writeit], writeit)

  def writeit(msg: AnyRef, rf: Any => Unit) {
    val block = Block(exchangeMessenger)
    block.setSystemServices(systemServices)
    val incDesString = IncDesString(null)
    val blockLength = IncDesInt(null)
    val results = new Results
    val chain = new Chain(results)
    chain.op(block, Set(null, incDesString))
    chain.op(incDesString, Set(null, "abc"))
    chain.op(block, Bytes(), "bytes")
    chain.op(blockLength,
      Unit => {
        val blkLen = results("bytes").asInstanceOf[Array[Byte]].length
        Set(null, blkLen)
      })
    chain.op(blockLength, Bytes(), "header")
    chain.op(systemServices,
      Unit => WriteBytes(0L, results("header").asInstanceOf[Array[Byte]]))
    chain.op(systemServices,
      Unit => WriteBytes(4L, results("bytes").asInstanceOf[Array[Byte]]))
    this(chain)(rf)
  }

  def readit(msg: AnyRef, rf: Any => Unit) {
    val block = Block(exchangeMessenger)
    block.setSystemServices(systemServices)
    val blockLength = IncDesInt(null)
    val results = new Results
    val chain = new Chain(results)
    chain.op(systemServices, ReadBytes(0L, 4), "header")
    chain.op(blockLength,
      Unit => {
        blockLength.load(results("header").asInstanceOf[Array[Byte]])
        Value()
      }, "length")
    chain.op(systemServices,
      Unit => {
        val blkLen = results("length").asInstanceOf[Int]
        ReadBytes(4L, blkLen)
      }, "bytes")
    chain.op(block,
      Unit => {
        block.load(results("bytes").asInstanceOf[Array[Byte]])
        Value()
      }, "incDesString")
    chain.op(Unit => results("incDesString").asInstanceOf[Actor], Value())
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
      driver.setExchangeMessenger(systemServices.exchangeMessenger)
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
      driver.setExchangeMessenger(systemServices.exchangeMessenger)
      driver.setSystemServices(systemServices)
      Future(driver, Readit()) must be equalTo("abc")
      systemServices.close
    }
  }
}
