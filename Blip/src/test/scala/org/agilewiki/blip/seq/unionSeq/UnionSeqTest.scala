package org.agilewiki.blip
package seq
package unionSeq

import org.specs.SpecificationWithJUnit

class UnionSeqTest extends SpecificationWithJUnit {
  "UnionSeqTest" should {
    "union synchronous" in {
      val seqs = new java.util.ArrayList[Sequence[Int, Int]]
      val range = new Range(1, 15)
      seqs.add(new FilterSeq(range, (x: Int) => x % 2 == 0))
      seqs.add(new FilterSeq(range, (x: Int) => x % 3 == 0))
      seqs.add(new FilterSeq(range, (x: Int) => x % 5 == 0))
      val union = new UnionSeq(seqs)
      Future(union, Loop((key: Int, value: java.util.List[Int]) => println(key+" "+value)))
    }
    "union asynchronous" in {
      val systemServices = SystemServices()
      try {
        val seqs = new java.util.ArrayList[Sequence[Int, Int]]
      val range2 = new Range(1, 15)
      range2.setMailbox(systemServices.newAsyncMailbox)
      val range3 = new Range(1, 15)
      range3.setMailbox(systemServices.newAsyncMailbox)
      val range5 = new Range(1, 15)
      range5.setMailbox(systemServices.newAsyncMailbox)
      seqs.add(new FilterSeq(range2, (x: Int) => x % 2 == 0))
      seqs.add(new FilterSeq(range3, (x: Int) => x % 3 == 0))
      seqs.add(new FilterSeq(range5, (x: Int) => x % 5 == 0))
      val union = new UnionSeq(seqs)
      union.setMailbox(systemServices.newAsyncMailbox)
      Future(union, Loop((key: Int, value: java.util.List[Int]) => println(key+" "+value)))
      } finally {
        systemServices.close
      }
    }
  }
}
