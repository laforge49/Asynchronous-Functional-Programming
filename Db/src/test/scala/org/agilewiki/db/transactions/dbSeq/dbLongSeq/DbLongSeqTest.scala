package org.agilewiki
package db
package transactions
package dbSeq
package dbLongSeq

import blip._
import bind._
import services._
import seq._
import incDes._
import org.specs.SpecificationWithJUnit

class DbLongSeqTest extends SpecificationWithJUnit {
  "DbLongSeqTest" should {
    "sequence" in {
      val systemServices = SystemServices(new ServicesRootComponentFactory)
      val dbName = "DbLongSeqNoLog.db"
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
      val dbLongSeq = new DbLongSeq[IncDesString](db, "/$")
      val chain = new Chain
      chain.op(systemServices, Register(db))
      chain.op(db, SetRequest(db, "/$", IncDesLongStringMap(null, db)))
      chain.op(one, Set(null, "One"))
      chain.op(db, SetRequest(db, "/$/1", one))
      chain.op(two, Set(null, "Two"))
      chain.op(db, SetRequest(db, "/$/2", two))
      chain.op(db, SetRequest(db, "/$/2", null))
      chain.op(three, Set(null, "Three"))
      chain.op(db, SetRequest(db, "/$/3", three))
      chain.op(ten, Set(null, "Ten"))
      chain.op(db, SetRequest(db, "/$/10", ten))
      chain.op(dbLongSeq, First(), "a")
      chain.op(dbLongSeq, Current(3L), "b")
      chain.op(dbLongSeq, Next(3L), "c")
      Future(systemServices, chain)
      println(chain.results)
      systemServices.close
    }
  }
}