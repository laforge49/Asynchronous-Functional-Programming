package org.agilewiki.blip
package seq
package range

import org.specs.SpecificationWithJUnit
import bind._

class RangeTest extends SpecificationWithJUnit {
  "Range" should {
    "support first, current and next" in {
      val range = new Range(4, 6)
      println(Future(range, First()))
      println(Future(range, Current(3)))
      println(Future(range, Next(4)))
      println(Future(range, Next(5)))
    }
  }
}
