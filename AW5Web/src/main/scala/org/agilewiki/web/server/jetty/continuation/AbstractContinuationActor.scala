/*
 * Copyright 2010 M.Naji
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
package web
package server
package jetty
package continuation

import javax.servlet.http.HttpServletRequest
import org.eclipse.jetty.continuation.{Continuation, ContinuationListener, ContinuationSupport}
import util.SystemComposite
import util.actors.nonblocking.NBActor

abstract class AbstractContinuationActor(systemContext: SystemComposite, uuid: String)
        extends NBActor(systemContext, uuid) with ContinuationListener {
  private var _continuation: Continuation = null

  protected def continuation = _continuation

  final override def messageHandler = {
    case msg: HttpServletRequest => processRequest(msg)
    case msg => {debug(msg.toString); handle(msg)}
  }

  def handle(msg: AnyRef) {
    unexpectedMsg(msg)
  }

  def process

  final private def processRequest(msg: HttpServletRequest) {
    debug(msg)
    _continuation = ContinuationSupport.getContinuation(msg)
    if (!_continuation.isSuspended) throw new IllegalStateException
    continuation.addContinuationListener(this)
    process
  }

  override def onComplete(continuation: Continuation) {
    debug("Continuation Completed")
  }

  override def onTimeout(continuation: Continuation) {
    debug("Continuation Expired")
  }
}
