/*
 * Copyright 2010  M.Naji
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
package comet

import java.io.File
import template.composer.{TemplateResultMsg, TemplateComposerActor}
import command.messages.UpdateResponseMsg
import javax.servlet.http.HttpSession
import template.continuation.ContextExtension
import command.{CommandLayer, UpdateRequestsActor}
import util.actors.msgs.ErrorMsg
import util.actors.AsynchronousActor
import util.{SystemComposite, Configuration, Timestamp}

class TemplateBayeuxActor(systemContext: SystemComposite, uuid: String) extends AsynchronousActor(systemContext, uuid) with ContextExtension {
  var currentCometMsg: CometRequest = null
  var userUuid: String = null
  var done = false
  var languageCode: String = null
  var timezone: String = null
  private var service: TemplateBayeuxService = null
  private var session: HttpSession = null

  private[comet] def apply(service: TemplateBayeuxService, session: HttpSession) {
    require(this.service == null || this.service == service)
    require(this.session == null || this.session == session)
    this.service = service
    this.session = session
  }

  private lazy val url = new File(CommandLayer(systemContext).mainDirectory, Configuration(systemContext).property(COMET_ROUTER_TEMPLATE_PARAMETER)).
          toURI.toString

  override def messageHandler = {
    case msg: TemplateResultMsg => {
      if (journalEntry != null) {
        val je = journalEntry
        journalEntry = null
        UpdateRequestsActor(systemContext, this, je, updateParameters, context)
      } else {
        val json = context.getVar("json").toString
        if(json != "{}" && json != "") {
          if(json.contains("\"broadcast\":true"))
            pushToUser(json)
          else
            pushToClient(json)
        }
      }
    }
    case msg: UpdateResponseMsg => {
      newContext
      val updateParameters = msg.updateParameters
      if (!updateParameters.containsKey("updateRequestInError")) {
        if (updateParameters.containsKey("user.language")) {
          languageCode = String.valueOf(updateParameters.get("user.language"))
          session.setAttribute("language", languageCode)
        }
        if (updateParameters.containsKey("user.timezone")) {
          timezone = String.valueOf(updateParameters.get("user.timezone"))
          session.setAttribute("timezone", timezone)
        }
        if (updateParameters.containsKey("newUser.rolonUuid")) {
          userUuid = String.valueOf(updateParameters.containsKey("newUser.rolonUuid"))
          session.setAttribute("userUuid", userUuid)
        }
      }
      val it = updateParameters.keySet.iterator
      while (it.hasNext) {
        val n = it.next
        val v = String.valueOf(updateParameters.get(n))
        context.setCon(n, v)
      }
      updateContext
    }
    case msg: ErrorMsg => {
      if (!done) {
        service.sendErrorMessage(userUuid, msg.error)
        done = true
      }
      unexpectedMsg(msg)
    }
    case msg: CometRequest => processRequest(msg)
    case msg => {
      if (!done) {
        service.sendErrorMessage(userUuid, "Internal server error")
        unexpectedMsg(msg)
      }
    }
  }

  override def exceptionHandler = {
    case ex: Throwable => {
      ex.printStackTrace
      service.sendErrorMessage(userUuid, ex.toString)
    }
    case _ =>
  }

  private def processRequest(msg: CometRequest) {
    debug(msg)
    currentCometMsg = msg
    userUuid = msg.userUuid
    languageCode = msg.languageCode
    timezone = msg.timezone
    newContext
    updateContext
  }

  private def updateContext {
    val timestamp = Timestamp.timestamp
    context.setCon("user.language", languageCode)
    context.setCon("user.timezone", timezone)
    context.setCon("user.uuid", userUuid)
    context.setCon("user.timestamp", timestamp)
    context.setCon("timestamp", timestamp)
    context.setCon("comet.clientId", currentCometMsg.message.clientId)
    context.setCon("comet.channel", currentCometMsg.message.channel)
    context.setCon("comet.id", currentCometMsg.message.id)
    def dumpMessage(prefix: String, msg: Any) {
      msg match {
        case message: java.util.Map[String, Any] => {
          val keys = message.keySet.toArray
          for (ndx <- 0 until keys.size) {
            val name = keys(ndx)
            dumpMessage(prefix + "." + name, message.get(name))
          }
        }
        case message: Array[Any] => {
          for (ndx <- 0 until message.size) {
            dumpMessage(prefix + "." + ndx, message(ndx))
          }
        }
        case value => context.setCon(prefix, String.valueOf(value))
      }
    }
    dumpMessage("comet.data", currentCometMsg.message.data)
    TemplateComposerActor(systemContext, userUuid, context, this, this, url)

  }

  override protected def newContext {
    super.newContext
    context.newVar("json", "{}")
  }

  override def pushToClient(data: String) {
    if(currentCometMsg == null)
      service.deliver(userUuid, data)
    else
      service.deliver(userUuid, data, currentCometMsg.message.clientId)
  }

  override def pushToUser(data: String) {
    service.deliver(userUuid, data)
  }
}