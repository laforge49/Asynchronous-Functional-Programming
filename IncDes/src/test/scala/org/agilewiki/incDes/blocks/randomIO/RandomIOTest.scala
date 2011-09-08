package org.agilewiki
package incDes
package blocks
package blockCache

import blip._
import org.specs.SpecificationWithJUnit

class RandomIOTest extends SpecificationWithJUnit {
  "RandomIOTest" should {
    "read and write" in {
      val properties = new java.util.TreeMap[String, String]
      properties.put("dbPathname", "RandomIOTest.db")
      val systemServices = SystemServices(new RandomIOComponentFactory, properties = properties)
    }
  }
}