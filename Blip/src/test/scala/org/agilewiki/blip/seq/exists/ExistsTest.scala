package org.agilewiki.blip
package seq
package exists

import org.specs.SpecificationWithJUnit
import bind._

class ExistsTest extends SpecificationWithJUnit {
  "ExistsTest" should {
    "test" in {
      println(Future(new Range(1,4), Exists((a: Int) => a == 3)))
      println(Future(new Range(1,4), Exists((a: Int) => a == 4)))
    }
  }
}
