package org.agilewiki
package util
package sequence
package composit

import java.util.TreeSet

import org.specs.SpecificationWithJUnit
import basic.{EmptySequence, NavigableSequence}

class HeadSequenceTest extends SpecificationWithJUnit {
  "Head Sequence" should {

    "Pass the basic head tests" in {
      var lst = List("a", "b", "c")
      lst = lst.flatMap(i => lst.map(j => i + j))
      lst = lst.flatMap(i => lst.map(j => i + j))
      var set = new TreeSet[String]
      for (s <- lst) set add s
      var iSeq = new NavigableSequence(set)
      iSeq.current == null must be equalTo false
      var seq = new HeadSequence(iSeq, "b")
      seq.current == null must be equalTo false
      var pk = seq.current
      while (pk != null) {
        pk < "b" must be equalTo true
        pk = seq.next(pk)
      }
      iSeq.current == null must be equalTo false
      iSeq.current >= "b" must be equalTo true
      seq.current must beNull

      seq.next("") must be equalTo "aaaa"
      seq current "abab" must be equalTo "abab"
      seq next "abab" must be equalTo "abac"
      seq next "baba" must beNull
    }


    var lst = List("a", "b", "c")
    lst = lst.flatMap(i => lst.map(j => i + j))
    var set = new TreeSet[String]
    for (s <- lst) set add s

    "Pass the head exclusive limit tests" in {
      var iSeq = new NavigableSequence(set)
      iSeq.current == null must be equalTo false
      var seq = new HeadSequence(iSeq, "cb")
      seq.current == null must be equalTo false
      var pk = seq.current
      while (pk != null) {
        pk < "cb" must be equalTo true
        pk = seq.next(pk)
      }
      iSeq.current == "cb" must be equalTo true
      seq.current == null must be equalTo true
    }

    "Pass the initialization tests" in {
      new HeadSequence(new EmptySequence, null) must throwA(
        new IllegalArgumentException(
          "requirement failed: The head sequence limit cannot be null"
          )
        )
      new HeadSequence(null, "max") must throwA(
        new IllegalArgumentException(
          "requirement failed: The wrapped sequence cannot be null"
          )
        )
    }

    "Pass the reverse test" in {
      var iSeq = new NavigableSequence(set, true)
      iSeq.isReverse must be equalTo true
      var seq = new HeadSequence(iSeq, "bc")
      seq.isReverse must be equalTo true
      seq.current must be equalTo "cc"
      seq.next("cc") must be equalTo "cb"
      seq.next("cb") must be equalTo "ca"
      seq.next("ca") must beNull
    }
  }
}
