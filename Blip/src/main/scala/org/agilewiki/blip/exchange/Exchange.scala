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

import annotation.tailrec
import messenger._
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.Semaphore

/**
 * The Exchange class supports synchronous exchanges of messages when the exchange
 * receiving the request is idle.
 */
abstract class Exchange(threadManager: ThreadManager,
                        async: Boolean = false,
                        _bufferedMessenger: BufferedMessenger[ExchangeMessengerMessage] = null)
  extends ExchangeMessenger(threadManager, _bufferedMessenger) {

  /**
   * Tracks which exchange has control. If an exchange can gain control
   * over another exchange, it can send requests to it synchronously.
   */
  val atomicControl = new AtomicReference[Exchange]

  /**
   * Recasts ExchangeRequest.curReq as an ExchangeRequest.
   */
  override def curReq = super.curReq.asInstanceOf[ExchangeRequest]

  /**
   * Returns the controlling exchange, or null.
   */
  def controllingExchange = atomicControl.get

  /**
   * The haveMessage method is called by a thread when the thread is been assigned to
   * the exchange. A call to haveMessage results a call to poll, but only if
   * no other exchange is in control.
   */
  override def haveMessage {
    if (async) super.haveMessage
    else if (atomicControl.compareAndSet(null, this)) {
      try {
        poll
      } finally {
        atomicControl.set(null)
      }
    }
  }

  /**
   * Process a request synchronously.
   */
  private def _sendReq(exchangeMessengerRequest: ExchangeMessengerRequest) {
    if (exchangeMessengerRequest != null) {
      exchangeMessengerRequest.asInstanceOf[ExchangeRequest].fastSend = true
      exchangeReq(exchangeMessengerRequest)
    }
    poll
  }

  /**
   * If control can be gained over the target exchange, process the request synchronously,
   * otherwise enqueue the request for subsequent processing on another thread.
   */
  final override def sendReq(targetActor: ExchangeMessengerActor,
                                      exchangeMessengerRequest: ExchangeMessengerRequest,
                                      srcExchange: ExchangeMessenger) {
    if (async) {
      super.sendReq(targetActor, exchangeMessengerRequest, srcExchange)
    } else {
      exchangeMessengerRequest.setOldRequest(srcExchange.curReq)
      val srcControllingExchange = srcExchange.asInstanceOf[Exchange].controllingExchange
      if (controllingExchange == srcControllingExchange) {
        _sendReq(exchangeMessengerRequest)
      } else if (!atomicControl.compareAndSet(null, srcControllingExchange)) {
        super.sendReq(targetActor, exchangeMessengerRequest, srcExchange)
      } else {
        try {
          _sendReq(exchangeMessengerRequest)
        } finally {
          atomicControl.set(null)
        }
      }
      if (!isEmpty) sendRem(srcExchange)
    }
  }

  /**
   * Handle a potential race condition.
   */
  @tailrec final def sendRem(srcExchange: ExchangeMessenger) {
    val srcControllingExchange = srcExchange.asInstanceOf[Exchange].controllingExchange
    if (controllingExchange == srcControllingExchange) {
      poll
    } else if (atomicControl.compareAndSet(null, srcControllingExchange)) {
      try {
        poll
      } finally {
        atomicControl.set(null)
      }
    } else return
    if (isEmpty) return
    sendRem(srcExchange)
  }

  /**
   * Return a response the same way the request was sent.
   */
  override def sendResponse(senderExchange: ExchangeMessenger, rsp: ExchangeMessengerResponse) {
    if (curReq.fastSend) {
      senderExchange.exchangeRsp(rsp)
    } else super.sendResponse(senderExchange, rsp)
  }
}
