package org.agilewiki.blip
package bind
package echoTiming

import messenger._
import org.specs.SpecificationWithJUnit

class EchoTimingTest extends SpecificationWithJUnit {
  "EchoTimingTest" should {
    "time messages" in {
      val c = 10//0000000
      val echo = new Echo
      val sender = new Sender(echo)
      val doIt = new DoIt(c) //c should be at least 100 million
      Future(sender, doIt) //83 nanoseconds per message
    }
  }
}
