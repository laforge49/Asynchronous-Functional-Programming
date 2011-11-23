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
 * Bindings is a trait common to actors and components of actors.
 */
trait Bindings {

  /**
   * The binding of an application request class to a MessageLogic
   * object is stored in messageLogics.
   */
  val messageLogics =
    new java.util.HashMap[Class[_ <: AnyRef], MessageLogic]

  /**
   * "This" actor is implicit.
   */
  implicit def activeActor: ActiveActor

  /**
   * Returns the exchange messenger object used by the actor.
   */
  def exchangeMessenger: Mailbox

  /**
   * Wraps a message processing function with an exception handling function.
   */
  def exceptionHandler(msg: AnyRef,
                       responseFunction: Any => Unit,
                       messageFunction: (AnyRef, Any => Unit) => Unit)
                      (exceptionFunction: (Exception, ExchangeMessenger) => Unit) {
    if (exchangeMessenger == null) throw
      new UnsupportedOperationException("Immutable actors can not use excepton handlers")
    val oldExceptionFunction = exchangeMessenger.curReq.asInstanceOf[BindRequest].
      exceptionFunction
    exchangeMessenger.curReq.asInstanceOf[BindRequest].exceptionFunction = exceptionFunction
    try {
      messageFunction(msg, rsp => {
        exchangeMessenger.curReq.asInstanceOf[BindRequest].exceptionFunction =
          oldExceptionFunction
        try {
          responseFunction(rsp)
        } catch {
          case ex: Exception => throw new TransparentException(ex)
        }
      })
    } catch {
      case ex: TransparentException => {
        exceptionFunction(ex.getCause.asInstanceOf[Exception], exchangeMessenger)
      }
      case ex: Exception => {
        exchangeMessenger.curReq.asInstanceOf[BindRequest].exceptionFunction =
          oldExceptionFunction
        exceptionFunction(ex, exchangeMessenger)
      }
    }
  }

  /**
   * Bind a class of application request to a message processing function.
   */
  protected def bind(reqClass: Class[_ <: AnyRef],
                     messageFunction: (AnyRef, Any => Unit) => Unit) {
    if (activeActor.bindActor.isOpen) throw new IllegalStateException
    messageLogics.put(reqClass, new BoundFunction(messageFunction))
  }

  /**
   * Bind a class of application request to a MessageLogic object.
   */
  protected def bindMessageLogic(reqClass: Class[_ <: AnyRef],
                         safe: MessageLogic) {
    if (activeActor.bindActor.isOpen) throw new IllegalStateException
    messageLogics.put(reqClass, safe)
  }
}
