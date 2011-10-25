package org.agilewiki
package db
package transactions
package swift
package swiftLock

import blip._
import services._
import log._
import incDes._
import batch._
import org.specs.SpecificationWithJUnit

case class Counter(db: Actor) {
  def init = {
    val counter = IncDesInt(null)
    val batch = Batch(db)
    val chain = new Chain
    chain.op(db, RecordUpdate(batch, "r1", "$", counter))
    chain.op(db, TransactionRequest(batch))
    chain
  }

  def buildAdd(n: Int) = {
    var counter: IncDesInt = null
    val batch = Batch(db)
    val chain = new Chain
    chain.op(db, RecordGet(db, batch, "r1", "$"), "counter")
    chain.op(Unit => {
      counter = chain("counter").asInstanceOf[IncDesInt]
      counter
    }, Value(), "value")
    chain.op(Unit => counter, Unit => {
      val value = chain("value").asInstanceOf[Int]
      Set(null, value + n)
    })
    chain.op(db, Unit => RecordUpdate(batch, "r1", "$", counter))
    (chain, TransactionRequest(batch))
  }

  def get = {
    val chain = new Chain
    chain.op(db, RecordGet(db, null, "r1", "$"), "counter")
    chain.op(Unit => chain("counter").asInstanceOf[IncDesInt], Value())
    chain
  }
}

class SwiftLockTest extends SpecificationWithJUnit {
  "SwiftLockTest" should {
    "add 1 to counter" in {
      val systemServices = SystemServices(new ServicesRootComponentFactory)
      val dbName = "swiftLock.db"
      val logDirPathname = "swiftLock"
      val file = new java.io.File(dbName)
      file.delete
      EmptyLogDirectory(logDirPathname)
      val properties = new Properties
      properties.put("dbPathname", dbName)
      properties.put("logDirPathname", logDirPathname)
      val db = Subsystem(
        systemServices,
        new SwiftComponentFactory,
        properties = properties,
        actorId = ActorId("db"))
      val chain = new Chain
      chain.op(systemServices, Register(db))
      chain.op(db, Counter(db).init)
      chain.op(db, Counter(db).get, "oldCounterValue")
      val (b1, t1) = Counter(db).buildAdd(1)
      chain.op(db, b1)
      chain.op(db, t1)
      chain.op(db, Counter(db).get, "newCounterValue")
      Future(systemServices, chain)
      println(chain.results)
      systemServices.close
    }
    "force a conflict" in {
      val systemServices = SystemServices(new ServicesRootComponentFactory)
      val dbName = "swiftLock.db"
      val logDirPathname = "swiftLock"
      val properties = new Properties
      properties.put("dbPathname", dbName)
      properties.put("logDirPathname", logDirPathname)
      val db = Subsystem(
        systemServices,
        new SwiftComponentFactory,
        properties = properties,
        actorId = ActorId("db"))
      val chain = new Chain
      chain.op(systemServices, Register(db))
      val (b0, t0) = Counter(db).buildAdd(-1)
      chain.op(db, b0)
      chain.op(db, t0)
      chain.op(db, Counter(db).get, "resetCounterValue")
      val (b1, t1) = Counter(db).buildAdd(1)
      val (b2, t2) = Counter(db).buildAdd(1)
      chain.op(db, b1)
      chain.op(db, b2)
      chain.op(db, t1)
      Future(systemServices, chain)
      println(chain.results)
      try {
        println(Future(db, t2))
        println("can't get here")
      } catch {
        case ex: TransactionConflictException => {
          ex.printStackTrace
          println("retrying")
          val retry = new Chain
          val (b3, t3) = Counter(db).buildAdd(1)
          retry.op(db, b3)
          retry.op(db, t3)
          Future(db, retry)
          println("retry was successful")
        }
        case ex: Exception => throw ex
      }
      println(Future(db, Counter(db).get))
      systemServices.close
    }
  }
}
