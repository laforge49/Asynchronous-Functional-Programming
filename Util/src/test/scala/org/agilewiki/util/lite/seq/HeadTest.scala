package org.agilewiki
package util
package lite
package seq

import org.specs.SpecificationWithJUnit
import java.util.ArrayList

class HeadTest extends SpecificationWithJUnit {
  "Head Seq" should {

    "Pass the basic navigation test" in {
      var lst = new ArrayList[String]
      lst add "one"
      lst add "two"
      lst add "three"
      lst add "four"
      lst add "five"
      lst add "etc"
      val iseq = new LiteListSeq(new LiteReactor(null), lst)
      val seq = iseq.head(2)
      FutureSeq(seq).firstMatch(0,"one") must be equalTo true
      FutureSeq(seq).currentMatch(1,1,"two") must be equalTo true
      FutureSeq(seq).isNextEnd(2) must be equalTo true
    }
  }
}
