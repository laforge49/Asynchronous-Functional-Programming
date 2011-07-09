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

import annotation.tailrec

class Future extends MsgSrc {
  @volatile private[this] var rsp: Any = _
  @volatile private[this] var satisfied = false

  def send(dst: Actor, msg: AnyRef) {
    val mailbox = dst.mailbox
    if (mailbox == null) sendSynchronous(dst, msg)
    else sendAsynchronous(dst, msg)
  }

  def sendSynchronous(dst: Actor, msg: AnyRef) {
    dst.sendSynchronous(msg, synchronousResponse)
  }

  def sendAsynchronous(dst: Actor, msg: AnyRef) {
    val req = new MailboxReq(dst, null, null, msg, this, null)
    dst.mailbox.request(req)
  }

  def synchronousResponse(_rsp: Any) {
    rsp = _rsp
    satisfied = true
  }

  override def response(msg: MailboxRsp) {
    synchronized {
      if (!satisfied) {
        rsp = msg.rsp
        satisfied = true
      }
      notify()
    }
  }

  @tailrec final def get: Any = {
    synchronized{
      if (satisfied) return rsp
      this.wait()
      if (satisfied) return rsp
    }
    get
  }
}

object Future {
  def apply(actor: Actor, msg: AnyRef) = {
    val future = new Future
    future.send(actor, msg)
    future.get
  }
}