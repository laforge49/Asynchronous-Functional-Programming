package org.agilewiki
package util
package lite
package seq

import org.specs.SpecificationWithJUnit
import java.util.TreeSet

class FilteredTest extends SpecificationWithJUnit {
  "Filtered" should {

    "Pass the basic always true filter test" in {
      val set = new TreeSet[String]
      val iSeq = new LiteNavigableSetSeq(new LiteReactor, set)
      FutureSeq(iSeq).isEmpty must be equalTo true
      val seq = new LiteFilterSeq(new LiteReactor, iSeq, (x: String) => true)
      FutureSeq(seq).isEmpty must be equalTo true
      set.add("123")
      FutureSeq(iSeq).next("0") must be equalTo "123"
      FutureSeq(seq).next("0") must be equalTo "123"
      FutureSeq(iSeq).current("123") must be equalTo "123"
      FutureSeq(seq).current("123") must be equalTo "123"
    }

    "Pass the basic always false filter test" in {
      var set = new TreeSet[String]
      val iSeq = new LiteNavigableSetSeq(new LiteReactor, set)
      FutureSeq(iSeq).isEmpty must be equalTo true
      val seq = new LiteFilterSeq(new LiteReactor, iSeq, (x: String) => false)
      FutureSeq(seq).isEmpty must be equalTo true
      set.add("123")
      FutureSeq(iSeq).first must be equalTo "123"
      FutureSeq(iSeq).current("123") must be equalTo "123"
      FutureSeq(seq).isEmpty must be equalTo true
    }

    "Pass the test of a filtering function" in {
      var set = new TreeSet[String]
      set add "00"
      set add "01"
      set add "10"
      set add "11"
      var iSeq = new LiteNavigableSetSeq(new LiteReactor, set)
      FutureSeq(iSeq).first must be equalTo "00"
      var seq = new LiteFilterSeq(new LiteReactor, iSeq, (x: String) => x endsWith "1")
      FutureSeq(seq).first must be equalTo "01"
      var pk = FutureSeq(seq).firstKey
      while (pk != null) {
        pk endsWith "1" must be equalTo true
        pk = FutureSeq(seq).nextKey(pk)
      }

      FutureSeq(seq).current("11") must be equalTo "11"
      FutureSeq(seq).current("00") must be equalTo "01"
      FutureSeq(seq).isCurrentEnd("abc") must be equalTo true

      FutureSeq(iSeq).current("0") must be equalTo "00"
      seq = new LiteFilterSeq(new LiteReactor, iSeq, (x: String) => x startsWith ".")
      FutureSeq(seq).isEmpty must be equalTo true
      FutureSeq(iSeq).next(".") must be equalTo "00"
    }
  }
}
