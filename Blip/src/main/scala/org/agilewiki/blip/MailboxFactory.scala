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
package org.agilewiki
package blip

import java.util.ArrayList
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicReference
import messenger._

/**
 * MailboxFactory is used to create asyncMailbox and syncMailbox objects.
 * It also provides the ThreadManager used by these objects.
 */
class MailboxFactory(_threadManager: ThreadManager = new MessengerThreadManager) {

  /**
   * Returns the ThreadManager used by the async and sync mailbox objects.
   */
  def threadManager = _threadManager

  /**
   * Stops all the threads of the ThreadManager as they become idle.
   */
  def close {threadManager.close}

  /**
   * Creates an asynchronous mailbox.
   */
  def asyncMailbox = {
    new AsyncMailbox(this)
  }

  /**
   * Creates a synchronous mailbox.
   */
  def syncMailbox = {
    new SyncMailboxBase(this)
  }
}

class AsyncMailbox(mailboxFactory: MailboxFactory)
  extends Mailbox with MessengerDispatch[ArrayList[MailboxMsg]] {

  val messenger = new Messenger(this, mailboxFactory.threadManager)

  def asyncMailbox = mailboxFactory.asyncMailbox

  def syncMailbox = mailboxFactory.syncMailbox

  override def isMailboxEmpty = messenger.isEmpty

  def poll = messenger.poll

  override def _send(blkmsg: ArrayList[MailboxMsg]) {
    messenger.put(blkmsg)
  }
}

class SyncMailboxBase(mailboxFactory: MailboxFactory)
  extends AsyncMailbox(mailboxFactory) {
  val atomicControl = new AtomicReference[Mailbox]
  val idle = new Semaphore(1)

  override def control = atomicControl.get

  override def receive(blkmsg: ArrayList[MailboxMsg]) {
    while (!atomicControl.compareAndSet(null, this)) {
      idle.acquire
      idle.release
    }
    try {
      _receive(blkmsg)
      flushPendingMsgs
    } finally {
      atomicControl.set(null)
    }
  }

  override def sendReq(targetActor: Actor,
                       req: MailboxReq,
                       srcMailbox: Mailbox) {
    val controllingMailbox = srcMailbox.control
    if (controllingMailbox == control) {
      _sendReq(req)
    } else if (!atomicControl.compareAndSet(null, controllingMailbox)) {
      srcMailbox.addPending(targetActor, req)
    } else {
      idle.acquire
      try {
        _sendReq(req)
      } finally {
        atomicControl.set(null)
        idle.release
      }
    }
  }

  override protected def _receive(blkmsg: ArrayList[MailboxMsg]) {
    var bm = blkmsg
    while (bm != null) {
      super._receive(bm)
      bm = poll
    }
  }

  protected def _sendReq(req: MailboxReq) {
    curMsg = req
    req.fastSend = true
    req.binding.process(this, req)
    val blkmsg = poll
    if (blkmsg != null) {
      _receive(blkmsg)
      flushPendingMsgs
    }
  }

  override protected def sendReply(sender: MsgSrc,
                                   rspMsg: MailboxRsp,
                                   senderMailbox: Mailbox) {
    if (currentRequestMessage.fastSend) {
      senderMailbox.rsp(rspMsg)
    } else super.sendReply(sender, rspMsg, senderMailbox)
  }
}
