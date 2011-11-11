package org.agilewiki.blip.messenger
package exchange.burstTiming

import java.util.concurrent.Semaphore

class Sender(c: Int, b: Int, threadManager: ThreadManager)
  extends ExchangeMessenger(threadManager)
  with MessageSource {

  val done = new Semaphore(0)
  val echo = new Echo(threadManager)
  var count = 0
  var i = 0
  var burst = 0
  var j = 0
  var r = 0
  var t0 = 0L

  count = c
  i = c
  burst = b
  r = 0
  t0 = System.currentTimeMillis
  putTo(echo, new ExchangeRequest(this))
  flushPendingMsgs

  def finished {
    done.acquire
  }

  override def messageListDestination: MessageListDestination[ExchangeMessage] = this

  override def exchangeReq(req: ExchangeRequest) {}

  override def exchangeRsp(rsp: ExchangeResponse) {
    if (r > 1) {
      r -= 1
    } else if (i > 0) {
      i -= 1
      j = burst
      r = burst
      while (j > 0) {
        j -= 1
        putTo(echo, new ExchangeRequest(this))
      }
    } else {
      val t1 = System.currentTimeMillis
      if (t1 != t0) println("msgs per sec = " +
        (count * burst * 2L * 1000L / (t1 - t0)))
      threadManager.close
      done.release
    }
  }
}
