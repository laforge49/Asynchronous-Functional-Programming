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
package exchange

import messenger._
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.Semaphore

abstract class Exchange(threadManager: ThreadManager,
                        async: Boolean = false,
                        _bufferedMessenger: BufferedMessenger[ExchangeMessengerMessage] = null)
  extends ExchangeMessenger(threadManager, _bufferedMessenger) {

  val atomicControl = new AtomicReference[Exchange]
  val idle = new Semaphore(1)

  override def curReq = super.curReq.asInstanceOf[ExchangeRequest]

  def controllingExchange = atomicControl.get

  override def haveMessage {
    if (async) poll
    else {
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
  }

  override def sendReq(targetActor: ExchangeMessengerActor,
              exchangeRequest: ExchangeRequest,
              srcExchange: Exchange) {
    if (async) super.sendReq(targetActor, exchangeRequest, srcExchange)
    else {
      val srcControllingExchange = srcExchange.controllingExchange
      if (controllingExchange == srcControllingExchange) {
        _sendReq(exchangeRequest)
      } else if (!atomicControl.compareAndSet(null, srcControllingExchange)) {
        super.sendReq(targetActor, exchangeRequest, srcExchange)
      } else {
        idle.acquire
        try {
          _sendReq(exchangeRequest)
        } finally {
          atomicControl.set(null)
          idle.release
        }
      }
    }
  }

  private def _sendReq(exchangeRequest: ExchangeRequest) {
    exchangeRequest.fastSend = true
    exchangeReq(exchangeRequest)
    poll
  }

  override def sendResponse(senderExchange: ExchangeMessenger, rsp: ExchangeMessengerResponse) {
    if (curReq.fastSend) {
      senderExchange.exchangeRsp(rsp)
    } else super.sendResponse(senderExchange, rsp)
  }
}