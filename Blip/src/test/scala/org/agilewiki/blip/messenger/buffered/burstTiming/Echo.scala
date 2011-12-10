package org.agilewiki.blip.messenger
package buffered.burstTiming

class Echo(threadManager: ThreadManager)
  extends MessageProcessor[Any] {

  val messenger = new BufferedMessenger[Any](threadManager)
  messenger.setMessageProcessor(this)

  override def processMessage(message: Any) {
    message match {
      case msg: MessageListDestination[Any] => {
        messenger.putTo(msg, messenger)
      }
    }
  }

  override def haveMessage {
    messenger.poll
  }
}
