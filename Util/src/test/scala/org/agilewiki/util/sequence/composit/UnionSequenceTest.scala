package org.agilewiki
package util
package sequence
package composit

import java.util.TreeSet

import org.specs.SpecificationWithJUnit
import basic.{NavigableSequence, EmptySequence}

class UnionSequenceTest extends SpecificationWithJUnit {
  "Union Sequence" should {
    val lst = List("a", "b", "c")
    val lsta = lst.map(i => i + "a")
    val lstb = lst.map(i => i + "b")
    val lstc = lst.map(i => i + "c")
    var lstAll = lst.flatMap(i => lst.map(j => i + j))
    var seta = new TreeSet[String]
    for (e <- lsta) seta add e
    var setb = new TreeSet[String]
    for (e <- lstb) setb add e
    var setc = new TreeSet[String]
    for (e <- lstc) setc add e

    "pass the initialization test" in {
      var seq = new UnionSequence
      seq.add(null) must throwA(
        new IllegalArgumentException(
          "requirement failed: A sequence must be not null"
          )
        )
      seq add new EmptySequence
      seq add new EmptySequence
      seq.current must beNull
    }

    "Pass the one sequence test" in {
      var seq = new UnionSequence
      seq add new NavigableSequence(seta)
      var pk = seq.current
      pk mustNot beNull
      while (pk != null) {
        lsta contains pk must be equalTo true
        pk = seq.next(pk)
      }
      seq.current must beNull

      seq = new UnionSequence
      seq add new NavigableSequence(seta)
      seq current "a" must be equalTo "aa"
      seq current "ba" must be equalTo "ba"
      seq current "cc" must beNull
    }

    "Pass the multiple sequences position test" in {
      var seq = new UnionSequence
      seq add new NavigableSequence(seta)
      seq add new NavigableSequence(setb)
      seq add new NavigableSequence(setc)
      seq.current mustNot beNull

      seq next "cd" must beNull
    }

    "Pass the multiple sequence positionTo test" in {
      var seq = new UnionSequence
      seq add new NavigableSequence(seta)
      seq add new NavigableSequence(setb)
      seq add new NavigableSequence(setc)
      seq.current mustNot beNull

      seq current "ba" must be equalTo "ba"
      seq current "c4" must be equalTo "ca"
      seq current "cd" must beNull
      seq.current must beNull
    }

    "Pass the orthogonal sequences order and cound tests" in {
      var seq = new UnionSequence
      seq add new NavigableSequence(seta)
      seq add new NavigableSequence(setb)
      seq add new NavigableSequence(setc)
      var i = 0
      var pk = seq.current
      pk mustNot beNull
      while (pk != null) {
        val nxt = seq.next(pk)
        if (nxt != null)
          pk < nxt must be equalTo true
        lstAll contains pk must be equalTo true
        pk = nxt
        i += 1
      }
      i must be equalTo lstAll.size
      seq.current must beNull
    }

    "Pass the non orthogonal sequences  order and cound tests" in {
      setb addAll seta must be equalTo true

      var seq = new UnionSequence
      seq add new NavigableSequence(seta)
      seq add new NavigableSequence(setb)
      seq add new NavigableSequence(setc)
      seq.current mustNot beNull
      var i = 0
      var pk = seq.current
      pk mustNot beNull
      while (pk != null) {
        val nxt = seq.next(pk)
        if (nxt != null)
          pk < nxt must be equalTo true
        lstAll contains pk must be equalTo true
        pk = nxt
        i += 1
      }
      i must be equalTo lstAll.size
      seq.current must beNull
    }

    "Pass the reverse test" in {
      var seq = new UnionSequence
      seq.isReverse must be equalTo false
      seq add new NavigableSequence(seta, true)
      seq.isReverse must be equalTo true
      seq add new NavigableSequence(setb) must throwA(
        new IllegalStateException(
          "All sequences must have the same direction"
          )
        )
      seq add new NavigableSequence(setb, true)
      seq add new NavigableSequence(setc, true)
      seq.current must be equalTo "cc"
      seq.next("cc") must be equalTo "cb"
      seq.next("cb") must be equalTo "ca"
      seq.current("ca") must be equalTo "ca"
      seq.next("ca") must be equalTo "bc"
      seq.next("bc") must be equalTo "bb"
      seq.next("bb") must be equalTo "ba"
      seq current "b" must be equalTo "ac"
      seq.next("ac") must be equalTo "ab"
      seq.next("ab") must be equalTo "aa"
      seq.current("a") must beNull
    }

  }
}
