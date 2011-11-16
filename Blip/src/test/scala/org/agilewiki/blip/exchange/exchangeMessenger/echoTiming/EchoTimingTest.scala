package org.agilewiki.blip
package exchange
package exchangeMessenger.echoTiming

import messenger._
import org.specs.SpecificationWithJUnit

class EchoTimingTest extends SpecificationWithJUnit {
  "EchoTimingTest" should {
    "time messages" in {
      val threadManager = new MessengerThreadManager
      val c = 10//000000
      val sender = new Sender(c, threadManager) //c should be at least 10 million
      sender.finished //about 1.5 microseconds per message
    }
  }
}
