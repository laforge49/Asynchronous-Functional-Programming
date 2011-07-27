package org.agilewiki.blip
package seq
package excludeSeq

import org.specs.SpecificationWithJUnit

class ExcludeSeqTest extends SpecificationWithJUnit {
  "ExcludeSeqTest" should {
    "exclude synchronous" in {
      val range = Range(1, 10)
      val seq = new FilterSeq(range, (x: Int) => x % 2 == 0)
      val exclude = new FilterSeq(range, (x: Int) => x % 3 == 0)
      val excludeSeq = new ExcludeSeq(seq, exclude)
      Future(excludeSeq, Loop((key: Int, value: Int) => println(key+" "+value)))
    }
    "exclude asynchronous" in {
      val range2 = new Range(new Mailbox, null, 1, 10)
      val seq = new FilterSeq(range2, (x: Int) => x % 2 == 0)
      val range3 = new Range(new Mailbox, null, 1, 200)
      val exclude = new FilterSeq(range2, (x: Int) => x % 3 == 0)
      val excludeSeq = new ExcludeSeq(seq, exclude)
      Future(excludeSeq, Loop((key: Int, value: Int) => println(key+" "+value)))
    }
  }
}
