package org.agilewiki.blip.messenger
package buffered.burstTiming

import java.util.concurrent.Semaphore

class Sender(c: Int, b: Int, threadManager: ThreadManager)
  extends MessageProcessor[Any] {

  val done = new Semaphore(0)
  val messenger = new BufferedMessenger[Any](this, threadManager)
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
  messenger.putTo(echo.messenger, messenger)
  messenger.flushPendingMsgs

  def finished {
    done.acquire
  }

  override def processMessage(message: Any) {
    message match {
      case msg: MessageListDestination[Any] => {
        if (r > 1) {
          r -= 1
        } else if (i > 0) {
          i -= 1
          j = burst
          r = burst
          while (j > 0) {
            j -= 1
            messenger.putTo(msg, messenger)
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
  }

  override def haveMessage {
    messenger.poll
  }
}
