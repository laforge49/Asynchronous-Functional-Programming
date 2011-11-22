package org.agilewiki.blip
package seq
package mapSeq

import org.specs.SpecificationWithJUnit
import bind._

class MapSeqTest extends SpecificationWithJUnit {
  "MapSeqTest" should {
    "map" in {
      val range = new Range(1, 4)
      val map = new MapSeq(range, (v: Int) => v * 2)
      Future(map, Loop((key: Int, value: Int) => println(key+" "+value)))
    }
  }
}
