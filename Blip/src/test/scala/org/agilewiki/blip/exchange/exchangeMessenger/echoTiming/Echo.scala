package org.agilewiki.blip
package exchange
package exchangeMessenger.echoTiming

import messenger._

class Echo(threadManager: ThreadManager)
  extends ExchangeMessenger(threadManager)
  with ExchangeMessengerActor {

  override def exchangeMessenger = this

  override protected def processRequest {
    curReq.sender.responseFrom(this, new ExchangeMessengerResponse)
  }

  override protected def processResponse(rsp: ExchangeMessengerResponse) {}
}
