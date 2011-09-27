package org.agilewiki
package db
package transactions
package list

import blip._
import services._
import incDes._
import org.specs.SpecificationWithJUnit

class ListTest extends SpecificationWithJUnit {
  "ListTest" should {
    "query" in {
      val systemServices = SystemServices(new ServicesRootComponentFactory)
      val dbName = "StringListNoLog.db"
      val file = new java.io.File(dbName)
      file.delete
      val properties = new Properties
      properties.put("dbPathname", dbName)
      val db = Subsystem(
        systemServices,
        new SmallNoLogComponentFactory,
        properties = properties,
        actorId = ActorId("db"))
      val list = IncDesStringList(null, db)
      val ids0 = IncDesString(null)
      val chain = new Chain
      chain.op(systemServices, Register(db))
      chain.op(list, Add[IncDesString, String](null, ids0))
      //chain.op(ids0, Set(null, ""))
      chain.op(db, SetRequest.process(db, "/$", list))
      chain.op(db, GetRequest.process(db, "/$/0"), "0")
      chain.op(db, GetRequest.process(db, "/$/1"), "1")
      Future(systemServices, chain)
      println(chain.results)
      systemServices.close
    }
  }
}