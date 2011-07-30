package org.agilewiki.blip
package seq
package filterSeq

import org.specs.SpecificationWithJUnit

class FilterSeqTest extends SpecificationWithJUnit {
  "FilterSeqTest" should {
    "filter" in {
      val range = new Range(0, 4)
      val filter = new FilterSeq(range, (v: Int) => v % 2 == 0)
      Future(filter, Loop((key: Int, value: Int) => println(key+" "+value)))
    }
  }
}
