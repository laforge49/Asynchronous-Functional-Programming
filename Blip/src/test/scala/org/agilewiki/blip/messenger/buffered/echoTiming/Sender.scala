package org.agilewiki.blip.messenger
package buffered.echoTiming

import java.util.concurrent.Semaphore

class Sender(c: Int, threadManager: ThreadManager)
  extends MessageProcessor[Any] {

  val done = new Semaphore(0)
  val messenger = new BufferedMessenger[Any](threadManager)
  messenger.setMessageProcessor(this)
  val echo = new Echo(threadManager)
  var count = 0
  var i = 0
  var t0 = 0L

  count = c
  i = c
  t0 = System.currentTimeMillis
  messenger.putTo(echo.messenger, messenger)
  messenger.flushPendingMsgs

  def finished {
    done.acquire
  }

  override def processMessage(message: Any) {
    message match {
      case msg: MessageListDestination[Any] => {
        if (i > 0) {
          i -= 1
          messenger.putTo(msg, messenger)
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
