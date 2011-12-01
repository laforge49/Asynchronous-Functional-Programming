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

import annotation.tailrec
import java.util.ArrayList
import messenger._
import exchange._

/**
 * The Future class for sending a request and waiting for a response.
 */
class Future
  extends ExchangeMessengerSource
  with MessageListDestination[ExchangeMessengerMessage] {
  @volatile private[this] var rsp: Any = _
  @volatile private[this] var satisfied = false

  /**
   * Sends an application request to a given actor.
   */
  def send(dst: BindActor, msg: AnyRef) {
    val safe = dst.messageLogics.get(msg.getClass)
    if (!safe.isInstanceOf[QueuedLogic]) throw
      new IllegalArgumentException(msg.getClass.getName + "can not be sent asynchronously to " + dst)
    dst.initialize
    val mailbox = dst.exchangeMessenger
    if (mailbox == null) {
      val boundFunction = safe.asInstanceOf[BoundFunction]
      boundFunction.reqFunction(msg, synchronousResponse)
    } else {
      val bound = safe.asInstanceOf[QueuedLogic]
      val req = dst.newRequest(Unit => {}, msg, bound, this)
      val blkmsg = new ArrayList[ExchangeMessengerMessage]
      blkmsg.add(req)
      dst.messageListDestination.incomingMessageList(blkmsg)
    }
  }

  /**
   * The logic for processing a synchronous response.
   */
  private def synchronousResponse(_rsp: Any) {
    rsp = _rsp
    satisfied = true
  }

  /**
   * Returns the MessageListDestination, this.
   */
  override def messageListDestination = this

  /**
   * The logic for processing an asynchronous response.
   */
  override def incomingMessageList(blkmsg: ArrayList[ExchangeMessengerMessage]) {
    synchronized {
      if (!satisfied) {
        rsp = blkmsg.get(0).asInstanceOf[ExchangeMessengerResponse].rsp
        satisfied = true
      }
      notify()
    }
  }

  /**
   * The get method waits for a response.
   */
  @tailrec final def get: Any = {
    synchronized {
      if (satisfied) return rsp
      this.wait()
      if (satisfied) return rsp
    }
    get
  }
}

/**
 * Sends a response and waits for a request.
 * This companion object is used mostly by test code, as it blocks the current thread
 * until a response is received.
 * Use of this companion object is further constrained, as it does not support application request
 * classes bound to a ConcurrentData objects nor application request classes bound to
 * a Forward object.
 */
object Future {
  def apply(actor: BindActor, msg: AnyRef) = {
    val future = new Future
    future.send(actor, msg)
    val rv = future.get
    rv match {
      case ex: Exception => throw ex
      case _ =>
    }
    rv
  }
}
