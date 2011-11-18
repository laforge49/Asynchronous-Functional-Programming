package org.agilewiki
package db
package transactions
package swift
package swiftRecords

import blip._
import bind._
import services._
import seq._
import incDes._
import records._
import log._
import dbSeq._
import batch._
import org.specs.SpecificationWithJUnit

class SwiftRecordsTest extends SpecificationWithJUnit {
  "SwiftRecordsTest" should {
    "create an empty swift records datastore" in {
      val systemServices = SystemServices(new ServicesRootComponentFactory)
      val dbName = "swiftRecords.db"
      val logDirPathname = "swiftRecords"
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
      Future(systemServices, chain)
      systemServices.close
    }
    "process an empty batch" in {
      val systemServices = SystemServices(new ServicesRootComponentFactory)
      val dbName = "swiftRecords.db"
      val logDirPathname = "swiftRecords"
      val properties = new Properties
      properties.put("dbPathname", dbName)
      properties.put("logDirPathname", logDirPathname)
      val db = Subsystem(
        systemServices,
        new SwiftComponentFactory,
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
      val dbName = "swiftRecords.db"
      val logDirPathname = "swiftRecords"
      val properties = new Properties
      properties.put("dbPathname", dbName)
      properties.put("logDirPathname", logDirPathname)
      val db = Subsystem(
        systemServices,
        new SwiftComponentFactory,
        properties = properties,
        actorId = ActorId("db"))
      val batch = Batch(db)
      val chain = new Chain
      chain.op(systemServices, Register(db))
      chain.op(db, NewRecord(batch, "fun"))
      chain.op(db, NewRecord(batch, "games"))
      chain.op(db, TransactionRequest(batch), "timestamp")
      chain.op(db, RecordsCount(db), "records count")
      chain.op(db, RecordGet(db, null, "fun", ""), "fun")
      chain.op(db, RecordGet(db, null, "games", ""), "games")
      Future(systemServices, chain)
      println(chain.results)
      systemServices.close
    }
    "Update records" in {
      val systemServices = SystemServices(new ServicesRootComponentFactory)
      val dbName = "swiftRecords.db"
      val logDirPathname = "swiftRecords"
      val properties = new Properties
      properties.put("dbPathname", dbName)
      properties.put("logDirPathname", logDirPathname)
      val db = Subsystem(
        systemServices,
        new SwiftComponentFactory,
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
      chain.op(db, RecordGet(db, null, "fun", "$"), "funContent")
      chain.op(Unit => chain("funContent"), Value(), "funValue")
      chain.op(db, RecordGet(db, null, "games", "$"), "gamesContent")
      chain.op(Unit => chain("gamesContent"), Value(), "gamesValue")
      Future(systemServices, chain)
      println(chain.results)
      systemServices.close
    }
    "RecordsSeq" in {
      val systemServices = SystemServices(new ServicesRootComponentFactory)
      val dbName = "swiftRecords.db"
      val logDirPathname = "swiftRecords"
      val properties = new Properties
      properties.put("dbPathname", dbName)
      properties.put("logDirPathname", logDirPathname)
      val db = Subsystem(
        systemServices,
        new SwiftComponentFactory,
        properties = properties,
        actorId = ActorId("db"))
      Future(systemServices, Register(db))
      val recordsSeq = new RecordsSeq(db)
      Future(recordsSeq, Loop((key: String, value: Record) => println(key + "->" + value)))
      systemServices.close
    }
    "RecordIntSeq" in {
      val systemServices = SystemServices(new ServicesRootComponentFactory)
      val dbName = "swiftRecords.db"
      val logDirPathname = "swiftRecords"
      val properties = new Properties
      properties.put("dbPathname", dbName)
      properties.put("logDirPathname", logDirPathname)
      val db = Subsystem(
        systemServices,
        new SwiftComponentFactory,
        properties = properties,
        actorId = ActorId("db"))
      val gamesContent = IncDesIntIncDesMap(null, db)
      val gamesSeq = new RecordIntSeq(db, "games", "$")
      val batch = Batch(db)
      val chain = new Chain
      chain.op(systemServices, Register(db))
      chain.op(db, NewRecord(batch, "games"))
      chain.op(gamesContent, PutString(null, 1, "Chess"))
      chain.op(gamesContent, PutString(null, 2, "Checkers"))
      chain.op(gamesContent, PutString(null, 3, "Spider"))
      chain.op(db, RecordUpdate(batch, "games", "$", gamesContent))
      chain.op(db, TransactionRequest(batch))
      chain.op(db, RecordSize(db, null, "games", "$"), "gamesSize")
      chain.op(db, RecordSize(db, null, "games", "$/4"), "unknownSize")
      chain.op(gamesSeq, LoopSafe(PrintIntStringMap()))
      Future(systemServices, chain)
      println(chain.results)
      systemServices.close
    }
    "Delete record" in {
      val systemServices = SystemServices(new ServicesRootComponentFactory)
      val dbName = "swiftRecords.db"
      val logDirPathname = "swiftRecords"
      val properties = new Properties
      properties.put("dbPathname", dbName)
      properties.put("logDirPathname", logDirPathname)
      val db = Subsystem(
        systemServices,
        new SwiftComponentFactory,
        properties = properties,
        actorId = ActorId("db"))
      val batch = Batch(db)
      val chain = new Chain
      chain.op(systemServices, Register(db))
      chain.op(db, DeleteRecord(batch, "fun"))
      chain.op(db, TransactionRequest(batch), "timestamp")
      chain.op(db, RecordsCount(db), "record count")
      chain.op(db, RecordExists(db, null, "fun", ""), "funExists")
      chain.op(db, RecordExists(db, null, "games", ""), "gamesExists")
      Future(systemServices, chain)
      println(chain.results)
      systemServices.close
    }
    "recover" in {
      val systemServices = SystemServices(new ServicesRootComponentFactory)
      val dbName = "swiftRecords.db"
      val logDirPathname = "swiftRecords"
      val file = new java.io.File(dbName)
      file.delete
      val properties = new Properties
      properties.put("dbPathname", dbName)
      properties.put("logDirPathname", logDirPathname)
      val db = Subsystem(
        systemServices,
        new SwiftRecoveryComponentFactory,
        properties = properties,
        actorId = ActorId("db"))
      val chain = new Chain
      chain.op(systemServices, Register(db))
      chain.op(db, Recover())
      Future(systemServices, chain)
      systemServices.close
    }
    "Check recovery" in {
      val systemServices = SystemServices(new ServicesRootComponentFactory)
      val dbName = "swiftRecords.db"
      val logDirPathname = "swiftRecords"
      val properties = new Properties
      properties.put("dbPathname", dbName)
      properties.put("logDirPathname", logDirPathname)
      val db = Subsystem(
        systemServices,
        new SwiftComponentFactory,
        properties = properties,
        actorId = ActorId("db"))
      val gamesSeq = new RecordIntSeq(db, "games", "$")
      val chain = new Chain
      chain.op(systemServices, Register(db))
      chain.op(db, RecordsCount(db), "record count")
      chain.op(db, RecordExists(db, null, "fun", ""), "funExists")
      chain.op(db, RecordExists(db, null, "games", ""), "gamesExists")
      chain.op(db, RecordSize(db, null, "games", "$"), "gamesSize")
      chain.op(gamesSeq, LoopSafe(PrintIntStringMap()))
      Future(systemServices, chain)
      println(chain.results)
      systemServices.close
    }
  }
}

case class PrintIntStringMap() extends MessageLogic {
  override def func(target: BindActor, msg: AnyRef, rf: Any => Unit)(implicit sender: ActiveActor) {
    val nvPair = msg.asInstanceOf[KVPair[Int, IncDesIncDes]]
    nvPair.value(Value()) {
      rsp => {
        rsp.asInstanceOf[Actor](Value()) {
          rsp2 => {
            println(nvPair.key + " -> " + rsp2)
            rf(true)
          }
        }
      }
    }
  }
}
