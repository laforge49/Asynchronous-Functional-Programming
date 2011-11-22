package org.agilewiki
package db
package transactions
package rootString
package simpleNoLogStringDataStore

import blip._
import bind._
import services._
import org.specs.SpecificationWithJUnit

class SimpleNoLogStringDataStoreTest extends SpecificationWithJUnit {
  "SimpleNoLogStringDataStoreTest" should {
    "update & query" in {
      val systemServices = SystemServices(new ServicesRootComponentFactory)
      val dbName = "SimpleNoLogString.db"
      val file = new java.io.File(dbName)
      file.delete
      val properties = new Properties
      properties.put("dbPathname", dbName)
      val db = Subsystem(
        systemServices,
        new SimpleNoLogStringDataStoreComponentFactory,
        properties = properties,
        actorId = ActorId("db"))
      val results = new Results
      val chain = new Chain(results)
      chain.op(systemServices, Register(db))
      chain.op(db, SetRootStringRequest.process(db, "Hello world!"), "timestamp")
      chain.op(db, GetRootStringRequest.process(db), "string")
      Future(systemServices, chain)
      println(results)
      systemServices.close
    }
  }
}