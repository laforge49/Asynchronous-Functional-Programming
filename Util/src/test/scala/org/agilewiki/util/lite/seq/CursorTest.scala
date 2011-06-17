package org.agilewiki
package util
package lite
package seq

import org.specs.SpecificationWithJUnit
import java.util.TreeSet

class CursorTest extends SpecificationWithJUnit {
  "Cursor" should {

    "Pass the comparision Test" in {
      var set = new TreeSet[String]
      var seq1 = new LiteSeqCursor(new LiteEmptySeq[String, String])
      FutureSeq(seq1).first
      var seq2 = new LiteSeqCursor(new LiteNavigableSetSeq[String](null, set))
      FutureSeq(seq2).first
      seq1 == seq2 must be equalTo true
      seq2 == seq1 must be equalTo true
      set add "anything"
      FutureSeq(seq2).current("anything") must be equalTo "anything"
      seq1 != seq2 must be equalTo true
      seq2 != seq1 must be equalTo true
      seq1.compareTo(seq2) > 0 must be equalTo true
      seq2.compareTo(seq1) < 0 must be equalTo true
      var set3 = new TreeSet[String]
      set3.add("something")
      var seq3 = new LiteSeqCursor(new LiteNavigableSetSeq[String](null, set3))
      FutureSeq(seq3).first
      seq2.compareTo(seq3) < 0 must be equalTo true
      seq3.compareTo(seq2) > 0 must be equalTo true
      seq2 = new LiteSeqCursor(new LiteEmptySeq[String, String])
      FutureSeq(seq2).first
      seq1 == seq2 must be equalTo true
    }

    "Pass Empty Sequence Test" in {
      var set = new TreeSet[String]
      var seq = new LiteSeqCursor(new LiteNavigableSetSeq[String](null, set))
      FutureSeq(seq).isEmpty must be equalTo true
      FutureSeq(seq).isNextEnd("anything") must be equalTo true
      FutureSeq(seq).isCurrentEnd("anything") must be equalTo true
    }

    "Pass the navigation Test of a sequence with size 1" in {
      var set = new TreeSet[String]
      set add "anything"
      var seq = new LiteSeqCursor(new LiteNavigableSetSeq[String](null, set))
      FutureSeq(seq).current("anything") must be equalTo "anything"
      FutureSeq(seq).isNextEnd("anything") must be equalTo true
      FutureSeq(seq).current("anything") must be equalTo "anything"
    }

    "Pass the navigationTest of a sequence with size 2" in {
      var set = new TreeSet[String]
      set add "first"
      set add "last"
      var seq = new LiteSeqCursor(new LiteNavigableSetSeq[String](null, set))
      FutureSeq(seq).current("first") must be equalTo "first"
      FutureSeq(seq).isNextEnd("last") must be equalTo true
      FutureSeq(seq).current("first") must be equalTo "first"
      FutureSeq(seq).current("last") must be equalTo "last"
    }

    "Pass the navigation Test of a long fixed size sequence" in {
      var set = new TreeSet[Int]
      for (i <- 0 to 5)
        set.add(i)
      var seq = new LiteSeqCursor(new LiteNavigableSetSeq[Int](null, set))
      for (i <- 0 to 5) {
        FutureSeq(seq).current(i) must be equalTo i
      }
      for (i <- 0 to 4) {
        FutureSeq(seq).next(i) must be equalTo (i + 1)
      }
    }

    "Pass the navigation Test of a long variable size sequence" in {
      var set = new TreeSet[Int]
      var seq = new LiteSeqCursor(new LiteNavigableSetSeq[Int](null, set))
      FutureSeq(seq).isEmpty must be equalTo true
      for (i <- 0 to 5)
        set.add(i)
      FutureSeq(seq).current(0) must be equalTo 0
      FutureSeq(seq).next(4) must be equalTo 5
      set remove 5
      FutureSeq(seq).current(3) must be equalTo 3
      set.removeAll(set)
      FutureSeq(seq).isEmpty must be equalTo true
    }
  }
}
