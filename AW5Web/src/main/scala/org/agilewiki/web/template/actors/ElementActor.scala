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

import org.agilewiki.web.template.composer.AckMsg
import org.agilewiki.web.template.router.{StartProcessMsg, TemplateRouterActor}
import org.agilewiki.web.template.saxmessages.{StartElementMsg, CharactersMsg, EndElementMsg}
import command.messages.{SequenceMsg, CmdRspMsg, CommandCompletionMsg}
import org.agilewiki.actors.application.Context
import util.actors.SynchronousActor
import util.sequence.actors.{EndMsg, ResultMsg}
import util.SystemComposite
import templatecache.TemplateResponse
import groovyengine.{ScriptResponse, RanScriptResponse, RanResponse}

abstract class ElementActor(systemContext: SystemComposite, uuid: String)
  extends SynchronousActor(systemContext, uuid) {
  protected var router: TemplateRouterActor = null
  protected var context: Context = null
  private var depth = 0

  final override def messageHandler = {
    case msg: StartProcessMsg if depth == 0 => start(msg)
    case msg: StartElementMsg if depth == 0 => {
      depth = 1
      firstStartElement(msg)
    }
    case msg: StartElementMsg if depth > 0 => {
      depth += 1
      startElement(msg)
    }
    case msg: CharactersMsg if depth > 0 => characters(msg)
    case msg: EndElementMsg if depth == 1 => {
      depth = -1
      lastEndElement(msg)
    }
    case msg: EndElementMsg if depth > 1 => {
      depth -= 1
      endElement(msg)
    }
    case msg: AckMsg => ack(msg)
    case msg: CmdRspMsg => cmdRspMsg(msg)
    case msg: CommandCompletionMsg => contextUpdateMsg(msg)
    case msg: SequenceMsg => sequenceMsg(msg)
    case msg: ResultMsg => resultMsg(msg)
    case msg: EndMsg => endMsg(msg)
    case msg: TemplateResponse => templateMsg(msg)
    case msg: RanResponse => ranMsg(msg)
    case msg: ScriptResponse => scriptMsg(msg)
    case msg: RanScriptResponse => ranScriptMsg(msg)
    case msg => unexpectedMsg(router, msg)
  }

  protected def ranScriptMsg(msg: RanScriptResponse) {
    unexpectedMsg(router, msg)
  }

  protected def scriptMsg(msg: ScriptResponse) {
    unexpectedMsg(router, msg)
  }

  protected def ranMsg(msg: RanResponse) {
    unexpectedMsg(router, msg)
  }

  protected def templateMsg(msg: TemplateResponse) {
    unexpectedMsg(router, msg)
  }

  protected def start(msg: StartProcessMsg) {
    debug(msg)
    router = msg.router
    context = router.context
  }

  protected def firstStartElement(msg: StartElementMsg)

  protected def startElement(msg: StartElementMsg) {
    debug(msg)
    router ! msg
  }

  protected def characters(msg: CharactersMsg) {
    debug(msg)
    router ! msg
  }

  protected def endElement(msg: EndElementMsg) {
    debug(msg)
    router ! msg
  }

  protected def lastEndElement(msg: EndElementMsg) {
    debug(msg)
    router ! CharactersMsg("")
  }

  protected def cmdRspMsg(msg: CmdRspMsg) {
    unexpectedMsg(router, msg)
  }

  protected def contextUpdateMsg(msg: CommandCompletionMsg) {
    unexpectedMsg(router, msg)
  }

  protected def sequenceMsg(msg: SequenceMsg) {
    unexpectedMsg(router, msg)
  }

  protected def ack(msg: AckMsg) {
    unexpectedMsg(router, msg)
  }

  protected def resultMsg(msg: ResultMsg) {
    unexpectedMsg(router, msg)
  }

  protected def endMsg(msg: EndMsg) {
    unexpectedMsg(router, msg)
  }
}
