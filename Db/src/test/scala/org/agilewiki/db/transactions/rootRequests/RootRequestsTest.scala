package org.agilewiki
package db
package transactions
package rootRequests

import blip._
import services._
import incDes._
import org.specs.SpecificationWithJUnit

class RootRequestsTest extends SpecificationWithJUnit {
  "SmallNoLogStringDataStoreTest" should {
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
      chain.op(db, SetRootRequest.process(db, IncDesInt(null)), "timestamp")
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
      val results = new Results
      val chain = new Chain(results)
      chain.op(systemServices, Register(db))
      chain.op(db, GetRootRequest.process(db), "IncDesInt")
      Future(systemServices, chain)
      println(results)
      systemServices.close
    }
  }
}