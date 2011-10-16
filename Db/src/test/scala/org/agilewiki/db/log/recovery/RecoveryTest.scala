package org.agilewiki
package db
package log
package recovery

import blip._
import services._
import incDes._
import db.transactions._
import org.specs.SpecificationWithJUnit

class RecoveryTest extends SpecificationWithJUnit {
  "RecoveryTest" should {
    "log" in {
      val systemServices = SystemServices(new ServicesRootComponentFactory)
      val dbName = "smallrecovery.db"
      val logDirPathname = "smallRecovery"
      val file = new java.io.File(dbName)
      file.delete
      EmptyLogDirectory(logDirPathname)
      val properties = new Properties
      properties.put("dbPathname", dbName)
      properties.put("logDirPathname", logDirPathname)
      val db = Subsystem(
        systemServices,
        new SmallComponentFactory,
        properties = properties,
        actorId = ActorId("db"))
      val results = new Results
      val chain = new Chain(results)
      chain.op(systemServices, Register(db))
      chain.op(db, SetRequest(db, "/$", IncDesInt(null)))
      Future(systemServices, chain)
      systemServices.close
    }
    "log 2 more" in {
      val systemServices = SystemServices(new ServicesRootComponentFactory)
      val dbName = "smallrecovery.db"
      val logDirPathname = "smallRecovery"
      val properties = new Properties
      properties.put("dbPathname", dbName)
      properties.put("logDirPathname", logDirPathname)
      val db = Subsystem(
        systemServices,
        new SmallComponentFactory,
        properties = properties,
        actorId = ActorId("db"))
      val results = new Results
      val chain = new Chain(results)
      chain.op(systemServices, Register(db))
      chain.op(db, SetRequest(db, "/$", IncDesString(null)))
      chain.op(db, SetRequest(db, "/$", IncDesBytes(null)))
      Future(systemServices, chain)
      systemServices.close
    }
    "recover" in {
      val systemServices = SystemServices(new ServicesRootComponentFactory)
      val dbName = "smallrecovery.db"
      val logDirPathname = "smallRecovery"
      val file = new java.io.File(dbName)
      file.delete
      val properties = new Properties
      properties.put("dbPathname", dbName)
      properties.put("logDirPathname", logDirPathname)
      val db = Subsystem(
        systemServices,
        new SmallRecoveryComponentFactory,
        properties = properties,
        actorId = ActorId("db"))
      val results = new Results
      val chain = new Chain(results)
      chain.op(systemServices, Register(db))
      chain.op(db, Recover())
      Future(systemServices, chain)
      systemServices.close
    }
    "query" in {
      val systemServices = SystemServices(new ServicesRootComponentFactory)
      val dbName = "smallrecovery.db"
      val logDirPathname = "smallRecovery"
      val properties = new Properties
      properties.put("dbPathname", dbName)
      properties.put("logDirPathname", logDirPathname)
      val db = Subsystem(
        systemServices,
        new SmallComponentFactory,
        properties = properties,
        actorId = ActorId("db"))
      val results = new Results
      val chain = new Chain(results)
      chain.op(systemServices, Register(db))
      chain.op(db, GetRequest(db, "/$"))
      println(Future(systemServices, chain))
      systemServices.close
    }
  }
}