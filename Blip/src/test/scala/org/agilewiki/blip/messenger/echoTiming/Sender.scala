package org.agilewiki.blip.messenger
package echoTiming

import java.util.concurrent.Semaphore

class Sender(threadManager: ThreadManager)
  extends MessageProcessor[Any] {

  val done = new Semaphore(0)
  val messenger = new Messenger[Any](this, threadManager)
  val echo = new Echo(threadManager)
  var count = 0
  var i = 0
  var t0 = 0L

  def finished {
    done.acquire
  }

  def put(message: Any) {
    messenger.put(message)
  }

  override def processMessage(message: Any) {
    message match {
      case c: Int => {
        count = c
        i = c
        t0 = System.currentTimeMillis
        echo.put(this)
      }
      case msg: Echo => {
        if (i > 0) {
          i -= 1
          msg.put(this)
        } else {
          val t1 = System.currentTimeMillis
          if (t1 != t0) println("msgs per sec = " + (count * 2L * 1000L / (t1 - t0)))
          threadManager.close
          done.release
        }
      }
    }
  }

  override def haveMessage {
    messenger.poll
  }
}
