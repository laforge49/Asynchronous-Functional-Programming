package org.agilewiki.blip
package exchange
package exchangeMessenger.burstTiming

import messenger._
import org.specs.SpecificationWithJUnit

class BurstTimingTest extends SpecificationWithJUnit {
  "BurstTimingTest" should {
    "time messages" in {
      val threadManager = new MessengerThreadManager
      val c = 10//000
      val b = 10//000
      val sender = new Sender(c, b, threadManager)
      sender.finished //about 103 nanoseconds per message
    }
  }
}
