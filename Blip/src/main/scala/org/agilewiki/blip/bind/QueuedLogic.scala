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

import exchange._

/**
 * The QueuedLogic class supports requests that are to be added to an actor's
 * incoming message queue.
 */
abstract class QueuedLogic(messageFunction: (AnyRef, Any => Unit) => Unit)
  extends MessageLogic {

  /**
   * Returns the function which will eventually be used to process the request.
   */
  def reqFunction = messageFunction

  /**
   * Process the request. Any exceptions raised durring request processing are
   * returned as a response.
   */
  def process(exchange: Exchange, bindRequest: BindRequest) {
    try {
      messageFunction(bindRequest.req, exchange.reply)
    } catch {
      case ex: Exception => {
        exchange.reply(ex)
      }
    }
  }

  /**
   * Create a BindRequest wrapping the application request and
   * add it to the actor's incoming message queue.
   */
  def enqueueRequest(srcExchange: Exchange,
                   targetActor: BindActor,
                   content: AnyRef,
                   responseFunction: Any => Unit) {
    val oldReq = srcExchange.curReq.asInstanceOf[BindRequest]
    val sender = oldReq.target
    val req = targetActor.newRequest(
      responseFunction,
      content,
      this,
      sender)
    req.setOldRequest(oldReq)
    targetActor.exchangeMessenger.sendReq(targetActor, req, srcExchange)
  }
}
