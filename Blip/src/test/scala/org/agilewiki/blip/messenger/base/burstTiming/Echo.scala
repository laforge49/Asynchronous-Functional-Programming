package org.agilewiki.blip.messenger
package base.burstTiming

class Echo(threadManager: ThreadManager)
  extends MessageProcessor[Any] {

  val messenger = new Messenger[Any](threadManager)
  messenger.setMessageProcessor(this)

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
