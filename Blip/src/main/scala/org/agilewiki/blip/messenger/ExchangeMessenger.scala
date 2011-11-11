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
package org.agilewiki.blip.messenger

import java.util.ArrayList

abstract class ExchangeMessenger(threadManager: ThreadManager,
                                 _buffered: BufferedMessenger[ExchangeMessage] = null)
  extends MessageProcessor[ExchangeMessage]
  with MessageListDestination[ExchangeMessage] {

  private val buffered = {
    if (_buffered != null) _buffered
    else new BufferedMessenger[ExchangeMessage](threadManager)
  }

  buffered.setMessageProcessor(this)

  def incomingMessageList(bufferedMessages: ArrayList[ExchangeMessage]) {
    buffered.incomingMessageList(bufferedMessages)
  }

  def putTo(messageListDestination: MessageListDestination[ExchangeMessage], message: ExchangeMessage) {
    buffered.putTo(messageListDestination, message)
  }

  def controllingExchange = this

  def isEmpty = buffered.isEmpty

  def poll = buffered.poll

  override def haveMessage {
    poll
  }

  override def processMessage(msg: ExchangeMessage) {
    msg match {
      case req: ExchangeRequest => exchangeReq(req)
      case rsp: ExchangeResponse => exchangeRsp(rsp)
    }
  }

  def sendReq(targetActor: ExchangeActor,
              req: ExchangeRequest,
              srcExchange: ExchangeMessenger) {
    srcExchange.putTo(targetActor.messageListDestination, req)
  }

  def exchangeReq(msg: ExchangeRequest)

  def sendResponse(senderExchange: ExchangeMessenger, rsp: ExchangeResponse) {
    putTo(senderExchange.buffered, rsp)
  }

  def exchangeRsp(msg: ExchangeResponse)
}