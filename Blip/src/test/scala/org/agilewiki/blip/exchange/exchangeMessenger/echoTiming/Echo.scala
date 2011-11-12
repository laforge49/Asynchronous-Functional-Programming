package org.agilewiki.blip
package exchange
package exchangeMessenger.echoTiming

import messenger._

class Echo(threadManager: ThreadManager)
  extends ExchangeMessenger(threadManager)
  with ExchangeMessengerSource {

  override def messageListDestination: MessageListDestination[ExchangeMessengerMessage] = this

  override def exchangeReq(req: ExchangeMessengerRequest) {
    val sender = req.sender
    putTo(sender.messageListDestination, new ExchangeMessengerResponse)
  }

  def exchangeRsp(rsp: ExchangeMessengerResponse) {}
}
