package org.agilewiki
package db
package transactions
package swift
package swiftTimings

import blip._
import seq._
import services._
import log._
import incDes._
import batch._
import org.specs.SpecificationWithJUnit

case class DoIt()

class Driver extends Actor {
  bind(classOf[DoIt], doit)

  def doit(msg: AnyRef, rf: Any => Unit) {
    val range = new Range(0, 10)
    range.setMailbox(mailbox)
    range.setSystemServices(systemServices)
    range(LoopSafe(Looper()))(rf)
  }
}

case class Looper() extends Safe {
  override def func(target: Actor, msg: AnyRef, rf: Any => Unit)(implicit sender: ActiveActor) {
    val systemServices = target.systemServices
    val batch = Batch(systemServices)
    val chain = new Chain
    chain.op(systemServices, Unit => RecordUpdate(batch, "r1", "$", IncDesInt(null)))
    chain.op(systemServices, TransactionRequest(batch))
    target(chain) {
      rsp => rf(true)
    }
  }
}

class SwiftTimingsTest extends SpecificationWithJUnit {
  "SwiftTimingsTest" should {
    "time swift" in {
      val systemServices = SystemServices(new ServicesRootComponentFactory)
      val dbName = "swiftTimings.db"
      val logDirPathname = "swiftTimings"
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
      val driver = new Driver
      driver.setMailbox(db.mailbox)
      driver.setSystemServices(db)
      val batch = Batch(db)
      val chain = new Chain
      chain.op(systemServices, Register(db))
      chain.op(db, NewRecord(batch, "r1"))
      chain.op(db, TransactionRequest(batch))
      chain.op(driver, DoIt())
      Future(systemServices, chain)
      systemServices.close
    }
  }
}
