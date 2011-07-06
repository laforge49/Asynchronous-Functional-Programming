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

class Actor(_mailbox: Mailbox, _factory: Factory) extends MsgDst with MsgSrc {
  val mailbox = _mailbox
  val factory = _factory
  implicit val activeActor = ActiveActor(this)

  def systemContext = _mailbox.systemContext

  def exceptionHandler: PartialFunction[Throwable, Unit] = {
    case null =>
  }

  private def defaultExceptionHandler: PartialFunction[Throwable, Unit] = {
    case ex => throw ex
  }

  private def processException(ex: Throwable) {
    exceptionHandler orElse defaultExceptionHandler
  }

  def apply(msg: AnyRef)
           (responseFunction: Any => Unit)
           (implicit srcActor: ActiveActor) {
    val srcMailbox = {
      if (srcActor == null) null
      else srcActor.actor.mailbox
    }
    if (srcMailbox == null && mailbox != null) throw new UnsupportedOperationException(
      "An immutable actor can only send to another immutable actor."
    )
    if (mailbox == null || mailbox == srcMailbox) sendSynchronous(msg, responseFunction)
    else sendAsynchronous(msg, responseFunction, srcActor)
  }

  def sendSynchronous(msg: AnyRef, responseFunction: Any => Unit) {
    val reqFunction = messageFunctions.get(msg.getClass)
    if (reqFunction == null) throw new UnsupportedOperationException(msg.getClass.getName)
    try {
      reqFunction(msg, rsp => {
        try {
          responseFunction(rsp)
        } catch {
          case ex: Exception => throw new TransparentException(ex)
        }
      })
    } catch {
      case ex: TransparentException => processException(ex.getCause)
      case ex: Exception => processException(ex)
    }
  }

  def sendAsynchronous(msg: AnyRef, responseFunction: Any => Unit, srcActor: ActiveActor) {
    throw new UnsupportedOperationException
  }
}
