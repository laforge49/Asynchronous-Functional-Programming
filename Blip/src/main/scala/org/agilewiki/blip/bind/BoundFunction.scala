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

class BoundFunction(messageFunction: (AnyRef, Any => Unit) => Unit)
  extends QueuedLogic(messageFunction) {

  override def func(target: BindActor, msg: AnyRef, rf: Any => Unit)
                   (implicit srcActor: ActiveActor) {
    val srcExchangeMessenger = {
      if (srcActor == null) null
      else {
        srcActor.bindActor.exchangeMessenger
      }
    }
    if (srcExchangeMessenger == null) {
      if (target.exchangeMessenger != null) {
        println("srcActor = " + srcActor)
        println("srcMailbox = " + srcExchangeMessenger)
        println("target = " + target)
        println("targetMailbox = " + target.exchangeMessenger)
        throw new UnsupportedOperationException(
          "An immutable bindActor can only send to another immutable bindActor."
        )
      }
    }
    val responseFunction: Any => Unit = {
      rsp => {
        rsp match {
          case rsp: Exception => {
            srcExchangeMessenger.curReq.asInstanceOf[BindRequest].
              exceptionFunction(rsp, srcExchangeMessenger)
          }
          case rsp => try {
            rf(rsp)
          } catch {
            case ex: Exception => srcExchangeMessenger.curReq.asInstanceOf[BindRequest].
              exceptionFunction(ex, srcExchangeMessenger)
          }
        }
      }
    }
    val targetExchangeMessenger = target.exchangeMessenger
    if (targetExchangeMessenger == null || targetExchangeMessenger == srcExchangeMessenger) {
      if (responseFunction == null) messageFunction(msg, AnyRef => {})
      else messageFunction(msg, responseFunction)
    } else enqueueRequest(srcExchangeMessenger.asInstanceOf[Exchange],
      target,
      msg,
      responseFunction)
  }
}
