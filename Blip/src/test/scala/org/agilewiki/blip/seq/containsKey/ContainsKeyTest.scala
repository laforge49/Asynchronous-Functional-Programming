package org.agilewiki.blip
package seq
package containsKey

import org.specs.SpecificationWithJUnit

class ContainsKeyTest extends SpecificationWithJUnit {
  "ContainsKeyTest" should {
    "test" in {
      println(Future(Range(1,4), ContainsKey(2)))
      println(Future(Range(1,4), ContainsKey(4)))
    }
  }
}
