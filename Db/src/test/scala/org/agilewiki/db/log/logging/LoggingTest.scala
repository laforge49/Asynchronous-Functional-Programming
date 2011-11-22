package org.agilewiki
package db
package log
package logging

import blip._
import bind._
import services._
import incDes._
import db.transactions._
import org.specs.SpecificationWithJUnit

class LoggingTest extends SpecificationWithJUnit {
  "LoggingTest" should {
    "log" in {
      val systemServices = SystemServices(new ServicesRootComponentFactory)
      val dbName = "smallLogging.db"
      val logDirPathname = "smallLogging"
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
  }
}