package org.agilewiki.blip
package seq
package excludeSeq

import org.specs.SpecificationWithJUnit

class ExcludeSeqTest extends SpecificationWithJUnit {
  "ExcludeSeqTest" should {
    "exclude synchronous" in {
      val range = new Range(1, 10)
      val seq = new FilterSeq(range, (x: Int) => x % 2 == 0)
      val exclude = new FilterSeq(range, (x: Int) => x % 3 == 0)
      val excludeSeq = new ExcludeSeq(seq, exclude)
      Future(excludeSeq, Loop((key: Int, value: Int) => println(key + " " + value)))
    }
    "exclude asynchronous" in {
      val systemServices = SystemServices()
      try {
        val range2 = new Range(1, 10)
        range2.setMailbox(systemServices.newSyncMailbox)
        val seq = new FilterSeq(range2, (x: Int) => x % 2 == 0)
        val range3 = new Range(1, 200)
        range3.setMailbox(systemServices.newSyncMailbox)
        val exclude = new FilterSeq(range2, (x: Int) => x % 3 == 0)
        val excludeSeq = new ExcludeSeq(seq, exclude)
        Future(excludeSeq, Loop((key: Int, value: Int) => println(key + " " + value)))
      } finally {
        systemServices.close
      }
    }
  }
}
