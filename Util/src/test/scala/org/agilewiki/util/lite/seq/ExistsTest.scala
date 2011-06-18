package org.agilewiki
package util
package lite
package seq

import org.specs.SpecificationWithJUnit

class ExistsTest extends SpecificationWithJUnit {
  "Exists" should {

    "test for even" in {
      val list = new java.util.ArrayList[Int]
      list.add(1)
      list.add(2)
      list.add(3)
      var seq = new LiteListSeq(null, list)
      FutureSeq(seq).exists((x: Int) => x % 2 == 0) must be equalTo(true)
      FutureSeq(seq).exists((x: Int) => x % 4 == 0) must be equalTo(false)
    }
  }
}
