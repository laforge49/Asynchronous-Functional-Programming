package org.agilewiki.blip
package exchange
package echoTiming

import messenger._
import java.util.concurrent.Semaphore

class Sender(c: Int, threadManager: ThreadManager)
  extends Exchange(threadManager)
  with ExchangeActor {

  val done = new Semaphore(0)
  val echo = new Echo(threadManager)
  var count = 0
  var i = 0
  var t0 = 0L

  count = c
  i = c
  t0 = System.currentTimeMillis
  echo.sendReq(echo, new ExchangeRequest(this), this)
  flushPendingMsgs

  def finished {
    done.acquire
  }

  override def exchange = this

  override def processRequest {}

  override def processResponse(rsp: ExchangeResponse) {
    if (i > 0) {
      i -= 1
      echo.sendReq(echo, new ExchangeRequest(this), this)
    } else {
      val t1 = System.currentTimeMillis
      if (t1 != t0) println("msgs per sec = " + (count * 2L * 1000L / (t1 - t0)))
      threadManager.close
      done.release
    }
  }
}
