package org.agilewiki
package util
package sequence
package basic

import java.util.ArrayList

import org.specs.SpecificationWithJUnit

class ListSequenceTest extends SpecificationWithJUnit {
  "List Sequence" should {
    "Pass the initialization test" in {
      new ListSequence(null) must throwA(
        new IllegalArgumentException(
          "requirement failed: The wrapped list cannot be null"
          )
        )
      var lst = new ArrayList[String]
      lst add "anything"
      lst add null
      lst add "any other thing"
      new ListSequence(lst) must throwA(
        new IllegalArgumentException(
          "requirement failed: The list cannot contain null value"
          )
        )
      lst remove null
      lst add "anything"
      new ListSequence(lst) must throwA(
        new IllegalArgumentException(
          "requirement failed: The list cannot contain duplicates"
          )
        )
    }


    var lst = new ArrayList[String]
    lst add "one"
    lst add "two"
    lst add "three"
    lst add "four"
    lst add "five"
    lst add "etc"

    "Pass the basic navigation test" in {
      var seq = new ListSequence(lst)
      seq.current must be equalTo "one"
      seq next "ono" must beNull
      seq.current must be equalTo "one"
      seq next "one" must be equalTo "two"
      seq current "thres" must beNull
      seq current "one" must be equalTo "one"

      var i = 0
      var pk = seq.current
      while (pk != null) {
        lst contains pk must be equalTo true
        pk = seq next pk
        i += 1
      }
      lst.size must be equalTo i
      seq.current must beNull
    }

    "Pass the inverse test" in {
      var seq = new ListSequence(lst, true)
      seq.isReverse must be equalTo true
      seq.current must be equalTo "etc"
      seq.next("etc") must be equalTo "five"
      seq.next("feur") must beNull
      seq.next("four") must be equalTo "three"
      seq.current("TWO") must beNull
      seq.current must be equalTo "three"
      seq.next("three") must be equalTo "two"
      seq next "two" must be equalTo "one"
      seq.next("one") must beNull
    }
  }
}