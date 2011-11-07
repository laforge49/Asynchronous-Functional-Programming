/*
 * Copyright 2011 Bill La Forge
 *
 * This file is part of AgileWiki and is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License (LGPL) as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 * or navigate to the following url http://www.gnu.org/licenses/lgpl-2.1.txt
 *
 * Note however that only Scala, Java and JavaScript files are being covered by LGPL.
 * All other files are covered by the Common Public License (CPL).
 * A copy of this license is also included and can be
 * found as well at http://www.opensource.org/licenses/cpl1.0.txt
 */
package org.agilewiki.blip.messenger

import java.util.ArrayList

class BufferedMessenger[T](messageProcessor: MessageProcessor[T], threadManager: ThreadManager)
  extends Buffered[T] with MessengerDispatch[ArrayList[T]] {
  val messenger = new Messenger[ArrayList[T]](this, threadManager)
  val pending = new java.util.HashMap[Buffered[T], ArrayList[T]]

  override def putBuffered(bufferedMessage: ArrayList[T]) {
    messenger.put(bufferedMessage)
  }

  /**
   * The isEmpty method returns true when there are no messages to be processed,
   * though the results may not always be correct due to concurrency issues.
   */
  def isEmpty = messenger.isEmpty

  /**
   * The poll method processes any messages in the queue.
   */
  def poll {
    if (messenger.poll) flushPendingMsgs
  }

  /**
   * The processMessage method is used to process an incoming message.
   */
  override def processMessage(bufferedMessage: ArrayList[T]) {
    var i = 0
    while (i < bufferedMessage.size){
      messageProcessor.processMessage(bufferedMessage.get(i))
      i -= 1
    }
  }

  override def haveMessage {
    messageProcessor.haveMessage
  }

  /**
   * The flushPendingMsgs is called when there are no pending incoming messages to process.
   * This method is used when outgoing messages are buffered.
   */
  override def flushPendingMsgs {
    if (isEmpty && !pending.isEmpty) {
      val it = pending.keySet.iterator
      while (it.hasNext) {
        val buffered = it.next
        val bufferedMessage = pending.get(buffered)
        buffered.putBuffered(bufferedMessage)
      }
      pending.clear
    }
  }

  def putTo(buffered: Buffered[T], message: T) {
    var bufferedMessage = pending.get(buffered)
    if (bufferedMessage == null) {
      bufferedMessage = new ArrayList[T]
      pending.put(buffered, bufferedMessage)
    }
    bufferedMessage.add(message)
    if (bufferedMessage.size > 63) {
      pending.remove(buffered)
      buffered.putBuffered(bufferedMessage)
    }
  }
}