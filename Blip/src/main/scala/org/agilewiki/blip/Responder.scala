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

trait Responder extends SystemContextGetter {
  val messageFunctions =
    new java.util.HashMap[Class[_ <: AnyRef], (AnyRef, Any => Unit) => Unit]
  lazy val sortedMessageFunctions = {
    val smf = new java.util.TreeMap[Class[_ <: AnyRef], (AnyRef, Any => Unit) => Unit](
      new ClassComparator
    )
    smf.putAll(messageFunctions)
    smf
  }

  protected def bind(reqClass: Class[_ <: AnyRef], messageFunction: (AnyRef, Any => Unit) => Unit) {
    messageFunctions.put(reqClass, messageFunction)
  }

  val safeMessageFunctions =
    new java.util.HashMap[Class[_ <: AnyRef], (AnyRef, Any => Unit, ActiveActor) => Unit]
  lazy val sortedSafeMessageFunctions = {
    val ssmf = new java.util.TreeMap[Class[_ <: AnyRef], (AnyRef, Any => Unit, ActiveActor) => Unit](
      new ClassComparator
    )
    ssmf.putAll(safeMessageFunctions)
    ssmf
  }

  protected def bindSafe(reqClass: Class[_ <: AnyRef],
                         safeReqFunction: (AnyRef, Any => Unit, ActiveActor) => Unit) {
    safeMessageFunctions.put(reqClass, safeReqFunction)
  }

  def mailbox: Mailbox

  implicit def activeActor: ActiveActor

  def id: ActorId

  def factory: Factory

  def factoryName = {
    if (factory == null) null
    else factory.name
  }

  def exceptionHandler(msg: AnyRef,
                       responseFunction: Any => Unit,
                       messageFunction: (AnyRef, Any => Unit) => Unit)
                      (exceptionFunction: Exception => Unit) {
    if (mailbox == null) throw
      new UnsupportedOperationException("Immutable actors can not use excepton handlers")
    val actor = activeActor.actor
    val oldExceptionFunction = mailbox.exceptionFunction
    mailbox.exceptionFunction = exceptionFunction
    try {
      messageFunction(msg, rsp => {
        try {
          responseFunction(rsp)
        } catch {
          case ex: Exception => throw new TransparentException(ex)
        }
      })
    } catch {
      case ex: TransparentException => {
        exceptionFunction(ex.getCause.asInstanceOf[Exception])
      }
      case ex: Exception =>
        exceptionFunction(ex)
    } finally {
      mailbox.exceptionFunction = oldExceptionFunction
    }
  }
}