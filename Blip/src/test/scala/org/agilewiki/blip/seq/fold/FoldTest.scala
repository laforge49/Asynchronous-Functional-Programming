package org.agilewiki.blip
package seq
package fold

import org.specs.SpecificationWithJUnit

class FoldTest extends SpecificationWithJUnit {
  "FoldTest" should {
    "sum" in {
      println(Future(Range(1,4), Fold(0, (a: Int, b: Int) => a + b)))
    }
  }
}
