package org.agilewiki.blip
package seq
package mapSeq

import org.specs.SpecificationWithJUnit

class MapSeqTest extends SpecificationWithJUnit {
  "MapSeqTest" should {
    "map" in {
      val range = Range(1, 4)
      val map = new MapSeq(range, (v: Int) => v * 2)
      Future(map, Loop((key: Int, value: Int) => println(key+" "+value)))
    }
  }
}
