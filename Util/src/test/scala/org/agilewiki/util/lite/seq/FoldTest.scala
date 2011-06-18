package org.agilewiki
package util
package lite
package seq

import org.specs.SpecificationWithJUnit
class FoldTest extends SpecificationWithJUnit {
  "Fold" should {

    "sum" in {
      val list = new java.util.ArrayList[Int]
      list.add(1)
      list.add(2)
      list.add(3)
      var seq = new LiteListSeq(null, list)
      FutureSeq(seq).fold(0, (x: Int, y: Int) => (x + y)) must be equalTo 6
    }
  }
}
