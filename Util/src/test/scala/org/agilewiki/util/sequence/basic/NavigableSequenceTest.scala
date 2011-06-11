package org.agilewiki
package util
package sequence
package basic

import java.util.TreeSet

import org.specs.SpecificationWithJUnit

class NavigableSequenceTest extends SpecificationWithJUnit {
  "NavigableSequence" should {
    "Pass Empty Sequence Test" in {
      var set = new TreeSet[String]
      var seq = new NavigableSequence(set)
      seq.current must beNull
      seq.next("anything") must beNull
      seq.current("anything") must beNull
    }

    "Pass the navigation Test of a sequence with size 1" in {
      var set = new TreeSet[String]
      set add "anything"
      var seq = new NavigableSequence(set)
      seq.current must be equalTo "anything"
      seq.next("anything") must beNull
      seq.current("anything") must be equalTo "anything"
    }

    "Pass the navigationTest of a sequence with size 2" in {
      var set = new TreeSet[String]
      set add "first"
      set add "last"
      var seq = new NavigableSequence(set)
      seq.current must be equalTo "first"
      seq.next("first") must be equalTo "last"
      seq.next("last") must beNull
      seq.current("first") must be equalTo "first"
      seq.current("last") must be equalTo "last"
    }

    "Pass the navigation Test of a long fixed size sequence" in {
      var set = new TreeSet[String]
      for (i <- 0 to 5)
        set.add("" + i)
      var seq = new NavigableSequence(set)
      seq.current mustNot beNull
      for (i <- 0 to 5) {
        var pk = seq.current("" + i)
        pk must be equalTo "" + i
      }
      for (i <- 0 to 4) {
        var pk = seq.next("" + i)
        pk must be equalTo "" + (i + 1)
      }
    }

    "Pass the navigation Test of a long variable size sequence" in {
      var set = new TreeSet[String]
      var seq = new NavigableSequence(set)
      seq.current must beNull
      for (i <- 0 to 5)
        set.add("" + i)
      seq.current("0") must be equalTo "0"
      seq.next("4") must be equalTo "5"
      set remove "5"
      seq.current("3") must be equalTo "3"
      set.removeAll(set)
      seq.current must beNull
    }

    "Pass the sequence comparison test" in {
      var set1 = new TreeSet[String]
      var set2 = new TreeSet[String]
      var seq1 = new NavigableSequence(set1)
      var seq2 = new NavigableSequence(set2)
      seq1 == seq2 must be equalTo true
      set1.add("first")
      seq1.current("first") must be equalTo "first"
      seq1 != seq2 must be equalTo true
      set2.add("first")
      seq2.current("first") must be equalTo "first"
      seq1 == seq2 must be equalTo true
      set1 add "last"
      seq1 current "last" must be equalTo "last"
      seq1 == seq2 must be equalTo false
      seq1 compareTo seq2 must be greaterThan 0
      seq1.current("first") must be equalTo "first"
      seq1 compareTo seq2 must be equalTo 0
    }

    "Pass the not found tests" in {
      var set = new TreeSet[String]
      var seq = new NavigableSequence(set)
      seq.current must beNull
      seq next "anything" must beNull
      seq current "anything" must beNull
      for (i <- 0 to 5)
        set.add("" + i)
      seq next "anything" must beNull
      seq current "anything" must beNull
      seq next "..." must be equalTo "0"
      seq current "..." must be equalTo "0"
      seq next "01010011" must be equalTo "1"
      seq current "10101100" must be equalTo "2"
    }

    "Pass the reverse sequence decaration test" in {
      var seq = new NavigableSequence(new TreeSet[String], true)
      seq.isReverse must be equalTo true
    }

    var lst = List.range(0, 10)
    var set = new TreeSet[String]
    for (i <- lst) set.add("" + i)

    "Pass the reverse order check" in {
      var seq = new NavigableSequence(set, true)
      seq.isReverse must be equalTo true
      var pk = seq.current
      pk must be equalTo "9"
      while (pk != null) {
        val nxt = seq next pk
        if (nxt != null)
          nxt < pk must be equalTo true
        pk = nxt
      }
    }
  }
}
