package org.agilewiki.blip
package seq
package filterSafeSeq

import bind._
import org.specs.SpecificationWithJUnit

class FilterSafe extends MessageLogic {
  override def func(target: BindActor, msg: AnyRef, rf: Any => Unit)(implicit sender: ActiveActor) {
    val kvPair = msg.asInstanceOf[KVPair[Int, Int]]
    rf(kvPair.value % 2 == 0)
  }
}

class FilterSafeSeqTest extends SpecificationWithJUnit {
  "FilterSeqTest" should {
    "filter" in {
      val range = new Range(0, 4)
      val filter = new FilterSafeSeq(range, new FilterSafe)
      Future(filter, Loop((key: Int, value: Int) => println(key+" "+value)))
    }
  }
}
