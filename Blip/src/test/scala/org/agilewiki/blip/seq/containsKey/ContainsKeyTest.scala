package org.agilewiki.blip
package seq
package containsKey

import org.specs.SpecificationWithJUnit
import bind._

class ContainsKeyTest extends SpecificationWithJUnit {
  "ContainsKeyTest" should {
    "test" in {
      println(Future(new Range(1,4), ContainsKey(2)))
      println(Future(new Range(1,4), ContainsKey(4)))
    }
  }
}
