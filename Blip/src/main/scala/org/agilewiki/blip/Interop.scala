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

import messenger._
import exchange._
import scala.actors.Reactor

class Interop[T >: AnyRef](reactor: Reactor[T])
  extends ExchangeMessengerSource
  with MessageListDestination[ExchangeMessengerMessage] {

  override def messageListDestination = this

  def afpSend(dst: Actor, msg: AnyRef)(rf: Any => Unit) {
    val safe = dst.messageLogics.get(msg.getClass)
    if (!safe.isInstanceOf[Bound]) throw
      new IllegalArgumentException(msg.getClass.getName + "can not be sent asynchronously to " + dst)
    dst._open
    val mailbox = dst.mailbox
    if (mailbox == null) {
      val boundFunction = safe.asInstanceOf[BoundFunction]
      boundFunction.reqFunction(msg, rf) //todo very weak
    } else {
      val bound = safe.asInstanceOf[Bound]
      val req = new MailboxReq(dst, rf, msg, bound, this)
      val blkmsg = new java.util.ArrayList[ExchangeMessengerMessage]
      blkmsg.add(req)
      dst.messageListDestination.incomingMessageList(blkmsg)
    }
  }

  override def incomingMessageList(blkmsg: java.util.ArrayList[ExchangeMessengerMessage]) {
    var i = 0
    while (i < blkmsg.size) {
      val mailboxRsp = blkmsg.get(i).asInstanceOf[ExchangeMessengerResponse]
      i += 1
      reactor ! mailboxRsp
    }
  }

  def afpResponse(mailboxRsp: ExchangeMessengerResponse) {
    mailboxRsp.responseFunction(mailboxRsp.rsp)
  }
}
