package org.agilewiki
package util
package sequence
package composit

import java.util.TreeSet

import org.specs.SpecificationWithJUnit
import basic.{NavigableSequence, EmptySequence}

class SubSequenceTest extends SpecificationWithJUnit {
  "Sub Sequence" should {

    "Pass the basic initialization test" in {
      new SubSequence(null, "anything") must throwA(
        new IllegalArgumentException(
          "requirement failed: The super sequence cannot be null"
          )
        )
      new SubSequence(new EmptySequence, null) must throwA(
        new IllegalArgumentException(
          "requirement failed: The prefix cannot be null"
          )
        )
      var seq = new SubSequence(new EmptySequence, "")
      seq.current must beNull
    }

    "Pass the basic SubSequence test" in {
      var lst = List("a", "b", "c")
      lst = lst.flatMap(i => lst.map(j => i + j))
      var set = new TreeSet[String]
      for (s <- lst) set add "Aphr0d1te" + s
      for (s <- lst) set add s + s
      var iSeq = new NavigableSequence(set)
      iSeq.current mustNot beNull
      var seq = new SubSequence(iSeq, "Zeus")
      seq.current must beNull
      seq = new SubSequence(iSeq, "Aphr")
      var pk = seq.current
      while (pk != null) {
        iSeq.current.endsWith(pk) must be equalTo true
        pk = seq next pk
      }
      seq.current must beNull
      iSeq.current mustNot beNull

      seq.current("aa") must beNull
      seq.current("Zeus") must beNull
      seq.current("Aphr0") must beNull
      seq.current("0d1teac") must be equalTo "0d1teac"

      seq.next("aa") must beNull
      seq.next("Zeus") must beNull
      seq.next("Aphr0") must beNull
      seq.next("0d1teac") must be equalTo "0d1teba"
    }

    var lst = List("a", "b", "c")
    lst = lst.flatMap(i => lst.map(j => i + j))
    var set = new TreeSet[String]
    for (s <- lst) set add s

    "Pass the limits test" in {
      var iSeq = new NavigableSequence(set)

      iSeq.current must be equalTo "aa"

      var seq = new SubSequence(iSeq, "b")
      iSeq.current must be equalTo "ba"

      seq.current must be equalTo "a"
      seq current "c" must be equalTo "c"
      seq next "c" must beNull
      seq next null must beNull
      seq.current must beNull
    }

    "Pass the reverse test" in {
      var iSeq = new NavigableSequence(set, true)
      iSeq.isReverse must be equalTo true
      var seq = new SubSequence(iSeq, "b")
      iSeq.current must be equalTo "bc"

      seq.current must be equalTo "c"
      seq.next("c") must be equalTo "b"
      seq.next("b") must be equalTo "a"
      seq.next("a") must beNull
    }

  }
}