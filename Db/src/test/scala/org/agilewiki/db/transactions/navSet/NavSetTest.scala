package org.agilewiki
package db
package transactions
package navSet

import blip._
import services._
import incDes._
import org.specs.SpecificationWithJUnit

class NavSetTest extends SpecificationWithJUnit {
  "NavSetTest" should {
    "update & query" in {
      val systemServices = SystemServices(new ServicesRootComponentFactory)
      val dbName = "IntStringMapNoLog.db"
      val file = new java.io.File(dbName)
      file.delete
      val properties = new Properties
      properties.put("dbPathname", dbName)
      val db = Subsystem(
        systemServices,
        new SmallNoLogComponentFactory,
        properties = properties,
        actorId = ActorId("db"))
      val chain = new Chain
      chain.op(systemServices, Register(db))
      chain.op(db, SetRequest.process(db, "/$", IncDesStringSet(null, db)))
      Future(systemServices, chain)
      println(chain.results)
      systemServices.close
    }
  }
}