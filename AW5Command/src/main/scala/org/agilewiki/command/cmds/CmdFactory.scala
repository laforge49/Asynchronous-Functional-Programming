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

import org.agilewiki.actors.ActorLayer
import org.agilewiki.command.messages.{CmdReqMsg, CmdRspMsg}
import util.actors._
import nonblocking.NBActor
import util.actors.res._
import util.SystemComposite

class CmdFactory(systemContext: SystemComposite, uuid: String)
        extends NBActor(systemContext, uuid) {
  val cmdLayer = CommandLayer(localContext)

  override def messageHandler = {
    case msg: CmdReqMsg => cmdReq(msg)
    case msg => unexpectedMsg(msg)
  }

  private def cmdReq(msg: CmdReqMsg) {
    try {
      val meta = cmdLayer.cmds.get(msg.cmdName)
      if (meta == null) error(msg, "Unknown command: " + msg.cmdName)
      else {
        val cmd = Actors(localContext).actorFromClassName(meta)
        msg.requester ! CmdRspMsg(msg.header, cmd)
      }
    } catch {
      case ex: Throwable => error(msg, ex)
    }
  }
}

object CmdFactory {
  def apply(systemContext: SystemComposite,
            cmdContext: InternalAddress,
            heading: String,
            userUuid: String,
            cmdName: String) {
    val cmdFactory = Actors(systemContext).actorFromClassName(ClassName(classOf[CmdFactory])).asInstanceOf[CmdFactory]
    cmdFactory ! CmdReqMsg(cmdContext, heading, userUuid, cmdName)
  }
}