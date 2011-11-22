package org.agilewiki.blip
package seq
package navMap

import org.specs.SpecificationWithJUnit
import bind._

class NavMapTest extends SpecificationWithJUnit {
  "NavMapSeq" should {
    "support first, current and next" in {
      val alphabet = new java.util.TreeMap[String, String]
      alphabet.put("a", "Apple")
      alphabet.put("b", "Boy")
      alphabet.put("c", "Cat")
      val alphabetSeq = new NavMapSeq(alphabet)
      println(Future(alphabetSeq, First()))
      println(Future(alphabetSeq, Current("")))
      println(Future(alphabetSeq, Next("a")))
      println(Future(alphabetSeq, Current("d")))
      println(Future(alphabetSeq, Next("aa")))
    }
  }
}
