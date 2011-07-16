package org.agilewiki.blip
package seq
package find

import org.specs.SpecificationWithJUnit

class FindTest extends SpecificationWithJUnit {
  "FindTest" should {
    "find" in {
      println(Future(Range(1,4), Find((a: Int) => a % 2 == 0)))
      println(Future(Range(1,4), Find((a: Int) => a % 4 == 0)))
    }
  }
}
