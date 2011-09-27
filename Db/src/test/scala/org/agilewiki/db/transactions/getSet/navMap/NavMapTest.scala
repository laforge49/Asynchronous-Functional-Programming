package org.agilewiki
package db
package transactions
package getSet
package navMap

import blip._
import services._
import incDes._
import org.specs.SpecificationWithJUnit

class NavMapTest extends SpecificationWithJUnit {
  "NavMapTest" should {
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
      val one = IncDesString(null)
      val two = IncDesString(null)
      val three = IncDesString(null)
      val ten = IncDesString(null)
      val chain = new Chain
      chain.op(systemServices, Register(db))
      chain.op(db, SetRequest.process(db, "/$", IncDesIntStringMap(null, db)))
      chain.op(one, Set(null, "One"))
      chain.op(db, SetRequest.process(db, "/$/1", one))
      chain.op(two, Set(null, "Two"))
      chain.op(db, SetRequest.process(db, "/$/2", two))
      chain.op(db, SetRequest.process(db, "/$/2", null))
      chain.op(three, Set(null, "Three"))
      chain.op(db, SetRequest.process(db, "/$/3", three))
      chain.op(ten, Set(null, "Ten"))
      chain.op(db, SetRequest.process(db, "/$/10", ten))
      chain.op(db, GetRequest.process(db, "/$/1"), "one")
      chain.op(db, GetRequest.process(db, "/$/2"), "two")
      chain.op(db, GetRequest.process(db, "/$/3"), "three")
      chain.op(db, GetRequest.process(db, "/$/10"), "ten")
      Future(systemServices, chain)
      println(chain.results)
      systemServices.close
    }
  }
}