package org.agilewiki.blip
package seq
package flatmapSafeSeq

import bind._
import org.specs.SpecificationWithJUnit

class MapSafe extends Safe {
  val alphabet = new java.util.HashMap[Int, String]
  alphabet.put(8, "Apple")
  alphabet.put(22, "Boy")
  alphabet.put(5, "Cat")

  override def func(target: BindActor, msg: AnyRef, rf: Any => Unit)(implicit sender: ActiveActor) {
    val kvPair = msg.asInstanceOf[KVPair[Int, Int]]
    rf(alphabet.get(kvPair.value))
  }
}

class FlatmapSafeSeqTest extends SpecificationWithJUnit {
  "FlatmapSafeSeqTest" should {
    "drop null values" in {
      val flatmap = new FlatmapSafeSeq(new Range(0, 10), new MapSafe)
      Future(flatmap, Loop((key: Int, value: String) => println(key+" "+value)))
    }
  }
}
