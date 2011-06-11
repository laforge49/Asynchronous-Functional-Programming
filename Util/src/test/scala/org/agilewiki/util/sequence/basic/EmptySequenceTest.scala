package org.agilewiki
package util
package sequence
package basic

import org.specs.SpecificationWithJUnit

import java.util.TreeSet


class EmptySequenceTest extends SpecificationWithJUnit {
  "Empty Sequence" should {
    "Pass the basic Empty Sequence Test" in {
      var seq = new EmptySequence
      seq.next("anything") == null must be equalTo true
      seq.current("anything") == null must be equalTo true
    }

    "Pass the comparision Test" in {
      var set = new TreeSet[String]
      var seq1 = new EmptySequence
      var seq2: SequenceSource = new NavigableSequence(set)
      seq1.compareTo(seq2) must throwA(new NoSuchElementException)
      seq1 == seq2 must be equalTo true
      seq2 == seq1 must be equalTo true
      set add "anything"
      seq2 current "anything" must be equalTo "anything"
      seq1 != seq2 must be equalTo true
      seq2 != seq1 must be equalTo true
      seq2 = new EmptySequence
      seq1 == seq2 must be equalTo true
    }
  }
}
