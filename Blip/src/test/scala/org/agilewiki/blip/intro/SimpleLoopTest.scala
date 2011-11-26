package org.agilewiki.blip
package intro

import org.specs.SpecificationWithJUnit
import bind._
import seq._

class SimpleLoopTest extends SpecificationWithJUnit {
  "SimpleLoopTest" should {
    "print a range" in {
      val range = new Range(4, 8)
      Future(range, Loop {
        (key: Int, value: Int) => {
          println(key + " -> " + value)
        }
      })
    }
    /*
    Output:
    4 -> 4
    5 -> 5
    6 -> 6
    7 -> 7
    */
  }
}
