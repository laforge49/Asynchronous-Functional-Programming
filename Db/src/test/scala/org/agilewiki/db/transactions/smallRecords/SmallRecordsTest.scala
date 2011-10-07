package org.agilewiki
package db
package transactions
package smallRecords

import blip._
import blip.services._
import incDes._
import log._
import org.specs.SpecificationWithJUnit

class SmallRecordsTest extends SpecificationWithJUnit {
  "SmallRecordsTest" should {
    "create an empty small records datastore" in {
      val systemServices = SystemServices(new ServicesRootComponentFactory)
      val dbName = "smallRecords.db"
      val logDirPathname = "smallRecords"
      val file = new java.io.File(dbName)
      file.delete
      EmptyLogDirectory(logDirPathname)
      val properties = new Properties
      properties.put("dbPathname", dbName)
      properties.put("logDirPathname", logDirPathname)
      properties.put("flushLog", "true")
      val db = Subsystem(
        systemServices,
        new SmallRecordsComponentFactory,
        properties = properties,
        actorId = ActorId("db"))
      val results = new Results
      val chain = new Chain(results)
      chain.op(systemServices, Register(db))
      Future(systemServices, chain)
      systemServices.close
    }
  }
}