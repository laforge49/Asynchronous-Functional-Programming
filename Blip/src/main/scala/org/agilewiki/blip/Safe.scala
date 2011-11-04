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

abstract class Safe {
  def func(target: Actor, msg: AnyRef, rf: Any => Unit)(implicit srcActor: ActiveActor)
}

class SafeConstant(any: Any)
  extends Safe {
  override def func(target: Actor, msg: AnyRef, rf: Any => Unit)(implicit sender: ActiveActor) {
    rf(any)
  }
}

class SafeForward(actor: Actor)
  extends Safe {
  override def func(target: Actor, msg: AnyRef, rf: Any => Unit)(implicit sender: ActiveActor) {
    actor(msg)(rf)
  }
}

class ChainFactory(chainFunction: (AnyRef, Chain) => Unit)
  extends Safe {
  override def func(target: Actor, msg: AnyRef, rf: Any => Unit)(implicit srcActor: ActiveActor) {
    val chain = new Chain
    chainFunction(msg, chain)
    target(chain)(rf)
  }
}

abstract class Bound(messageFunction: (AnyRef, Any => Unit) => Unit) extends Safe {

  def reqFunction = messageFunction

  def process(mailbox: Mailbox, mailboxReq: MailboxReq) {
    mailbox.exceptionFunction = mailbox.reqExceptionFunction
    mailbox.transactionContext = null
    try {
      messageFunction(mailboxReq.req, mailbox.reply)
    } catch {
      case ex: Exception => {
        mailbox.reply(ex)
      }
    }
  }

  def asyncSendReq(srcMailbox: Mailbox,
                   targetActor: Actor,
                   content: AnyRef,
                   responseFunction: Any => Unit) {
    val oldReq = srcMailbox.currentRequestMessage
    val sender = oldReq.target
    val req = new MailboxReq(
      targetActor,
      responseFunction,
      oldReq,
      content,
      this,
      sender)
    targetActor.mailbox.sendReq(targetActor, req, srcMailbox)
  }
}

class BoundFunction(messageFunction: (AnyRef, Any => Unit) => Unit)
  extends Bound(messageFunction) {

  override def func(target: Actor, msg: AnyRef, rf: Any => Unit)
                   (implicit srcActor: ActiveActor) {
    var oldCurMsg: MailboxMsg = null
    var oldExceptionFunction: Exception => Unit = null
    var oldTransactionContext: TransactionContext = null
    val srcMailbox = {
      if (srcActor == null) null
      else {
        srcActor.actor.mailbox
      }
    }
    if (srcMailbox == null) {
      if (target.mailbox != null) {
        println("srcActor = " + srcActor)
        println("srcMailbox = " + srcMailbox)
        println("target = " + target)
        println("targetMailbox = " + target.mailbox)
        throw new UnsupportedOperationException(
          "An immutable actor can only send to another immutable actor."
        )
      }
    } else {
      oldCurMsg = srcMailbox.curMsg
      oldExceptionFunction = srcMailbox.exceptionFunction
      oldTransactionContext = srcMailbox.transactionContext
    }
    val responseFunction: Any => Unit = {
      rsp => {
        if (srcMailbox != null) {
          srcMailbox.curMsg = oldCurMsg
          srcMailbox.exceptionFunction = oldExceptionFunction
          srcMailbox.transactionContext = oldTransactionContext
        }
        rsp match {
          case rsp: Exception => srcMailbox.exceptionFunction(rsp)
          case rsp => try {
            rf(rsp)
          } catch {
            case ex: Exception => srcMailbox.exceptionFunction(ex)
          }
        }
      }
    }
    val targetMailbox = target.mailbox
    if (targetMailbox == null || targetMailbox == srcMailbox) {
      if (responseFunction == null) messageFunction(msg, AnyRef => {})
      else messageFunction(msg, responseFunction)
    } else asyncSendReq(srcMailbox, target, msg, responseFunction)
  }
}

abstract class BoundTransaction(messageFunction: (AnyRef, Any => Unit) => Unit)
  extends Bound(messageFunction) {
  def level: Int

  def maxCompatibleLevel: Int

  override def func(target: Actor, msg: AnyRef, rf: Any => Unit)
                   (implicit srcActor: ActiveActor) {
    if (rf == null) throw new IllegalArgumentException("transaction requests require a response function")
    val srcMailbox = {
      if (srcActor == null) null
      else srcActor.actor.mailbox
    }
    val targetMailbox = target.mailbox
    if (srcMailbox == null || targetMailbox == null) throw new UnsupportedOperationException(
      "Transactions require that both the requesting and target actors have mailboxes."
    )
    val oldCurMsg = srcMailbox.curMsg
    val oldExceptionFunction = srcMailbox.exceptionFunction
    val oldTransactionContext = srcMailbox.transactionContext
    val responseFunction: Any => Unit = {
      rsp => {
          srcMailbox.curMsg = oldCurMsg
          srcMailbox.exceptionFunction = oldExceptionFunction
          srcMailbox.transactionContext = oldTransactionContext
        rsp match {
          case rsp: Exception => srcMailbox.exceptionFunction(rsp)
          case rsp => try {
            rf(rsp)
          } catch {
            case ex: Exception => srcMailbox.exceptionFunction(ex)
          }
        }
      }
    }
    asyncSendReq(srcMailbox, target, msg, responseFunction)
  }

  override def process(mailbox: Mailbox, mailboxReq: MailboxReq) {
    val transactionProcessor = mailboxReq.target
    transactionProcessor.addPendingTransaction(mailboxReq)
  }

  def processTransaction(mailbox: Mailbox, mailboxReq: MailboxReq)

  def processTransaction(mailbox: Mailbox, mailboxReq: MailboxReq, transactionContext: TransactionContext) {
    val transactionProcessor = mailboxReq.target
    mailbox.curMsg = mailboxReq
    mailbox.exceptionFunction = mailbox.reqExceptionFunction
    mailbox.transactionContext = transactionContext
    try {
      if (transactionProcessor.isInvalid) throw new IllegalStateException
      messageFunction(mailboxReq.req, {
        rsp1: Any => {
          transactionProcessor.removeActiveTransaction(mailboxReq)
          mailbox.reply(rsp1)
          transactionProcessor.runPendingTransaction
        }
      })
    } catch {
      case ex: Exception => {
        transactionProcessor.removeActiveTransaction(mailboxReq)
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
