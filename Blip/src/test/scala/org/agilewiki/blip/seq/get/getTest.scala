package org.agilewiki.blip
package seq
package get

import org.specs.SpecificationWithJUnit
import bind._

class GetTest extends SpecificationWithJUnit {
  "GetTest" should {
    "test" in {
      val alphabet = new java.util.TreeMap[String, String]
      alphabet.put("a", "Apple")
      alphabet.put("b", "Boy")
      alphabet.put("c", "Cat")
      val alphabetSeq = new NavMapSeq(alphabet)
      println(Future(alphabetSeq, Get("c")))
      println(Future(alphabetSeq, Get("d")))
    }
  }
}
