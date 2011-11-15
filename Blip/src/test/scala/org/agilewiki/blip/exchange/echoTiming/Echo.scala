package org.agilewiki.blip
package exchange
package echoTiming

import messenger._

class Echo(threadManager: ThreadManager)
  extends Exchange(threadManager)
  with ExchangeActor {

  override def exchange = this

  override def processRequest(req: ExchangeRequest) {
    req.sender.responseFrom(this, new ExchangeResponse)
  }

  override def processResponse(rsp: ExchangeResponse) {}
}
