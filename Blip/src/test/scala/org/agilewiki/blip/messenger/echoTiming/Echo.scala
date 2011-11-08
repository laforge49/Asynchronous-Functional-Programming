package org.agilewiki.blip.messenger
package echoTiming

class Echo(threadManager: ThreadManager)
  extends MessageProcessor[Any] {

  val messenger = new Messenger[Any](this, threadManager)

  def put(message: Any) {
    messenger.put(message)
  }

  override def processMessage(message: Any) {
    message match {
      case msg: Sender => msg.put(this)
    }
  }

  override def haveMessage {
    messenger.poll
  }
}
