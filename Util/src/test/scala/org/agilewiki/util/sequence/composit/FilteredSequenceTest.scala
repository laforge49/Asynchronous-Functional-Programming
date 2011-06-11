package org.agilewiki
package util
package sequence
package composit

import java.util.TreeSet

import org.specs.SpecificationWithJUnit
import basic.{EmptySequence, NavigableSequence}

class FilteredSequenceTest extends SpecificationWithJUnit {
  "Filtered Sequence" should {

    "Pass the basic always true filter test" in {
      var set = new TreeSet[String]
      var iSeq = new NavigableSequence(set)
      iSeq.current == null must be equalTo true
      var seq = new FilteredSequence(iSeq, (x) => true)
      seq.current == null must be equalTo true
      set add "123"
      iSeq next "0" must be equalTo "123"
      iSeq.current == null must be equalTo false
      seq.current == null must be equalTo false
    }

    "Pass the basic always false filter test" in {
      var set = new TreeSet[String]
      var iSeq = new NavigableSequence(set)
      iSeq.current == null must be equalTo true
      var seq = new FilteredSequence(iSeq, (x) => false)
      seq.current == null must be equalTo true
      set add "123"
      iSeq current "123" must be equalTo "123"
      iSeq.current == null must be equalTo false
      seq.current == null must be equalTo true
    }

    "Pass the test of a filtering function" in {
      var set = new TreeSet[String]
      set add "00"
      set add "01"
      set add "10"
      set add "11"
      var iSeq = new NavigableSequence(set)
      iSeq.current must be equalTo "00"
      var seq = new FilteredSequence(iSeq, (x) => x endsWith "1")
      seq.current must be equalTo "01"
      var pk = seq.current
      while (pk != null) {
        pk endsWith "1" must be equalTo true
        pk = seq next pk
      }

      seq.current("11") must be equalTo "11"
      seq.current("00") must be equalTo "01"
      seq.current("abc") == null must be equalTo true

      iSeq current "0" must be equalTo "00"
      seq = new FilteredSequence(iSeq, (x) => x startsWith ".")
      iSeq.current == null must be equalTo true
      seq.current == null must be equalTo true
      iSeq.current == null must be equalTo true
      iSeq next "." must be equalTo "00"
      seq == new EmptySequence must be equalTo true
    }

    "Pass the initialization tests" in {
      new FilteredSequence(null, (x) => true) must throwA(
        new IllegalArgumentException(
          "requirement failed: The wrapped sequence cannot be null"
          )
        )
      new FilteredSequence(new EmptySequence, null) must throwA(
        new IllegalArgumentException(
          "requirement failed: The filter function cannot be null"
          )
        )
    }

  }
}