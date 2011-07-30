package org.agilewiki.blip
package seq
package mapSafeSeq

import org.specs.SpecificationWithJUnit

class MapSafe extends Safe {
  override def func(msg: AnyRef, rf: Any => Unit)(implicit sender: ActiveActor) {
    val kvPair = msg.asInstanceOf[KVPair[Int, Int]]
    rf(kvPair.value * 2)
  }
}

class MapSafeSeqTest extends SpecificationWithJUnit {
  "MapSafeSeqTest" should {
    "map" in {
      val transform = new MapSafeSeq[Int, Int, Int](new Range(0, 3), new MapSafe)
      Future(transform, Loop((key: Int, value: Int) => println(key + " " + value)))
    }
  }
}
