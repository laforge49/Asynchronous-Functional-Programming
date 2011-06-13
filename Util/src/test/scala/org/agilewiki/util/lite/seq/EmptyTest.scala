package org.agilewiki
package util
package lite
package seq

import org.specs.SpecificationWithJUnit

class EmptyTest extends SpecificationWithJUnit {
  "Empty Seq" should {

    "be empty" in {
      val seq = new LiteEmptySeq[Int](null)
      FutureSeq(seq).isEmpty must be equalTo true
    }
  }
}
