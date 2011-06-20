package org.agilewiki
package util
package lite
package seq

import org.specs.SpecificationWithJUnit
import java.util.ArrayList

class TailTest extends SpecificationWithJUnit {
  "Tail Seq" should {

    "Pass the basic navigation test" in {
      var lst = new ArrayList[String]
      lst add "one"
      lst add "two"
      lst add "three"
      lst add "four"
      lst add "five"
      lst add "etc"
      val iseq = new LiteListSeq(new LiteReactor, lst)
      val seq = iseq.tailActor(4)
      FutureSeq(seq).firstMatch(4,"five") must be equalTo true
      FutureSeq(seq).currentMatch(2,4,"five") must be equalTo true
      FutureSeq(seq).nextMatch(4,5,"etc") must be equalTo true
      FutureSeq(seq).isCurrentEnd(8) must be equalTo true
    }
  }
}
