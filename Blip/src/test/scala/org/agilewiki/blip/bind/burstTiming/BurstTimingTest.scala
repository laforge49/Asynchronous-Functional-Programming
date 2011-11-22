package org.agilewiki.blip
package bind
package burstTiming

import org.specs.SpecificationWithJUnit

class BurstTimingTest extends SpecificationWithJUnit {
  "BurstTimingTest" should {
    "time messages" in {
      val c = 10000
      val b = 10000
      val echo = new Echo
      val sender = new Sender(echo)
      val doIt = new DoIt(c, b)
      Future(sender, doIt) //84 nanoseconds per message
    }
  }
}
