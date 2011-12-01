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
package org.agilewiki.blip
package bind

import messenger._
import exchange._
import scala.actors.Reactor

/**
 * A helper class for Scala Reactors which send requests to a BindActor and receive the response.
 */
class Interop[T >: AnyRef](reactor: Reactor[T])
  extends ExchangeMessengerSource
  with MessageListDestination[ExchangeMessengerMessage] {

  /**
   * Returns the MessageListDestination, this.
   */
  override def messageListDestination = this

  /**
   * The afpSend method sends a message to a BindActor and provides a response function
   * for processing the response.
   */
  def afpSend(dst: Actor, msg: AnyRef)(rf: Any => Unit) {
    val safe = dst.messageLogics.get(msg.getClass)
    if (!safe.isInstanceOf[QueuedLogic]) throw
      new IllegalArgumentException(msg.getClass.getName + "can not be sent asynchronously to " + dst)
    dst.initialize
    val mailbox = dst.exchangeMessenger
    if (mailbox == null) {
      val boundFunction = safe.asInstanceOf[BoundFunction]
      boundFunction.reqFunction(msg, rf)
    } else {
      val bound = safe.asInstanceOf[QueuedLogic]
      val req = dst.newRequest(rf, msg, bound, this)
      val blkmsg = new java.util.ArrayList[ExchangeMessengerMessage]
      blkmsg.add(req)
      dst.messageListDestination.incomingMessageList(blkmsg)
    }
  }

  /**
   * The logic for processing an asynchronous response.
   */
  override def incomingMessageList(blkmsg: java.util.ArrayList[ExchangeMessengerMessage]) {
    var i = 0
    while (i < blkmsg.size) {
      val mailboxRsp = blkmsg.get(i).asInstanceOf[ExchangeMessengerResponse]
      i += 1
      reactor ! mailboxRsp
    }
  }

  /**
   * The afpResponse method should be called when the Reactor receives
   * an ExchangeMessengerResponse object.
   */
  def afpResponse(mailboxRsp: ExchangeMessengerResponse) {
    mailboxRsp.responseFunction(mailboxRsp.rsp)
  }
}
