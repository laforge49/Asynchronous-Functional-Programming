package org.agilewiki.blip
package seq
package list

import org.specs.SpecificationWithJUnit

class ListTest extends SpecificationWithJUnit {
  "ListSeq" should {
    "support first, current and next" in {
      val fact = new java.util.ArrayList[Int]
      fact.add(0)
      fact.add(1)
      fact.add(2)
      fact.add(6)
      fact.add(24)
      val factSeq = new ListSeq(fact)
      println(Future(factSeq, First()))
      println(Future(factSeq, Current(3)))
      println(Future(factSeq, Next(3)))
      println(Future(factSeq, Next(4)))
    }
    "work asynchronously, too" in {
      val fact = new java.util.ArrayList[Int]
      fact.add(0)
      fact.add(1)
      fact.add(2)
      fact.add(6)
      fact.add(24)
      val factSeq = new ListSeq(fact)
      factSeq.setMailbox(new ReactorMailbox)
      println(Future(factSeq, Next(4)))
      fact.add(120)
      println(Future(factSeq, Next(4)))
    }
  }
}
