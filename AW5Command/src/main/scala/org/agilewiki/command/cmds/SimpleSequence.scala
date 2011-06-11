/*
 * Copyright 2010 Bill La Forge
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
package command
package cmds

import org.agilewiki.command.messages.{SequenceMsg, InvokeCmdMsg}
import util.actors._
import org.agilewiki.actors.application.Context
import util.sequence.actors.{EndMsg, ResultMsg, CurrentMsg, NextMsg}
import util.SystemComposite

abstract class SimpleSequence(systemContext: SystemComposite, uuid: String)
        extends SynchronousActor(systemContext, uuid) {
  protected var requester: InternalAddress = null
  protected val commandLayer = CommandLayer(localContext)
  protected var context: Context = null

  final override def messageHandler = {
    case msg: InvokeCmdMsg => invokeCmdMsg(msg)
    case msg: NextMsg => nextMsg(msg)
    case msg: CurrentMsg => currentMsg(msg)
    case msg: ResultMsg => resultMsg(msg)
    case msg: EndMsg => endMsg(msg)
    case msg: SequenceMsg => sequenceMsg(msg)
    case msg => unexpectedMsg(requester, msg)
  }

  protected def invokeCmdMsg(msg: InvokeCmdMsg) {
    try {
      requester = msg.requester
      context = msg.context
      val s = seq
      if (s != null)
        requester ! SequenceMsg(msg.header, s)
    } catch {
      case ex: Throwable => error(requester, ex)
    }
  }

  protected def sequenceMsg(msg: SequenceMsg) {unexpectedMsg(requester, msg)}

  protected def nextMsg(msg: NextMsg) {unexpectedMsg(requester, msg)}

  protected def currentMsg(msg: CurrentMsg) {unexpectedMsg(requester, msg)}

  protected def resultMsg(msg: ResultMsg) {unexpectedMsg(requester, msg)}

  protected def endMsg(msg: EndMsg) {unexpectedMsg(requester, msg)}

  protected def seq: Agent
}
