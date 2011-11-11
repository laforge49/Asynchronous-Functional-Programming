package org.agilewiki.blip.messenger
package exchange.burstTiming

class Echo(threadManager: ThreadManager)
  extends ExchangeMessenger(threadManager)
  with MessageSource {

  override def messageListDestination: MessageListDestination[ExchangeMessage] = this

  override def exchangeReq(req: ExchangeRequest) {
    val sender = req.sender
    sender.responseFrom(this, new ExchangeResponse)
  }

  override def exchangeRsp(rsp: ExchangeResponse) {}
}
