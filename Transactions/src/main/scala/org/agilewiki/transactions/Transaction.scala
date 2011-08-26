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
package transactions

import blip._

abstract class Transaction(messageFunction: (AnyRef, Any => Unit) => Unit)
  extends Bound(messageFunction) {
  def level: Int

  def maxCompatibleLevel: Int

  override def process(mailbox: Mailbox, mailboxReq: MailboxReq) {
    val transactionProcessor = mailboxReq.target.asInstanceOf[TransactionProcessor]
    transactionProcessor.addPending(mailboxReq)
  }

  def processTransaction(mailbox: Mailbox, mailboxReq: MailboxReq) {
    val transactionProcessor = mailboxReq.target.asInstanceOf[TransactionProcessor]
    mailbox.curMsg = mailboxReq
    mailbox.exceptionFunction = mailbox.reqExceptionFunction
    mailbox.transactionContext = null
    try {
      if (transactionProcessor.isInvalid) throw new IllegalStateException
      messageFunction(mailboxReq.req, {
        rsp1: Any => {
          transactionProcessor.removeActive(mailboxReq)
          mailbox.reply(rsp1)
          transactionProcessor.runPending
        }
      })
    } catch {
      case ex: Exception => {
        transactionProcessor.removeActive(mailboxReq)
        mailbox.reply(ex)
      }
    }
  }
}

class Query(messageFunction: (AnyRef, Any => Unit) => Unit)
  extends Transaction(messageFunction) {
  override def level = 5
  override def maxCompatibleLevel = 5
}

class Update(messageFunction: (AnyRef, Any => Unit) => Unit)
  extends Transaction(messageFunction) {
  override def level = 10
  override def maxCompatibleLevel = 0
}