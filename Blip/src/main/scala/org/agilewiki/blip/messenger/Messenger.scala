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

import java.util.concurrent.atomic.AtomicBoolean
import annotation.tailrec

/**
 * A Messenger receives messages, queues them, and then processes them on another thread.
 */
class Messenger[T](dispatcher: MessengerDispatch[T], threadManager: ThreadManager)
  extends Runnable {

  private val queue = new ConcurrentLinkedBlockingQueue[T]
  private val running = new AtomicBoolean
  private var incomingMessage: T = null.asInstanceOf[T]

  /**
   * The isEmpty method returns true when there are no messages to be processed,
   * though the results may not always be correct due to concurrency issues.
   */
  def isEmpty = queue.size() == 0

  /**
   * The put method adds a message to the queue of messages to be processed.
   */
  def put(message: T) {
    queue.put(message)
    if (running.compareAndSet(false, true)) {
      threadManager.process(this)
    }
  }

  /**
   * The poll method processes any messages in the queue.
   * True is returned if any messages were processed.
   */
  def poll: Boolean = {
    if (incomingMessage == null) incomingMessage = queue.poll
    if (incomingMessage == null) return false
    while (incomingMessage != null) {
      val msg = incomingMessage
      incomingMessage = null.asInstanceOf[T]
      dispatcher.processMessage(msg)
      incomingMessage = queue.poll
    }
    true
  }

  /**
   * The run method is used to process the messages in the message queue.
   * Each message is in turn processed using the MessageDispatcher.
   */
  @tailrec final override def run {
    incomingMessage = queue.poll
    if (incomingMessage == null) {
      dispatcher.flushPendingMsgs
      running.set(false)
      if (queue.peek == null || !running.compareAndSet(false, true)) return
    }
    dispatcher.haveMessage
    run
  }
}
