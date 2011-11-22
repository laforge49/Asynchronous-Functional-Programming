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

import bind._
import exchange._

class ChainFactory(chainFunction: (AnyRef, Chain) => Unit)
  extends MessageLogic {
  override def func(target: BindActor, msg: AnyRef, rf: Any => Unit)
                   (implicit srcActor: ActiveActor) {
    val chain = new Chain
    chainFunction(msg, chain)
    target.asInstanceOf[Actor](chain)(rf)
  }
}

abstract class BoundTransaction(messageFunction: (AnyRef, Any => Unit) => Unit)
  extends QueuedLogic(messageFunction) {
  def level: Int

  def maxCompatibleLevel: Int

  override def func(target: BindActor, msg: AnyRef, rf: Any => Unit)
                   (implicit srcActor: ActiveActor) {
    if (rf == null) throw new IllegalArgumentException("transaction requests require a response function")
    val srcExchangeMessenger = {
      if (srcActor == null) null
      else srcActor.bindActor.exchangeMessenger
    }
    val targetExchangeMessenger = target.exchangeMessenger
    if (srcExchangeMessenger == null || targetExchangeMessenger == null) throw new UnsupportedOperationException(
      "Transactions require that both the requesting and target actors have mailboxes."
    )
    val responseFunction: Any => Unit = {
      rsp => {
        rsp match {
          case rsp: Exception => srcExchangeMessenger.curReq.asInstanceOf[MailboxReq].
            exceptionFunction(rsp, srcExchangeMessenger.asInstanceOf[Mailbox])
          case rsp => try {
            rf(rsp)
          } catch {
            case ex: Exception => srcExchangeMessenger.curReq.asInstanceOf[MailboxReq].
              exceptionFunction(ex, srcExchangeMessenger.asInstanceOf[Mailbox])
          }
        }
      }
    }
    enqueueRequest(srcExchangeMessenger.asInstanceOf[Mailbox], target.asInstanceOf[Actor], msg, responseFunction)
  }

  override def process(exchange: Exchange, bindRequest: BindRequest) {
    val transactionProcessor = bindRequest.target.asInstanceOf[Actor]
    transactionProcessor.addPendingTransaction(bindRequest.asInstanceOf[MailboxReq])
  }

  def processTransaction(mailbox: Mailbox, mailboxReq: MailboxReq)

  def processTransaction(mailbox: Mailbox,
                         mailboxReq: MailboxReq,
                         transactionContext: TransactionContext) {
    val transactionProcessor = mailboxReq.target.asInstanceOf[Actor]
    mailbox.setCurrentRequest(mailboxReq)
    mailboxReq.transactionContext = transactionContext
    try {
      if (transactionProcessor.isInvalid) throw new IllegalStateException
      messageFunction(mailboxReq.req, {
        rsp1: Any => {
          transactionProcessor.removeActiveTransaction
          mailbox.reply(rsp1)
          transactionProcessor.runPendingTransaction
        }
      })
    } catch {
      case ex: Exception => {
        transactionProcessor.removeActiveTransaction
        mailbox.reply(ex)
      }
    }
  }
}

class Query(messageFunction: (AnyRef, Any => Unit) => Unit)
  extends BoundTransaction(messageFunction) {
  override def level = 5

  override def maxCompatibleLevel = 5

  override def processTransaction(mailbox: Mailbox, mailboxReq: MailboxReq) {
    processTransaction(mailbox, mailboxReq, new QueryContext)
  }
}

class Update(messageFunction: (AnyRef, Any => Unit) => Unit)
  extends BoundTransaction(messageFunction) {
  override def level = 10

  override def maxCompatibleLevel = 0

  override def processTransaction(mailbox: Mailbox, mailboxReq: MailboxReq) {
    processTransaction(mailbox, mailboxReq, new UpdateContext)
  }
}
