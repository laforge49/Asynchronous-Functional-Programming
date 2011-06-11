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
package continuation

import java.io.{PrintWriter, OutputStreamWriter, File}
import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpSession}
import command.messages._
import util.actors.res._
import command.cmds.RolonQry
import composer.{TemplateResultMsg, TemplateComposerActor}
import server.jetty.continuation.AbstractContinuationActor
import util.actors.Actors
import command.{CommandLayer, UpdateRequestsActor}
import util.actors.msgs.ErrorMsg
import util.{SystemComposite, Configuration}
import org.agilewiki.actors.ActorLayer
import core.CoreNames

class TemplateContinuationActor(systemContext: SystemComposite, uuid: String)
  extends AbstractContinuationActor(systemContext, uuid)
  with ContextExtension {
  var httpServletRequest: HttpServletRequest = null
  var userUuid: String = null
  var currentRequest: String = null
  var session: HttpSession = null
  var done = false
  var languageCode: String = null
  var timezone: String = null
  var breadcrumbs: java.util.ArrayList[String] = null
  var url: String = null
  var ts: String = null

  override def process {
    session = httpServletRequest.getSession
    userUuid = session.getAttribute("userUuid").asInstanceOf[String]
    if (userUuid == null || userUuid.length == 0) userUuid = CoreNames.ANONYMOUS_UUID
    languageCode = session.getAttribute("language").asInstanceOf[String]
    if (languageCode == null) {
      languageCode = Configuration(localContext).requiredProperty(DEFAULT_LANGUAGE_PARAMETER)
      session.setAttribute("language", languageCode)
    }
    timezone = session.getAttribute("timezone").asInstanceOf[String]
    if (timezone == null || timezone.trim.isEmpty) {
      timezone = Configuration(localContext).requiredProperty(DEFAULT_TIMEZONE_PARAMETER)
      session.setAttribute("timezone", timezone)
    }
    breadcrumbs = session.getAttribute("breadcrumbs").asInstanceOf[java.util.ArrayList[String]]
    if (breadcrumbs == null) {
      breadcrumbs = new java.util.ArrayList[String]
      session.setAttribute("breadcrumbs", breadcrumbs)
    }
    breadcrumbs = new java.util.ArrayList[String](breadcrumbs)
    currentRequest = httpServletRequest.getRequestURI
    if (currentRequest.length == 0 || currentRequest == SLASH_TEMPLATES || currentRequest == SLASH_TEMPLATES)
      currentRequest = SLASH_TEMPLATES_SLASH + languageCode + "/organizer/index.html"
    else if (currentRequest.endsWith("/"))
      currentRequest += "index.html"
    url = CommandLayer(localContext).fileUrl(currentRequest)
    if (url == null) {
      val sr = continuation.getServletResponse.asInstanceOf[HttpServletResponse]
      sr.sendError(HttpServletResponse.SC_NOT_FOUND)
      continuation.complete
      return
    }
    val parameterNames = httpServletRequest.getParameterNames
    newContext
    while (parameterNames.hasMoreElements) {
      val p = String.valueOf(parameterNames.nextElement)
      val v = String.valueOf(httpServletRequest.getParameter(p))
      context.setCon("_." + p, v)
    }
    ts = httpServletRequest.getParameter("timestamp")
    if (ts == null) ts = ""
//    println(url + " ts: " + ts)
    updateContext
  }

  def updateContext {
    var activeTemplateDirectory = currentRequest
    val i = activeTemplateDirectory.lastIndexOf("/")
    activeTemplateDirectory = activeTemplateDirectory.substring(0, i)
    context.setCon("currentRequest", currentRequest)
    context.setCon("activeTemplateDirectory", activeTemplateDirectory)
    context.setCon("pageDirectory", activeTemplateDirectory)
    context.setCon("user.language", languageCode)
    context.setCon("user.timezone", timezone)
    context.setSpecial("user.breadcrumbs", breadcrumbs)
    context.setCon("user.uuid", userUuid)
    context.setCon("templates.directory.pathname",
      new File(CommandLayer(localContext).mainDirectory, TEMPLATES).toString)
    context.setCon("activeTemplatePathname",
      new File(CommandLayer(localContext).mainDirectory, activeTemplateDirectory).toString)
    context.setCon("pagePathname",
      new File(CommandLayer(localContext).mainDirectory, activeTemplateDirectory).toString)
    val timestamp = ActorLayer(localContext).timestampManager.latestQueryTime
    context.setCon("timestamp", timestamp)
    context.setCon("user.timestamp", timestamp)
    val rolonQry = Actors(localContext).actorFromClassName(ClassName(classOf[RolonQry])).
      asInstanceOf[RolonQry]
    context.setCon("rolonUuid", userUuid)
    val invokeCmdMsg = InvokeCmdMsg(this, null, userUuid, context)
    rolonQry ! invokeCmdMsg
  }

  def templateComposerActor {
    try {
      var timestamp = context.get("_.timestamp")
      if (timestamp.length == 0) {
        timestamp = context.get("timestamp")
        context.setCon("_.timestamp", timestamp)
      } else {
        context.setCon("timestamp", timestamp)
      }
      TemplateComposerActor(localContext, userUuid, context, this, this, url)
    } catch {
      case ex: Throwable => {
        ex.printStackTrace
        val response = continuation.getServletResponse.asInstanceOf[HttpServletResponse]
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.toString)
        continuation.complete
      }
    }
  }

  override def handle(msg: AnyRef) {
    msg match {
      case msg: CommandCompletionMsg => {
        templateComposerActor
      }
      case msg: TemplateResultMsg => {
        try {
          if (journalEntry != null) {
            val je = journalEntry
            journalEntry = null
            UpdateRequestsActor(localContext, this, je, updateParameters, context)
          }
          else {
            val response = continuation.getServletResponse.asInstanceOf[HttpServletResponse]
//            ts = "" ///////////////////////disable browser cache!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            if (ts.length > 0) {
              response.addHeader("CACHE-CONTROL","PRIVATE")
              response.addHeader("EXPIRES","Mon, 22 Jul 2099 11:12:01 GMT")   /////needs fixing--too long a period
            } else {
              response.addHeader("Cache-Control","no-cache,no-store")
            }
            val os = response.getOutputStream
            val pos = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true)
            if (currentRequest.endsWith(".xml"))
              response.setContentType("text/xml; charset=UTF-8")
            else {
              response.setContentType("text/html; charset=UTF-8")
              pos.println(
                "<!DOCTYPE html>")
            }
            pos.println(msg.result)
            continuation.complete
          }
        } catch {
          case ex: Throwable => {
            ex.printStackTrace
            val response = continuation.getServletResponse.asInstanceOf[HttpServletResponse]
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.toString)
            continuation.complete
          }
        }
      }
      case msg: UpdateResponseMsg =>
        try {
          updateResponse(msg.updateParameters)
        } catch {
          case ex: Throwable => {
            ex.printStackTrace
            val response = continuation.getServletResponse.asInstanceOf[HttpServletResponse]
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.toString)
            continuation.complete
          }
        }
      case msg: ErrorMsg => {
        if (!done) {
          val response = continuation.getServletResponse.asInstanceOf[HttpServletResponse]
          response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "unable to process")
          continuation.complete
          done = true
        }
        unexpectedMsg(msg)
      }
      case msg => {
        if (!done) {
          val sr = continuation.getServletResponse.asInstanceOf[HttpServletResponse]
          sr.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
          continuation.complete
          unexpectedMsg(msg)
          done = true
        }
      }
    }
  }

  def updateResponse(updateParameters: java.util.Map[String, Any]) {
    val oldRolonUuid = context.get("_.rolonUuid")
    newContext
    if (updateParameters.containsKey("updateRequestInError")) {
      context.setCon("_.rolonUuid", oldRolonUuid)
    } else {
      val rolonUuid = String.valueOf((updateParameters.get("_.rolonUuid")))
      context.setCon("_.rolonUuid", rolonUuid)
      if (updateParameters.containsKey("templateRequest")) {
        currentRequest = String.valueOf(updateParameters.get("templateRequest"))
        if (currentRequest.endsWith("/"))
          currentRequest += "index.html"
      }
      if (updateParameters.containsKey("user.language")) {
        languageCode = String.valueOf(updateParameters.get("user.language"))
        session.setAttribute("language", languageCode)
        if (currentRequest.length < SLASH_TEMPLATES_SLASH.length)
          currentRequest = SLASH_TEMPLATES_SLASH + languageCode + "/"
        else {
          currentRequest = currentRequest.substring(SLASH_TEMPLATES_SLASH.length)
          currentRequest = SLASH_TEMPLATES_SLASH + languageCode + currentRequest.substring(currentRequest.indexOf("/"))
        }
        var url = CommandLayer(localContext).fileUrl(currentRequest)
        if (url == null) {
          currentRequest = SLASH_TEMPLATES_SLASH + languageCode + "/index.html"
        }
      }
      if (updateParameters.containsKey("user.timezone")) {
        timezone = String.valueOf(updateParameters.get("user.timezone"))
        session.setAttribute("timezone", timezone)
      }
      if (updateParameters.containsKey("user.breadcrumbs")) {
        breadcrumbs = updateParameters.get("user.breadcrumbs").asInstanceOf[java.util.ArrayList[String]]
        session.setAttribute("breadcrumbs", breadcrumbs)
      }
      if (updateParameters.containsKey("newUser.rolonUuid")) {
        userUuid = String.valueOf(updateParameters.get("newUser.rolonUuid"))
        session.setAttribute("userUuid", userUuid)
      }
    }
    url = CommandLayer(localContext).fileUrl(currentRequest)
    if (url == null) {
      val sr = continuation.getServletResponse.asInstanceOf[HttpServletResponse]
      sr.sendError(HttpServletResponse.SC_NOT_FOUND)
      continuation.complete
      return
    }
    val it = updateParameters.keySet.iterator
    while (it.hasNext) {
      val n = it.next
      val v = String.valueOf(updateParameters.get(n))
      if (n != "_.rolonUuid")
        context.setCon(n, v)
    }
    updateContext
  }
}
