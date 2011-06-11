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
package util
package actors
package nonblocking

import scala.PartialFunction
import java.util.ArrayDeque

abstract class NBActor(context: SystemComposite, uuid: String)
        extends NBActorCapability {
  require(uuid != null && !uuid.isEmpty,"An NBActor must have a uuid")
  require(context != null, "An NBActor must have a context")
  require(context.isInstanceOf[SystemNonBlockingComponent], "An NBActor requires a SystemNonBlockingComponent context")

  override def localContext = context

  override def getUuid: String = uuid
}

trait NBActorCapability extends FullInternalAddressActor {
  private var pendingMessages: ArrayDeque[AnyRef] = new ArrayDeque[AnyRef]
  private[nonblocking] var _dispatchThread: DispatchThread = null

  private[nonblocking] def dispatchThread = {
    if (_dispatchThread == null)
      _dispatchThread = DispatchManager(localContext).selectDispatchThread
    _dispatchThread
  }

  def !(msg: AnyRef) {
    pendingMessages.addLast(msg)
    if (pendingMessages.size < 3)
      dispatchThread.moreWork(this)
  }

  private[nonblocking] def processMessages {
    var msg = pendingMessages.pollFirst
    while (msg != null) {
      try {
        messageHandler(msg)
      } catch {
        case ex: Throwable => {
          ex.printStackTrace
        }
      }
      msg = pendingMessages.pollFirst
    }
  }

  protected def fallback: PartialFunction[AnyRef, Unit] = {
    case msg => unexpectedMsg(msg)
  }

  protected def messageHandler: PartialFunction[AnyRef, Unit]
}