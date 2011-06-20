package org.agilewiki
package util
package lite
package seq

import org.specs.SpecificationWithJUnit
import java.util.ArrayList

class ListTest extends SpecificationWithJUnit {
  "List Seq" should {

    "Pass the basic navigation test" in {
      var lst = new ArrayList[String]
      lst add "one"
      lst add "two"
      lst add "three"
      lst add "four"
      lst add "five"
      lst add "etc"
      var seq = new LiteListSeq(new LiteReactor, lst)
      FutureSeq(seq).firstMatch(0,"one") must be equalTo true
      FutureSeq(seq).currentMatch(2,2,"three") must be equalTo true
      FutureSeq(seq).nextMatch(4,5,"etc") must be equalTo true
      FutureSeq(seq).isCurrentEnd(8) must be equalTo true
    }
  }
}
