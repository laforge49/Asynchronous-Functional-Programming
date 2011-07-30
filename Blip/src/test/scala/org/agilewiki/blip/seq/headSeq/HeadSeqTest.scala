package org.agilewiki.blip
package seq
package headSeq

import org.specs.SpecificationWithJUnit

class HeadSeqTest extends SpecificationWithJUnit {
  "HeadSeqTest" should {
    "head" in {
      val alphabet = new java.util.TreeMap[Int, String]
      alphabet.put(8, "Apple")
      alphabet.put(22, "Boy")
      alphabet.put(5, "Cat")
      val navMap = new NavMapSeq(alphabet)
      val head = new HeadSeq(navMap, 22)
      Future(head, Loop((key: Int, value: String) => println(key + " " + value)))
    }
  }
}
