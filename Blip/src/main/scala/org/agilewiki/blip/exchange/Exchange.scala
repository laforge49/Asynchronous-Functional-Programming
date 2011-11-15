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
                        stateFactory: ExchangeStateFactory = new ExchangeStateFactory,
                        _bufferedMessenger: BufferedMessenger[ExchangeMessengerMessage] = null)
  extends ExchangeMessenger(threadManager, _bufferedMessenger) {

  protected var _state: ExchangeState = null
  val atomicControl = new AtomicReference[Exchange]
  val idle = new Semaphore(1)

  stateFactory.exchange = this

  def state = _state

  def setState(state: ExchangeState) {
    _state = state
  }

  def newState(currentRequest: ExchangeRequest) {
    _state = stateFactory(currentRequest)
  }

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

  def sendReq(targetActor: ExchangeActor,
              exchangeRequest: ExchangeRequest,
              srcExchange: Exchange) {
    exchangeRequest.sourceState = srcExchange.state
    if (async) srcExchange.putTo(targetActor.messageListDestination, exchangeRequest)
    else {
      val srcControllingExchange = srcExchange.controllingExchange
      if (controllingExchange == srcControllingExchange) {
        _sendReq(exchangeRequest)
      } else if (!atomicControl.compareAndSet(null, srcControllingExchange)) {
        srcExchange.putTo(targetActor.messageListDestination, exchangeRequest)
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

  override def exchangeReq(msg: ExchangeMessengerRequest) {
    val req = msg.asInstanceOf[ExchangeRequest]
    newState(req)
    processRequest(req)
  }

  protected def processRequest(msg: ExchangeRequest)

  def sendResponse(senderExchange: Exchange, rsp: ExchangeMessengerResponse) {
    if (state.currentRequest.fastSend) {
      senderExchange.exchangeRsp(rsp)
    } else putTo(senderExchange.bufferedMessenger, rsp)
  }

  override def exchangeRsp(msg: ExchangeMessengerResponse) {
    val exchangeResponse = msg.asInstanceOf[ExchangeResponse]
    setState(exchangeResponse.sourceState)
    processResponse(exchangeResponse)
  }

  protected def processResponse(msg: ExchangeResponse)
}