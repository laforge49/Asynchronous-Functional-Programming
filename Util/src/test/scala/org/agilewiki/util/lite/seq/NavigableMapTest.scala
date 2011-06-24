package org.agilewiki
package util
package lite
package seq

import org.specs.SpecificationWithJUnit

class NavigableMapTest extends SpecificationWithJUnit {
  "Map Seq" should {

    "Pass the basic navigation test" in {
      var map = new java.util.TreeMap[String, String]
      map.put("Jane", "brunette")
      map.put("Sally", "blond")
      map.put("Sue", "redhead")
      var seq = new LiteNavigableMapSeq(new LiteReactor(null), map)
      FutureSeq(seq).firstMatch("Jane", "brunette") must be equalTo true
      FutureSeq(seq).currentMatch("Sally", "Sally", "blond") must be equalTo true
      FutureSeq(seq).nextMatch("Sally", "Sue", "redhead") must be equalTo true
      FutureSeq(seq).isCurrentEnd("Zander") must be equalTo true
    }
  }
}
