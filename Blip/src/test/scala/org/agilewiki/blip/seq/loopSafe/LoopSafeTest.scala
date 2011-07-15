package org.agilewiki.blip
package seq
package loopSafe

import org.specs.SpecificationWithJUnit

class SumSafe extends Safe {
  var sum = 0

  override def func(msg: AnyRef, rf: Any => Unit)(implicit sender: ActiveActor) {
    val nvPair = msg.asInstanceOf[KVPair[Int, Int]]
    sum += nvPair.value
    rf(null)
  }
}

class LoopSafeTest extends SpecificationWithJUnit {
  "LoopSafeTest" should {
    "sum" in {
      val sumSafe = new SumSafe
      val rangeSeq = Range(1, 4)
      Future(rangeSeq, LoopSafe(sumSafe))
      println(sumSafe.sum)
    }
  }
}
