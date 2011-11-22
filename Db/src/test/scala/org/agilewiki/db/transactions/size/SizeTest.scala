package org.agilewiki
package db
package transactions
package size

import blip._
import bind._
import services._
import incDes._
import org.specs.SpecificationWithJUnit

class SizeTest extends SpecificationWithJUnit {
  "SizeTest" should {
    "get size" in {
      val systemServices = SystemServices(new ServicesRootComponentFactory)
      val dbName = "SizeNoLog.db"
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
      chain.op(db, SetRequest(db, "/$", IncDesLongIncDesMap(null, db)))
      chain.op(db, SetRequest(db, "/$/-111111111111111111",
        IncDesIncDes(null)))
      chain.op(db, SetRequest(db, "/$/-111111111111111111/$",
        IncDesStringSet(null, db)))
      chain.op(db, SetRequest(db, "/$/0",
        IncDesIncDes(null)))
      chain.op(db, SetRequest(db, "/$/0/$",
        IncDesBytesList(null, db)))
      chain.op(db, SizeRequest(db, "/$"), "mapSize")
      chain.op(db, SizeRequest(db, "/$/-111111111111111111/$"), "setSize")
      chain.op(db, SizeRequest(db, "/$/0/$"), "listSize")
      Future(systemServices, chain)
      println(chain.results)
      systemServices.close
    }
  }
}