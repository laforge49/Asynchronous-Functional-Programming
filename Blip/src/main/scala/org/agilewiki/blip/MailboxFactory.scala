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
import annotation.tailrec
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.{AtomicReference, AtomicBoolean}

class MailboxFactory(threadManager: ThreadManager = new MailboxThreadManager) {

  def process(task: Runnable) {
    threadManager.process(task)
  }

  def close {threadManager.close}

  def asyncMailbox = {
    new AsyncMailbox(this)
  }

  def syncMailbox = {
    new SyncMailboxBase(this)
  }
}

class AsyncMailbox(mailboxFactory: MailboxFactory)
  extends Mailbox with Runnable {
  protected val queue = new ConcurrentLinkedBlockingQueue[ArrayList[MailboxMsg]]
  private val running = new AtomicBoolean

  def asyncMailbox = mailboxFactory.asyncMailbox

  def syncMailbox = mailboxFactory.syncMailbox

  def isMailboxEmpty = queue.size() == 0

  def _send(blkmsg: ArrayList[MailboxMsg]) {
    queue.put(blkmsg)
    if (running.compareAndSet(false, true)) mailboxFactory.process(this)
  }

  @tailrec final override def run {
    var msgblk = queue.poll
    if (msgblk == null) {
      running.set(false)
      if (queue.peek == null || !running.compareAndSet(false, true)) return
    }
    receive(msgblk)
    run
  }
}

class SyncMailboxBase(mailboxFactory: MailboxFactory)
  extends AsyncMailbox(mailboxFactory) {
  val atomicControl = new AtomicReference[Mailbox]
  val idle = new Semaphore(1)

  override def control = atomicControl.get

  override protected def receive(blkmsg: ArrayList[MailboxMsg]) {
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
      bm = queue.poll()
    }
  }

  protected def _sendReq(req: MailboxReq) {
    curMsg = req
    req.fastSend = true
    req.binding.process(this, req)
    val blkmsg = queue.poll()
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
