package org.agilewiki
package db
package transactions
package batch
package smallRecords

import blip._
import services._
import seq._
import incDes._
import records._
import log._
import dbSeq._
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
    "Create two records" in {
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
      chain.op(db, NewRecord(batch, "games"))
      chain.op(db, TransactionRequest(batch), "timestamp")
      chain.op(db, RecordsCount(db), "records count")
      Future(systemServices, chain)
      println(chain.results)
      systemServices.close
    }
    "Update records" in {
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
      val funContent = IncDesInt(null)
      val gamesContent = IncDesString(null)
      val batch = Batch(db)
      val chain = new Chain
      chain.op(systemServices, Register(db))
      chain.op(funContent, Set(null, 42))
      chain.op(gamesContent, Set(null, "Checkers"))
      chain.op(db, RecordUpdate(batch, "fun", "$", funContent))
      chain.op(db, RecordUpdate(batch, "games", "$", gamesContent))
      chain.op(db, TransactionRequest(batch), "timestamp")
      chain.op(db, RecordsCount(db), "records count")
      chain.op(db, GetRequest(db, "/$/fun/$"), "funContent")
      chain.op(Unit => chain("funContent"), Value(), "funValue")
      chain.op(db, GetRequest(db, "/$/games/$"), "gamesContent")
      chain.op(Unit => chain("gamesContent"), Value(), "gamesValue")
      Future(systemServices, chain)
      println(chain.results)
      systemServices.close
    }
    "RecordsSeq" in {
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
      Future(systemServices, Register(db))
      val recordsSeq = new RecordsSeq(db)
      Future(recordsSeq, Loop((key: String, value: Record) => println(key + "->" + value)))
      systemServices.close
    }
    "Delete record" in {
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
      chain.op(db, DeleteRecord(batch, "fun"))
      chain.op(db, TransactionRequest(batch), "timestamp")
      chain.op(db, RecordsCount(db), "record count")
      chain.op(db, GetRequest(db, "/$/fun"), "fun")
      chain.op(db, GetRequest(db, "/$/games"), "games")
      Future(systemServices, chain)
      println(chain.results)
      systemServices.close
    }
  }
}