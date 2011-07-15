package org.agilewiki.blip
package seq
package loop

import org.specs.SpecificationWithJUnit

class LoopTest extends SpecificationWithJUnit {
  "Loop" should {
    "print a list" in {
      val fact = new java.util.ArrayList[Int]
      fact.add(0)
      fact.add(1)
      fact.add(2)
      fact.add(6)
      fact.add(24)
      val factSeq = new ListSeq(null, null, fact)
      Future(factSeq, Loop((key: Int, value: Int) => println(key+" "+value)))
    }
  }
}
