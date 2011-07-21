package org.agilewiki.blip
package seq
package unionSeq

import org.specs.SpecificationWithJUnit

class UnionSeqTest extends SpecificationWithJUnit {
  "UnionSeqTest" should {
    "union synchronous" in {
      val seqs = new java.util.ArrayList[Sequence[Int, Int]]
      val range = Range(1, 15)
      seqs.add(new FilterSeq(range, (x: Int) => x % 2 == 0))
      seqs.add(new FilterSeq(range, (x: Int) => x % 3 == 0))
      seqs.add(new FilterSeq(range, (x: Int) => x % 5 == 0))
      val union = new UnionSeq(null, seqs)
      Future(union, Loop((key: Int, value: java.util.List[Int]) => println(key+" "+value)))
    }
    "union asynchronous" in {
      val seqs = new java.util.ArrayList[Sequence[Int, Int]]
      val range2 = new Range(new Mailbox, null, 1, 15)
      val range3 = new Range(new Mailbox, null, 1, 15)
      val range5 = new Range(new Mailbox, null, 1, 15)
      seqs.add(new FilterSeq(range2, (x: Int) => x % 2 == 0))
      seqs.add(new FilterSeq(range3, (x: Int) => x % 3 == 0))
      seqs.add(new FilterSeq(range5, (x: Int) => x % 5 == 0))
      val union = new UnionSeq(new Mailbox, seqs)
      Future(union, Loop((key: Int, value: java.util.List[Int]) => println(key+" "+value)))
    }
  }
}
