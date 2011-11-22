package org.agilewiki.blip
package seq
package find

import org.specs.SpecificationWithJUnit
import bind._

class FindTest extends SpecificationWithJUnit {
  "FindTest" should {
    "find" in {
      println(Future(new Range(1,4), Find((a: Int) => a % 2 == 0)))
      println(Future(new Range(1,4), Find((a: Int) => a % 4 == 0)))
    }
  }
}
