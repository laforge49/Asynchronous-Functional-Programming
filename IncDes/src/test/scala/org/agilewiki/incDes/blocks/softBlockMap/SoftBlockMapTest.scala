package org.agilewiki
package incDes
package blocks
package softBlockMap

import org.specs.SpecificationWithJUnit

class SoftBlockMapTest extends SpecificationWithJUnit {
  "BlockMapTest" should {
    "not overflow memory" in {
      val softBlockMap = new SoftBlockMap(5)
      var i = 0
      val l=1000//000
      while (i < l) {
        i += 1
        val b = new Block
        b.partness(null, i, null)
        softBlockMap.add(b)
      }
    }
  }
}