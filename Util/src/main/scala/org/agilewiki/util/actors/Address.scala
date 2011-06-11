/*
 * Copyright 2010 B. La Forge
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
package org.agilewiki.util
package actors

import java.util.concurrent.SynchronousQueue
import msgs.{RequestMsg, ErrorMsg}
import res.ClassName
import org.agilewiki.util.{Configuration, Logger}

trait Address

trait InternalAddress extends Address {
  def localContext: SystemComposite

  def !(msg: AnyRef)

  final def bang(msg: AnyRef) {
    this ! msg
  }
}

class InternalAddressFuture(context: SystemComposite) extends InternalAddress with Logger {
  override def localContext = context

  @volatile private[this] var item: AnyRef = _
  @volatile private[this] var satisfied = false

  def !(msg: AnyRef): Unit = synchronized {
    if (!satisfied) {
      item = msg
      satisfied = true
    }
    notifyAll()
  }

  def get: AnyRef = synchronized {
    if (satisfied) item
    else {
      this.wait()
      if (satisfied) item
      else get
    }
  }
}

class InternalAddressQueue(context: SystemComposite)
        extends SynchronousQueue[AnyRef]
                with InternalAddress {
  override def localContext = context

  override def !(msg: AnyRef) {
    put(msg)
  }
}

trait Agent {
  def !(msg: AnyRef)
}

trait FullInternalAddressActor
  extends InternalAddressActor {

  def getUuid: String

  override  protected def formatLog(msg: => AnyRef) = {
      "[SERVER:" + Configuration(localContext).localServerName + "]; [ACTOR:" + getUuid + "];" + msg
    }
}

trait InternalAddressActor extends Logger with Agent with InternalAddress {

  protected def messageHandler: PartialFunction[AnyRef, Unit]

  protected def formatLog(msg: => AnyRef) = {
      "[SERVER:" + Configuration(localContext).localServerName + "];" + msg
    }

  override def trace(msg: => AnyRef) = super.trace(formatLog(msg))

  override def debug(msg: => AnyRef) = super.debug(formatLog(msg))

  override def debug(msg: => AnyRef, t: Throwable) = super.debug(formatLog(msg), t)

  override def info(msg: => AnyRef) = super.info(formatLog(msg))

  override def info(msg: => AnyRef, t: Throwable) = super.info(formatLog(msg), t)

  override def warn(msg: => AnyRef) = super.warn(formatLog(msg))

  override def warn(msg: => AnyRef, t: Throwable) = super.warn(formatLog(msg), t)

  override def error(msg: => AnyRef) = super.error(formatLog(msg))

  override def error(msg: => AnyRef, t: Throwable) = super.error(formatLog(msg), t)

    def errorTxt(txt: String) =
      Configuration(localContext).localServerName + " Error from agent " + this.getClass.getName + ": " + txt

    private[actors] def throwableTxt(ex: Throwable) = {
      ex.printStackTrace
      ex.toString
    }

    private[actors] def logError(txt: String) {
      warn(txt)
      System.err.println(getClass.getName + " " + txt)
    }

    def error(txt: String, ex: Throwable) {
      logError(txt + ": " + throwableTxt(ex))
    }

    def error(errorMsg: ErrorMsg) {
      logError(errorMsg.error)
    }

    def error(reply: InternalAddress, errorMsg: ErrorMsg) {
      reply ! errorMsg
    }

    private[actors] val unexpectedTxt =
    "Unexpected message received"

    private[actors] def unexpectedTxt(msg: AnyRef): String =
      unexpectedTxt + ": " + msg.asInstanceOf[AnyRef].getClass.getName + "[ " + String.valueOf(msg) + " ]"

    def error(txt: String) {
      val et = errorTxt(txt)
      logError(et)
    }

    def error(reply: InternalAddress, txt: String) {
      error(reply, ErrorMsg(null, txt, Configuration(localContext).localServerName, ClassName(getClass)))
    }

    def error(requestMsg: RequestMsg, txt: String) {
      error(requestMsg.requester, txt)
    }

    def error(ex: Throwable) {
      error(throwableTxt(ex))
    }

    def error(reply: InternalAddress, ex: Throwable) {
      error(reply, throwableTxt(ex))
    }

    def error(requestMsg: RequestMsg, ex: Throwable) {
      error(requestMsg.requester, ex)
    }

    def unexpectedMsg(msg: AnyRef) {
      if (msg.isInstanceOf[ErrorMsg]) {
        error(msg.asInstanceOf[ErrorMsg])
      } else if (msg.isInstanceOf[RequestMsg]) {
        val m = msg.asInstanceOf[RequestMsg]
        error(m, unexpectedTxt(m))
      } else if (msg.isInstanceOf[Object]) {
        error(unexpectedTxt(msg.asInstanceOf[Object]))
      } else error(unexpectedTxt)
    }

    def unexpectedMsg(reply: InternalAddress, msg: AnyRef) {
      if (reply == null)
        unexpectedMsg(msg)
      else if (msg.isInstanceOf[ErrorMsg])
        reply ! msg
      else if (msg.isInstanceOf[Object])
        error(reply, (unexpectedTxt(msg.asInstanceOf[Object])))
      else error(reply, unexpectedTxt)
    }
}
