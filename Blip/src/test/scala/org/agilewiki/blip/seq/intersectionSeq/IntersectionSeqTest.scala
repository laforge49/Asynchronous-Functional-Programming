package org.agilewiki.blip
package seq
package intersectionSeq

import org.specs.SpecificationWithJUnit
import bind._

class IntersectionSeqTest extends SpecificationWithJUnit {
  "IntersectionSeqTest" should {
    "intersect synchronous" in {
      val seqs = new java.util.ArrayList[Sequence[Int, Int]]
      val range = new Range(1, 200)
      seqs.add(new FilterSeq(range, (x: Int) => x % 2 == 0))
      seqs.add(new FilterSeq(range, (x: Int) => x % 3 == 0))
      seqs.add(new FilterSeq(range, (x: Int) => x % 5 == 0))
      val intersection = new IntersectionSeq(seqs)
      Future(intersection, Loop((key: Int, value: java.util.List[Int]) => println(key + " " + value)))
    }
    "intersect asynchronous" in {
      val systemServices = SystemServices()
      try {
        val seqs = new java.util.ArrayList[Sequence[Int, Int]]
        val range2 = new Range(1, 200)
        range2.setExchangeMessenger(systemServices.newAsyncMailbox)
        val range3 = new Range(1, 200)
        range3.setExchangeMessenger(systemServices.newAsyncMailbox)
        val range5 = new Range(1, 200)
        range5.setExchangeMessenger(systemServices.newAsyncMailbox)
        seqs.add(new FilterSeq(range2, (x: Int) => x % 2 == 0))
        seqs.add(new FilterSeq(range3, (x: Int) => x % 3 == 0))
        seqs.add(new FilterSeq(range5, (x: Int) => x % 5 == 0))
        val intersection = new IntersectionSeq(seqs)
        intersection.setExchangeMessenger(systemServices.newAsyncMailbox)
        Future(intersection, Loop((key: Int, value: java.util.List[Int]) => println(key + " " + value)))
      } finally {
        systemServices.close
      }
    }
  }
}
