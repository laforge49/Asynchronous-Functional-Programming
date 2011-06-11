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
package web
package template
package actors

import org.agilewiki.command.cmds.CmdFactory
import org.agilewiki.command.messages.{CmdRspMsg, InvokeCmdMsg, CommandCompletionMsg}
import org.agilewiki.web.template.saxmessages.{StartElementMsg, CharactersMsg}
import command.CommandLayer
import util.SystemComposite
import groovyengine.{GroovyActor, RanResponse}

class QueryActor(systemContext: SystemComposite, uuid: String)
  extends ElementActor(systemContext, uuid) {
  private var userUuid: String = null

  override protected def firstStartElement(msg: StartElementMsg) {
    debug(msg)
    try {
      val attributes = msg.attributes
      val cmdName = "qry-" + attributes.getValue("cmd")
      if (cmdName == null)
        error(router, "Missing cmd attribute from element " + QueryActor.elementName)
      else {
        userUuid = context.get("user.uuid")
        val attLen = attributes.getLength
        var i = 0
        while (i < attLen) {
          val nm = attributes.getQName(i)
          val v = attributes.getValue(i)
          context.setCon(nm, v)
          i += 1
        }
        if (CommandLayer(localContext).cmds.containsKey(cmdName))
          CmdFactory(localContext, this, null, userUuid, cmdName)
        else {
          GroovyActor.run(systemContext, this, null, cmdName, context)
        }
      }
    } catch {
      case ex: Throwable => error(router, ex)
    }
  }

  override protected def ranMsg(msg: RanResponse) {
    router ! CharactersMsg("")
  }

  override protected def cmdRspMsg(msg: CmdRspMsg) {
    val cmd = msg.cmd
    val invokeCmdMsg = InvokeCmdMsg(this, null, userUuid, context)
    cmd ! invokeCmdMsg
  }

  override protected def contextUpdateMsg(msg: CommandCompletionMsg) {
    router ! CharactersMsg("")
  }
}

object QueryActor extends TemplateActor("aw:query", classOf[QueryActor].getName)
