package org.agilewiki
package db
package transactions
package batch
package smallRecords

import blip._
import blip.services._
import log._
import org.specs.SpecificationWithJUnit

class SmallRecordsTest extends SpecificationWithJUnit {
  "SmallRecordsTest" should {
    "create an empty small records datastore" in {
      val systemServices = SystemServices(new ServicesRootComponentFactory)
      val dbName = "smallRecords.db"
      val logDirPathname = "smallRecords"
      val file = new java.io.File(dbName)
      file.delete
      EmptyLogDirectory(logDirPathname)
      val properties = new Properties
      properties.put("dbPathname", dbName)
      properties.put("logDirPathname", logDirPathname)
      properties.put("flushLog", "true")
      val db = Subsystem(
        systemServices,
        new SmallRecordsComponentFactory,
        properties = properties,
        actorId = ActorId("db"))
      val chain = new Chain
      chain.op(systemServices, Register(db))
      Future(systemServices, chain)
      systemServices.close
    }
    "process an empty batch" in {
      val systemServices = SystemServices(new ServicesRootComponentFactory)
      val dbName = "smallRecords.db"
      val logDirPathname = "smallRecords"
      val properties = new Properties
      properties.put("dbPathname", dbName)
      properties.put("logDirPathname", logDirPathname)
      properties.put("flushLog", "true")
      val db = Subsystem(
        systemServices,
        new SmallRecordsComponentFactory,
        properties = properties,
        actorId = ActorId("db"))
      val batch = Batch(db)
      val chain = new Chain
      chain.op(systemServices, Register(db))
      chain.op(db, TransactionRequest(batch))
      println(Future(systemServices, chain))
      systemServices.close
    }
    "Create a record" in {
      val systemServices = SystemServices(new ServicesRootComponentFactory)
      val dbName = "smallRecords.db"
      val logDirPathname = "smallRecords"
      val properties = new Properties
      properties.put("dbPathname", dbName)
      properties.put("logDirPathname", logDirPathname)
      properties.put("flushLog", "true")
      val db = Subsystem(
        systemServices,
        new SmallRecordsComponentFactory,
        properties = properties,
        actorId = ActorId("db"))
      val batch = Batch(db)
      val chain = new Chain
      chain.op(systemServices, Register(db))
      chain.op(db, NewRecord(batch, "fun"))
      chain.op(db, TransactionRequest(batch), "timestamp")
      chain.op(db, SizeRequest(db, "/$"), "record count")
      Future(systemServices, chain)
      println(chain.results)
      systemServices.close
    }
  }
}