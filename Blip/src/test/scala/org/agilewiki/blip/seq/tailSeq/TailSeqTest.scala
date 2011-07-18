package org.agilewiki.blip
package seq
package tailSeq

import org.specs.SpecificationWithJUnit

class TailSeqTest extends SpecificationWithJUnit {
  "TailSeqTest" should {
    "tail" in {
      val range = Range(0, 1000000)
      val tail = new TailSeq(range, 999998)
      Future(tail, Loop((key: Int, value: Int) => println(key+" "+value)))
    }
  }
}
