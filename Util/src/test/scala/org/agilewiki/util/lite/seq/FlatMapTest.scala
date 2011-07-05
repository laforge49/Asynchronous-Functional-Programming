package org.agilewiki
package util
package lite
package seq

import org.specs.SpecificationWithJUnit

class FlatMapTest extends SpecificationWithJUnit {
  "FlatMap operations" should {

    "transform and filter a sequence" in {
      val list = new java.util.ArrayList[String]
      list.add("a")
      list.add("b")
      list.add("c")
      var seq = new LiteListSeq(new LiteReactor, list)
      val m = new java.util.TreeMap[String, String]
      m.put("a", "Apple")
      m.put("c", "Cat")
      var mappedSeq = seq.flatMap((x: String) => m.get(x))
      FutureSeq(mappedSeq).firstMatch(0, "Apple") must be equalTo true
      FutureSeq(mappedSeq).currentMatch(1, 2, "Cat") must be equalTo true
      FutureSeq(mappedSeq).nextMatch(1, 2, "Cat") must be equalTo true
      FutureSeq(mappedSeq).isNextEnd(2) must be equalTo true
      val mapSeq = new LiteNavigableMapSeq(new LiteReactor, m)
      mappedSeq = seq.flatMap(mapSeq)
      FutureSeq(mappedSeq).firstMatch(0, "Apple") must be equalTo true
      FutureSeq(mappedSeq).currentMatch(1, 2, "Cat") must be equalTo true
      FutureSeq(mappedSeq).nextMatch(1, 2, "Cat") must be equalTo true
      FutureSeq(mappedSeq).isNextEnd(2) must be equalTo true
    }
  }
}
