package org.agilewiki
package util
package lite
package seq

import org.specs.SpecificationWithJUnit

class SetTest extends SpecificationWithJUnit {
  "Set Seq" should {

    "Pass the basic navigation test" in {
      var lst = new java.util.TreeSet[String]
      lst add "one"
      lst add "two"
      lst add "three"
      lst add "four"
      lst add "five"
      lst add "etc"
      var seq = new LiteNavigableSetSeq(null, lst)
      FutureSeq(seq).firstMatch("etc", "etc") must be equalTo true
      FutureSeq(seq).currentMatch("f", "five", "five") must be equalTo true
      FutureSeq(seq).nextMatch("three", "two", "two") must be equalTo true
      FutureSeq(seq).isCurrentEnd("zed") must be equalTo true
    }
  }
}
