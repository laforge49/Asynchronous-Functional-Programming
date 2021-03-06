package org.agilewiki
package db
package transactions
package getSet
package navSet

import blip._
import bind._
import services._
import incDes._
import org.specs.SpecificationWithJUnit

class NavSetTest extends SpecificationWithJUnit {
  "NavSetTest" should {
    "update & query" in {
      val systemServices = SystemServices(new ServicesRootComponentFactory)
      val dbName = "IntStringSetNoLog.db"
      val file = new java.io.File(dbName)
      file.delete
      val properties = new Properties
      properties.put("dbPathname", dbName)
      val db = Subsystem(
        systemServices,
        new SmallNoLogComponentFactory,
        properties = properties,
        actorId = ActorId("db"))
      val tru = IncDesBoolean(null)
      val fls = IncDesBoolean(null)
      val chain = new Chain
      chain.op(systemServices, Register(db))
      chain.op(db, SetRequest(db, "/$", IncDesStringSet(null, db)))
      chain.op(tru, Set(null, true))
      chain.op(fls, Set(null, false))
      chain.op(db, SetRequest(db, "/$/1", tru))
      chain.op(db, SetRequest(db, "/$/2", tru))
      chain.op(db, SetRequest(db, "/$/2", fls))
      chain.op(db, SetRequest(db, "/$/3", tru))
      chain.op(db, SetRequest(db, "/$/10", tru))
      chain.op(db, GetRequest(db, "/$/1"), "one")
      chain.op(Unit => chain("one"), Value(), "1")
      chain.op(db, GetRequest(db, "/$/2"), "two")
      chain.op(Unit => chain("two"), Value(), "2")
      Future(systemServices, chain)
      println(chain.results)
      systemServices.close
    }
  }
}