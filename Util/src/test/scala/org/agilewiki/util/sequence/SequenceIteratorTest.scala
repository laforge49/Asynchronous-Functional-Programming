package org.agilewiki
package util
package sequence

import basic.{NavigableSequence, EmptySequence}
import java.util.TreeSet

import org.specs.SpecificationWithJUnit

class SequenceIteratorTest extends SpecificationWithJUnit {
  "Sequence Iterator" should {

    "Pass the initialization tests" in {
      new SequenceIterator(null) must throwA(
        new IllegalArgumentException(
          "requirement failed: The wrapped sequence cannot be null"
          )
        )

      var it = new SequenceIterator(new EmptySequence)
      it.hasNext must be equalTo false
      it.next must throwA(new NoSuchElementException)
    }


    var lst = List("a", "b")
    lst = lst.flatMap(i => lst.map(j => i + j))
    var set = new TreeSet[String]
    for (i <- lst) set add i

    "Pass the Normal Iterator test" in {
      set.isEmpty must be equalTo false
      var seq = new NavigableSequence(set)
      var it = new SequenceIterator(seq)
      var i = 0
      while (it.hasNext) {
        lst contains it.next must be equalTo true
        i = i + 1
      }
      i must be equalTo lst.size
      it.next must throwA(new NoSuchElementException)
      it.hasNext must be equalTo false
      seq.current must beNull
    }
  }
}