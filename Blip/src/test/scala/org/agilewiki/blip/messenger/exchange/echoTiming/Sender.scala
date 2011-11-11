package org.agilewiki.blip.messenger
package exchange.echoTiming

import java.util.concurrent.Semaphore

class Sender(c: Int, threadManager: ThreadManager)
  extends ExchangeMessenger(threadManager)
  with MessageSource {

  val done = new Semaphore(0)
  val echo = new Echo(threadManager)
  var count = 0
  var i = 0
  var t0 = 0L

  count = c
  i = c
  t0 = System.currentTimeMillis
  putTo(echo, new ExchangeRequest(this))
  flushPendingMsgs

  def finished {
    done.acquire
  }

  override def messageListDestination: MessageListDestination[ExchangeMessage] = this

  override def exchangeReq(req: ExchangeRequest) {}

  override def exchangeRsp(rsp: ExchangeResponse) {
    if (i > 0) {
      i -= 1
      putTo(echo, new ExchangeRequest(this))
    } else {
      val t1 = System.currentTimeMillis
      if (t1 != t0) println("msgs per sec = " + (count * 2L * 1000L / (t1 - t0)))
      threadManager.close
      done.release
    }
  }
}
