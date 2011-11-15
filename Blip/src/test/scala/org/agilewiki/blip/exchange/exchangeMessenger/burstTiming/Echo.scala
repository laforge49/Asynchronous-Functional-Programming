package org.agilewiki.blip
package exchange
package exchangeMessenger.burstTiming

import messenger._

class Echo(threadManager: ThreadManager)
  extends ExchangeMessenger(threadManager)
  with ExchangeMessengerActor {

  override def exchangeMessenger = this

  override protected def processRequest {
    reply(null)
  }
}
