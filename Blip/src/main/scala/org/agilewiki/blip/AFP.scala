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

import scala.actors.Reactor

class AFP[T >: AnyRef](reactor: Reactor[T])
  extends MsgSrc
  with MsgCtrl {

  override def ctrl = this

  def afpSend(dst: Actor, msg: AnyRef, exceptionHandler: Exception => Unit)(rf: Any => Unit) {
    val safe = dst.messageFunctions.get(msg.getClass)
    if (!safe.isInstanceOf[Bound]) throw
      new IllegalArgumentException(msg.getClass.getName + "can not be sent asynchronously to " + dst)
    dst._open
    val mailbox = dst.mailbox
    if (mailbox == null) {
      val boundFunction = safe.asInstanceOf[BoundFunction]
      boundFunction.reqFunction(msg, rf)
    } else {
      val bound = safe.asInstanceOf[Bound]
      val req = new MailboxReq(dst, null, null, msg, bound, this, exceptionHandler, null)
      val blkmsg = new java.util.ArrayList[MailboxMsg]
      blkmsg.add(req)
      dst.ctrl._send(blkmsg)
    }
  }

  override def _send(blkmsg: java.util.ArrayList[MailboxMsg]) {
    var i = 0
    while (i < blkmsg.size) {
      val mailboxRsp = blkmsg.get(i).asInstanceOf[MailboxRsp]
      i += 1
      reactor ! mailboxRsp
    }
  }

  def afpResponse(mailboxRsp: MailboxRsp) {
    mailboxRsp.rsp match {
      case rsp: Exception => mailboxRsp.senderExceptionFunction(rsp)
      case rsp => mailboxRsp.responseFunction(rsp)
    }
  }
}
