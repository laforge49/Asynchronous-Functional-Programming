package org.agilewiki.blip
package exchange
package echoTiming

import messenger._

class Echo(threadManager: ThreadManager)
  extends Exchange(threadManager)
  with ExchangeMessengerActor {

  override def exchangeMessenger = this

  override def processRequest {
    reply(null)
  }
}
