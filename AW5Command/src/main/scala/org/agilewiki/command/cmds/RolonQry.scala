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
package command.cmds

import util.actors._
import org.agilewiki.actors.application.query.{RolonResponse, RolonRequest}
import org.agilewiki.command.messages.{CommandCompletionMsg, InvokeCmdMsg}
import org.agilewiki.actors.application.{Context, RolonDoesNotExist, ApplicationActor}
import org.agilewiki.command.{VersionCache, CommandLayer}
import util.{SystemComposite, RolonName}

class RolonQry(systemContext: SystemComposite, uuid: String)
        extends ApplicationActor(systemContext, uuid) {
  var requester: InternalAddress = null
  var context: Context = null
  var header: Any = null
  var rolonUuid: String = null
  var timestamp: String = null
  var versionId: String = null
  var versionCache: VersionCache = null

  override def messageHandler = {
    case msg: InvokeCmdMsg => invokeCmdMsg(msg)
    case msg: RolonResponse => rolonResponse(msg)
    case msg: RolonDoesNotExist => rolonDoesNotExits(msg)
    case msg => unexpectedMsg(requester, msg)
  }

  def invokeCmdMsg(msg: InvokeCmdMsg) {
    debug(msg)
    requester = msg.requester
    try {
      val commandLayer = CommandLayer(localContext)
      versionCache = commandLayer.versionCache
      context = msg.context
      header = msg.header
      if (!context.contains("rolonUuid")) {
        error(msg, "Missing from rolon query: rolonUuid")
      } else {
        rolonUuid = String.valueOf(context.get("rolonUuid"))
        timestamp = String.valueOf(context.get("timestamp"))
        versionId = timestamp + "|" + rolonUuid
        if (context.contains(versionId + ".role")) {
          val contextUpdateMsg = CommandCompletionMsg(header)
          requester ! contextUpdateMsg
        } else {
          if (versionCache.has(versionId)) {
            versionCache.load(localContext, versionId, context)
            val contextUpdateMsg = CommandCompletionMsg(header)
            requester ! contextUpdateMsg
          } else {
            val rolonRequest = RolonRequest(RolonName(rolonUuid), timestamp)
            val rolonRequestMsg = rolonRequest.message(this, null)
            rolonRequestMsg.send
          }
        }
      }
    } catch {
      case ex: Throwable => error(requester, ex)
    }
  }

  def rolonDoesNotExits(msg: RolonDoesNotExist) {
    try {
      debug(msg)
      val contextUpdateMsg = CommandCompletionMsg(header)
      requester ! contextUpdateMsg
    } catch {
      case ex: Throwable => error(requester, ex)
    }
  }

  def rolonResponse(msg: RolonResponse) {
    try {
      debug(msg)
      versionCache.put(versionId, msg)
      versionCache.load(localContext, versionId, context)
      val contextUpdateMsg = CommandCompletionMsg(header)
      requester ! contextUpdateMsg
    } catch {
      case ex: Throwable => error(requester, ex)
    }
  }
}

object RolonQry {
  val name = "qry-rolon"
  val cls = classOf[RolonQry].getName
}
