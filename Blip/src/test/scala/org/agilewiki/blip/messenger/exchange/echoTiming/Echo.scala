package org.agilewiki.blip.messenger
package exchange.echoTiming

class Echo(threadManager: ThreadManager)
  extends ExchangeMessenger(threadManager)
  with MessageSource {

  override def messageListDestination: MessageListDestination[ExchangeMessage] = this

  override def exchangeReq(req: ExchangeRequest) {
    val sender = req.sender
    sender.responseFrom(this, new ExchangeResponse)
  }

  def exchangeRsp(rsp: ExchangeResponse) {}
}
