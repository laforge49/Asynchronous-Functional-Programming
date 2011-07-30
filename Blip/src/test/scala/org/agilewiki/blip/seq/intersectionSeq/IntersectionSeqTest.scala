package org.agilewiki.blip
package seq
package intersectionSeq

import org.specs.SpecificationWithJUnit

class IntersectionSeqTest extends SpecificationWithJUnit {
  "IntersectionSeqTest" should {
    "intersect synchronous" in {
      val seqs = new java.util.ArrayList[Sequence[Int, Int]]
      val range = new Range(1, 200)
      seqs.add(new FilterSeq(range, (x: Int) => x % 2 == 0))
      seqs.add(new FilterSeq(range, (x: Int) => x % 3 == 0))
      seqs.add(new FilterSeq(range, (x: Int) => x % 5 == 0))
      val intersection = new IntersectionSeq(seqs)
      Future(intersection, Loop((key: Int, value: java.util.List[Int]) => println(key+" "+value)))
    }
    "intersect asynchronous" in {
      val seqs = new java.util.ArrayList[Sequence[Int, Int]]
      val range2 = new Range(1, 200)
      range2.setMailbox(new Mailbox)
      val range3 = new Range(1, 200)
      range3.setMailbox(new Mailbox)
      val range5 = new Range(1, 200)
      range5.setMailbox(new Mailbox)
      seqs.add(new FilterSeq(range2, (x: Int) => x % 2 == 0))
      seqs.add(new FilterSeq(range3, (x: Int) => x % 3 == 0))
      seqs.add(new FilterSeq(range5, (x: Int) => x % 5 == 0))
      val intersection = new IntersectionSeq(seqs)
      intersection.setMailbox(new Mailbox)
      Future(intersection, Loop((key: Int, value: java.util.List[Int]) => println(key+" "+value)))
    }
  }
}
