package org.agilewiki.blip
package seq
package navSet

import org.specs.SpecificationWithJUnit

class NavSetTest extends SpecificationWithJUnit {
  "NavSetSeq" should {
    "support first, current and next" in {
      val fruit = new java.util.TreeSet[String]
      fruit.add("Apple")
      fruit.add("Orange")
      fruit.add("Pear")
      val fruitSeq = new NavSetSeq(null, null, fruit)
      println(Future(fruitSeq, First()))
      println(Future(fruitSeq, Current("Bananna")))
      println(Future(fruitSeq, Next("Orange")))
      println(Future(fruitSeq, Current("Orange")))
      println(Future(fruitSeq, Next("Pear")))
    }
  }
}
