package org.agilewiki
package incDes
package blocks
package simpleNoLogDataStore

import blip._
import services._
import org.specs.SpecificationWithJUnit

class SimpleNoLogDataStoreTest extends SpecificationWithJUnit {
  "SimpleNoLogDataStoreTest" should {
    "update & query" in {
      val systemServices = SystemServices(new SimpleNoLogDataStoreComponentFactory)
      val dbName = "SimpleNoLog.db"
      val file = new java.io.File(dbName)
      file.delete
      val properties = new Properties
      properties.put("dbPathname", dbName)
      SimpleNoLogDataStoreSubsystem(systemServices, properties = properties)
    }
  }
}