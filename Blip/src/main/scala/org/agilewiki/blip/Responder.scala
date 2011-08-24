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

abstract class Bound {
  def send(target: Actor, msg: AnyRef, responseFunction: Any => Unit)(implicit srcActor: ActiveActor)

  def process(mailbox: Mailbox, mailboxReq: MailboxReq, responseFunction: Any => Unit) {
    throw new UnsupportedOperationException
  }
}

class BoundFunction(messageFunction: (AnyRef, Any => Unit) => Unit)
  extends Bound {
  def reqFunction = messageFunction

  override def send(target: Actor, msg: AnyRef, responseFunction: Any => Unit)(implicit srcActor: ActiveActor) {
    val srcMailbox = {
      if (srcActor == null) null
      else srcActor.actor.mailbox
    }
    if (srcMailbox == null && target.mailbox != null) throw new UnsupportedOperationException(
      "An immutable actor can only send to another immutable actor."
    )
    if (target.mailbox == null || target.mailbox == srcMailbox) messageFunction(msg, responseFunction)
    else srcMailbox.send(target, msg, this)(responseFunction)
  }

  override def process(mailbox: Mailbox, mailboxReq: MailboxReq, responseFunction: Any => Unit) {
    mailbox.curMsg = mailboxReq
    val target = mailboxReq.target
    mailbox.exceptionFunction = mailbox.reqExceptionFunction
    try {
      messageFunction(mailboxReq.req, responseFunction)
    } catch {
      case ex: Exception => {
        responseFunction(ex)
      }
    }
  }
}

class BoundAsync(messageFunction: (AnyRef, Any => Unit) => Unit)
  extends Bound {
  override def send(target: Actor, msg: AnyRef, responseFunction: Any => Unit)(implicit srcActor: ActiveActor) {
    val srcMailbox = srcActor.actor.mailbox
    if (srcMailbox == null) throw new UnsupportedOperationException("source actor has no mailbox")
    if (target.mailbox == null) throw new UnsupportedOperationException("target actor has no mailbox")
    srcMailbox.send(target, msg, this)(responseFunction)
  }

  override def process(mailbox: Mailbox, mailboxReq: MailboxReq, responseFunction: Any => Unit) {
    mailbox.curMsg = mailboxReq
    val target = mailboxReq.target
    mailbox.exceptionFunction = mailbox.reqExceptionFunction
    try {
      messageFunction(mailboxReq.req, responseFunction)
    } catch {
      case ex: Exception => {
        responseFunction(ex)
      }
    }
  }
}

trait Responder extends SystemServicesGetter {
  val messageFunctions =
    new java.util.HashMap[Class[_ <: AnyRef], Bound]

  protected def bind(reqClass: Class[_ <: AnyRef], messageFunction: (AnyRef, Any => Unit) => Unit) {
    if (activeActor.actor.opened) throw new IllegalStateException
    messageFunctions.put(reqClass, new BoundFunction(messageFunction))
  }

  protected def bindAsync(reqClass: Class[_ <: AnyRef], messageFunction: (AnyRef, Any => Unit) => Unit) {
    if (activeActor.actor.opened) throw new IllegalStateException
    messageFunctions.put(reqClass, new BoundAsync(messageFunction))
  }

  protected def bindSafe(reqClass: Class[_ <: AnyRef],
                         safe: Safe) {
    if (activeActor.actor.opened) throw new IllegalStateException
    messageFunctions.put(reqClass, safe)
  }

  def mailbox: Mailbox

  implicit def activeActor: ActiveActor

  def factory: Factory

  def factoryId = {
    if (factory == null) null
    else factory.id
  }

  def exceptionHandler(msg: AnyRef,
                       responseFunction: Any => Unit,
                       messageFunction: (AnyRef, Any => Unit) => Unit)
                      (exceptionFunction: Exception => Unit) {
    if (mailbox == null) throw
      new UnsupportedOperationException("Immutable actors can not use excepton handlers")
    val actor = activeActor.actor
    val oldExceptionFunction = mailbox.exceptionFunction
    mailbox.exceptionFunction = exceptionFunction
    try {
      messageFunction(msg, rsp => {
        try {
          responseFunction(rsp)
        } catch {
          case ex: Exception => throw new TransparentException(ex)
        }
      })
    } catch {
      case ex: TransparentException => {
        exceptionFunction(ex.getCause.asInstanceOf[Exception])
      }
      case ex: Exception =>
        exceptionFunction(ex)
    } finally {
      mailbox.exceptionFunction = oldExceptionFunction
    }
  }
}
