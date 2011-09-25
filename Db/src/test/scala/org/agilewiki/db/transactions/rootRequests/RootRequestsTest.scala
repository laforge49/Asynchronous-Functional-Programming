package org.agilewiki
package db
package transactions
package rootRequests

import blip._
import services._
import incDes._
import org.specs.SpecificationWithJUnit

class RootRequestsTest extends SpecificationWithJUnit {
  "RootRequestsTest" should {
    "update" in {
      val systemServices = SystemServices(new ServicesRootComponentFactory)
      val dbName = "SmallNoLog.db"
      val file = new java.io.File(dbName)
      file.delete
      val properties = new Properties
      properties.put("dbPathname", dbName)
      val db = Subsystem(
        systemServices,
        new SmallNoLogComponentFactory,
        properties = properties,
        actorId = ActorId("db"))
      val results = new Results
      val chain = new Chain(results)
      chain.op(systemServices, Register(db))
      chain.op(db, SetRequest.process(db, "/$", IncDesInt(null)), "timestamp")
      Future(systemServices, chain)
      println(results)
      systemServices.close
    }
    "query" in {
      val systemServices = SystemServices(new ServicesRootComponentFactory)
      val dbName = "SmallNoLog.db"
      val properties = new Properties
      properties.put("dbPathname", dbName)
      val db = Subsystem(
        systemServices,
        new SmallNoLogComponentFactory,
        properties = properties,
        actorId = ActorId("db"))
      val chain = new Chain
      chain.op(systemServices, Register(db))
      chain.op(db, GetRequest.process(db, "/"), "root")
      chain.op(db, GetRequest.process(db, "/$"), "IncDesInt")
      chain.op(db, SetRequest.process(db, "/$", null), "timestamp")
      chain.op(db, GetRequest.process(db, "/$"), "null")
      Future(systemServices, chain)
      println(chain.results)
      systemServices.close
    }
  }
}