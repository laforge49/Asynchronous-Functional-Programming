package org.agilewiki.blip
package bind
package dualEchoTiming

import org.specs.SpecificationWithJUnit

class DualEchoTimingTest extends SpecificationWithJUnit {
  "DualEchoTimingTest" should {
    "time messages" in {
      val c = 20000
      val b = 20000
      val echo1 = new Echo
      val echo2 = new Echo
      val sender1 = new Sender(echo1)
      val sender2 = new Sender(echo2)
      val driver = new Driver(sender1, sender2)
      val mailboxFactory = new MailboxFactory
      sender1.setExchangeMessenger(mailboxFactory.newAsyncMailbox)
      sender2.setExchangeMessenger(mailboxFactory.newAsyncMailbox)
      driver.setExchangeMessenger(mailboxFactory.newAsyncMailbox)
      val doIt = new DoIt(c, b)
      try {
        Future(driver, doIt) //51 nanoseconds per message==63% faster than EchoTimingTest
      } finally {
        mailboxFactory.close
      }
    }
  }
}
