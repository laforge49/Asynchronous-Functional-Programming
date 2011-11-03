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
import java.util.concurrent.{ConcurrentLinkedQueue, Semaphore, ThreadFactory}

class MailboxFactory(threadManager: ThreadManager = new MailboxThreadManager) {

  def process(task: Runnable) {
    threadManager.process(task)
  }

  def close {threadManager.close}

  def asyncMailbox = {
    new AsyncMailboxBase(this)
  }

  def syncMailbox = {
    new SyncMailboxBase(this)
  }
}

abstract class MailboxBase(mailboxFactory: MailboxFactory) extends Runnable {
  protected val queue = new ConcurrentLinkedBlockingQueue[ArrayList[MailboxMsg]]

  def asyncMailbox = mailboxFactory.asyncMailbox

  def syncMailbox = mailboxFactory.syncMailbox

  def isMailboxEmpty = queue.size() == 0

  def _send(blkmsg: ArrayList[MailboxMsg]) {
    queue.put(blkmsg)
    mailboxFactory.process(this)
  }

  @tailrec final override def run {
    var msgblk = queue.poll
    if (msgblk == null) return
    receive(msgblk)
    run
  }

  protected def receive(blkmsg: ArrayList[MailboxMsg])
}

class AsyncMailboxBase(mailboxFactory: MailboxFactory) extends MailboxBase(mailboxFactory) with Mailbox

class SyncMailboxBase(mailboxFactory: MailboxFactory) extends MailboxBase(mailboxFactory) with SyncMailbox {

  override protected def _receive(blkmsg: ArrayList[MailboxMsg]) {
    var bm = blkmsg
    while (bm != null) {
      super._receive(bm)
      bm = queue.poll()
    }
  }

  override protected def _sendReq(req: MailboxReq) {
    super._sendReq(req)
    val blkmsg = queue.poll()
    if (blkmsg != null) {
      _receive(blkmsg)
      flushPendingMsgs
    }
  }
}
