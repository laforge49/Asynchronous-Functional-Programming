package org.agilewiki
package util
package sequence
package composit

import java.util.TreeSet

import org.specs.SpecificationWithJUnit
import basic.{NavigableSequence, EmptySequence}

class IntersectionSequenceTest extends SpecificationWithJUnit {
  "Intersection Sequence" should {
    "Pass the Empty Test" in {
      val seq = new IntersectionSequence
      seq.current == null must be equalTo true
      seq.next("anything") == null must be equalTo true

      seq add new EmptySequence
      seq.current == null must be equalTo true
      seq.next("anything") == null must be equalTo true

      seq add new EmptySequence
      seq.current == null must be equalTo true
      seq.next("anything") == null must be equalTo true
    }

    "Pass a single sequence test" in {
      var lst = List("a", "b")
      lst = lst.flatMap(i => lst.map(j => i + j))
      lst = lst.flatMap(i => lst.map(j => i + j))
      var set = new TreeSet[String]
      for (s <- lst) set add s
      var iSeq = new NavigableSequence(set)
      iSeq.current must be equalTo "aaaa"
      var seq = new IntersectionSequence
      seq add iSeq
      seq next "" mustNot beNull
      var pk = seq.current
      while (pk != null) {
        pk must be equalTo iSeq.current
        pk = seq.next(pk)
      }
      seq.current must beNull
      iSeq.current must beNull
    }

    "Pass orthogonal sequences test" in {
      var lst = List("a", "b")
      lst = lst.flatMap(i => lst.map(j => i + j))
      var set = new TreeSet[String]
      for (s <- lst) set add s
      var iSeq1 = new NavigableSequence(set)
      lst = List("aa", "bb")
      lst = lst.flatMap(i => lst.map(j => i + j))
      set = new TreeSet[String]
      for (s <- lst) set add s
      var iSeq2 = new NavigableSequence(set)
      var seq = new IntersectionSequence
      seq add iSeq1
      seq add iSeq2
      seq next "" must beNull
    }

    var lst = List("a", "b", "d")
    lst = lst.flatMap(i => lst.map(j => i + j))
    var set1 = new TreeSet[String]
    for (s <- lst) set1 add s
    lst = List("a", "b", "c")
    lst = lst.flatMap(i => lst.map(j => i + j))
    var set2 = new TreeSet[String]
    for (s <- lst) set2 add s


    "Pass none orthogonal sequences test" in {
      var iSeq1 = new NavigableSequence(set1)
      var iSeq2 = new NavigableSequence(set2)
      var seq = new IntersectionSequence
      seq add iSeq1
      seq add iSeq2

      seq.current must be equalTo "aa"
      seq current "ab" must be equalTo "ab"
      seq current "ba" must be equalTo "ba"
      seq current "bb" must be equalTo "bb"
      seq current "cd" must beNull

      seq next "aa" must be equalTo "ab"
      seq next "bb" must beNull
      seq next "cd" must beNull

      seq next "" must be equalTo "aa"
      lst = List("a", "b", "c")
      lst = lst.flatMap(i => lst.map(j => i + j))
      var n: String = null
      do {
        lst contains seq.current must be equalTo true
        n = seq.next(n)
      } while (n != null)
    }

    "Pass the reverse test" in {
      var iSeq1 = new NavigableSequence(set1, true)
      iSeq1.isReverse must be equalTo true
      var iSeq2 = new NavigableSequence(set2)
      var seq = new IntersectionSequence
      seq.isReverse must be equalTo false
      seq add iSeq1
      seq.isReverse must be equalTo true
      seq add iSeq2 must throwA(new IllegalStateException("All sequences must have the same direction"))
      seq = new IntersectionSequence
      seq add iSeq1
      iSeq2 = new NavigableSequence(set2, true)
      seq add iSeq2
      seq.current must be equalTo "bb"
      seq next "bb" must be equalTo "ba"
      seq next "ba" must be equalTo "ab"
      seq next "ab" must be equalTo "aa"
      seq next "aa" must beNull
      seq current "b" must be equalTo "ab"
      seq current "a" must beNull
    }
  }
}
