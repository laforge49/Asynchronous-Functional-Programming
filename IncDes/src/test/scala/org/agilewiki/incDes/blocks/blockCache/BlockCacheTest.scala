package org.agilewiki
package incDes
package blocks
package blockCache

import blip._
import org.specs.SpecificationWithJUnit

class BlockCacheTest extends SpecificationWithJUnit {
  "BlockCacheTest" should {
    "not overflow memory" in {
      val systemServices = SystemServices(new BlockCacheComponentFactory)
      var i = 0
      val l=10000//0000
      while (i < l) {
        i += 1
        val b = Block(null)
        b.partness(null, i, null)
        Future(systemServices, BlockCacheAdd(b))
      }
    }
  }
}