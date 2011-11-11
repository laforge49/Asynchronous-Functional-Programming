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

import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicReference
import messenger._

class SyncMailbox(mailboxFactory: MailboxFactory)
  extends Mailbox(mailboxFactory) {
  val atomicControl = new AtomicReference[ExchangeMessenger]
  val idle = new Semaphore(1)

  override def controllingExchange = atomicControl.get

  override def haveMessage {
    while (!atomicControl.compareAndSet(null, this)) {
      idle.acquire
      idle.release
    }
    try {
      poll
    } finally {
      atomicControl.set(null)
    }
  }

  override def sendReq(targetActor: ExchangeActor,
                       req: ExchangeRequest,
                       srcExchange: ExchangeMessenger) {
    val controllingMailbox = srcExchange.controllingExchange
    if (controllingMailbox == controllingExchange) {
      _sendReq(req)
    } else if (!atomicControl.compareAndSet(null, controllingMailbox)) {
      srcExchange.putTo(targetActor.messageListDestination, req)
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

  protected def _sendReq(exchangeRequest: ExchangeRequest) {
    val req = exchangeRequest.asInstanceOf[MailboxReq]
    req.fastSend = true
    req.binding.process(this, req)
    poll
  }

  override def sendResponse(senderExchange: ExchangeMessenger,
                                      rsp: ExchangeResponse) {
    if (mailboxState.currentRequestMessage.fastSend) {
      senderExchange.exchangeRsp(rsp)
    } else super.sendResponse(senderExchange, rsp)
  }
}
