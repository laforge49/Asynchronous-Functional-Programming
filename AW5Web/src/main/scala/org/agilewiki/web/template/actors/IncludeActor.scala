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

import org.agilewiki.web.template.player.{SaxListPlayerActor, StartPlayerMsg}
import org.agilewiki.web.template.router.StartProcessMsg
import org.agilewiki.web.template.saxmessages.StartElementMsg
import java.io.File
import command.CommandLayer
import util.SystemComposite
import templatecache._

class IncludeActor(systemContext: SystemComposite, uuid: String)
  extends DropContentActor(systemContext, uuid) {
  var language: String = null

  override protected def start(msg: StartProcessMsg) {
    super.start(msg)
    language = context.get("user.language")
  }

  override protected def firstStartElement(msg: StartElementMsg) {
    debug(msg)
    try {
      var templateUrl: String = null
      val attributes = msg.attributes
      var path = attributes.getValue("path")
      if (path == null) {
        error(router, "missing path attribute on the " + IncludeActor.elementName + " element in " + context.get("activeTemplate"))
        return
      }
      if (path.startsWith("/")) {
        templateUrl = CommandLayer(localContext).fileUrl(path)
      } else {
        path = context.get("activeTemplateDirectory") + "/" + path
        templateUrl = CommandLayer(localContext).fileUrl(path)
      }
      if (templateUrl == null) {
        error(router, "unable to find path " + path)
        return
      }
      router.context.setCon("activeTemplate", path)
      val i = path.lastIndexOf("/")
      val activeTemplateDirectory = path.substring(0, i)
      router.context.setCon("activeTemplateDirectory", activeTemplateDirectory)
      router.context.setCon("activeTemplatePathname",
        new File(CommandLayer(localContext).mainDirectory, activeTemplateDirectory).toString)
      val req = TemplateRequest(this, null, templateUrl)
      val templateCacheActor = Web(systemContext).templateCacheActor
      templateCacheActor ! req
    } catch {
      case ex: Throwable => error(router, ex)
    }
  }

  override protected def templateMsg(msg: TemplateResponse) {
    try {
      val template = msg.template
      val player = SaxListPlayerActor(localContext, template)
      player ! StartPlayerMsg(router)
    } catch {
      case ex: Throwable => error(router, ex)
    }
  }
}

object IncludeActor extends TemplateActor("aw:include", classOf[IncludeActor].getName)
