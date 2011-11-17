package org.agilewiki.blip
package exchange
package echoTiming

import messenger._
import org.specs.SpecificationWithJUnit

class EchoTimingTest extends SpecificationWithJUnit {
  "EchoTimingTest" should {
    "time messages" in {
      val threadManager = new MessengerThreadManager
      val c = 10//0000000
      val sender = new Sender(c, threadManager) //c should be at least 100 million
      sender.finished //about 94 nanoseconds per message
    }
  }
}
