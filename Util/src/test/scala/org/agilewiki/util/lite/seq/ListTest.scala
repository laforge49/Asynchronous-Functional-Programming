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
      var seq = new LiteListSeq(null, lst)
      FutureSeq(seq).first must be equalTo "one"
      FutureSeq(seq).current(2) must be equalTo "three"
      FutureSeq(seq).next(4) must be equalTo "etc"
      FutureSeq(seq).isCurrentEnd(8) must be equalTo true
    }
  }
}
