package org.agilewiki.blip
package seq
package exists

import org.specs.SpecificationWithJUnit

class ExistsTest extends SpecificationWithJUnit {
  "FoldTest" should {
    "sum" in {
      println(Future(Range(1,4), Exists((a: Int) => a == 3)))
      println(Future(Range(1,4), Exists((a: Int) => a == 4)))
    }
  }
}
