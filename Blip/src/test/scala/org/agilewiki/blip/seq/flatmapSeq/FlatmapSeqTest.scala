package org.agilewiki.blip
package seq
package flatmapSeq

import org.specs.SpecificationWithJUnit
import bind._

class FlatmapSeqTest extends SpecificationWithJUnit {
  "FlatmapSeqTest" should {
    "drop null values" in {
      val alphabet = new java.util.HashMap[Int, String]
      alphabet.put(8, "Apple")
      alphabet.put(22, "Boy")
      alphabet.put(5, "Cat")
      val range = new Range(0, 10)
      val flatmap = new FlatmapSeq(range, (v: Int) => alphabet.get(v))
      Future(flatmap, Loop((key: Int, value: String) => println(key+" "+value)))
    }
  }
}
