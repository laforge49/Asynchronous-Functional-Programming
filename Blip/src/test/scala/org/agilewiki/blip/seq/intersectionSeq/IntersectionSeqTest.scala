package org.agilewiki.blip
package seq
package intersectionSeq

import org.specs.SpecificationWithJUnit

class IntersectionSeqTest extends SpecificationWithJUnit {
  "IntersectionSeqTest" should {
    "intersect synchronous" in {
      val seqs = new java.util.ArrayList[Sequence[Int, Int]]
      val range = Range(1, 200)
      seqs.add(new FilterSeq(range, (x: Int) => x % 2 == 0))
      seqs.add(new FilterSeq(range, (x: Int) => x % 3 == 0))
      seqs.add(new FilterSeq(range, (x: Int) => x % 5 == 0))
      val intersection = new IntersectionSeq(null, seqs)
      Future(intersection, Loop((key: Int, value: java.util.List[Int]) => println(key+" "+value)))
    }
    "intersect asynchronous" in {
      val seqs = new java.util.ArrayList[Sequence[Int, Int]]
      val range2 = new Range(new Mailbox, null, 1, 200)
      val range3 = new Range(new Mailbox, null, 1, 200)
      val range5 = new Range(new Mailbox, null, 1, 200)
      seqs.add(new FilterSeq(range2, (x: Int) => x % 2 == 0))
      seqs.add(new FilterSeq(range3, (x: Int) => x % 3 == 0))
      seqs.add(new FilterSeq(range5, (x: Int) => x % 5 == 0))
      val intersection = new IntersectionSeq(new Mailbox, seqs)
      Future(intersection, Loop((key: Int, value: java.util.List[Int]) => println(key+" "+value)))
    }
  }
}
