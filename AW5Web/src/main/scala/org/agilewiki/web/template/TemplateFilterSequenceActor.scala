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

import org.agilewiki.actors.application.Context
import util.actors._
import nonblocking.NBActor
import util.actors.res._
import org.agilewiki.web.template.composer.{TemplateComposerActor, TemplateResultMsg}
import java.io.File
import util.sequence.actors._
import util.SystemComposite

class TemplateFilterSequenceActor(systemContext: SystemComposite, uuid: String)
        extends NBActor(systemContext, uuid) with SequenceConvenience
                with ExtendedContext {
  var context: Context = null
  var extendedContext: ExtendedContext = null
  var wseq: SequenceConvenience = null
  var activeTemplate: String = null
  var activeTemplateDirectory: String = null
  var activeTemplatePathname: String = null
  var url: String = null
  var userUuid: String = null
  var requester: InternalAddress = null
  var header: Any = null
  var result: String = null
  var templateContext: Context = null
  var ignoreName: String = null
  var keyName: String = null
  var extensionName: String = null
  var delimiter: Char = 0.asInstanceOf[Char]

  final override def messageHandler = {
    case msg: NextMsg => nextMsg(msg)
    case msg: CurrentMsg => currentMsg(msg)
    case msg: ResultMsg => resultMsg(msg)
    case msg: EndMsg => endMsg(msg)
    case msg: TemplateResultMsg => templateResultMsg(msg)
    case msg => unexpectedMsg(requester, msg)
  }

  def currentMsg(msg: CurrentMsg) {
    try {
      requester = msg.requester
      header = msg.header
      var key = msg.key
      wseq ! CurrentMsg(this, key, null)
    } catch {
      case ex: Throwable => error(requester, ex)
    }
  }

  def nextMsg(msg: NextMsg) {
    try {
      requester = msg.requester
      header = msg.header
      var key = msg.key
      wseq ! NextMsg(this, key, null)
    } catch {
      case ex: Throwable => error(requester, ex)
    }
  }

  def endMsg(msg: EndMsg) {
    try {
      requester ! EndMsg(header)
    } catch {
      case ex: Throwable => error(requester, ex)
    }
  }

  def resultMsg(msg: ResultMsg) {
    try {
      result = msg.result
      templateContext = Context(context.contextMap)
      templateContext.setCon(keyName, result)
      templateContext.newVar(ignoreName, "")
      if (delimiter != 0.asInstanceOf[Char]) {
        templateContext.newVar(extensionName, "")
      }
      templateContext.setCon("activeTemplate", activeTemplate)
      templateContext.setCon("activeTemplateDirectory", activeTemplateDirectory)
      templateContext.setCon("activeTemplatePathname", activeTemplatePathname)
      TemplateComposerActor(localContext, userUuid, templateContext, this, extendedContext, url)
    } catch {
      case ex: Throwable => error(requester, ex)
    }
  }

  def templateResultMsg(mg: TemplateResultMsg) {
    try {
      if (templateContext.get(ignoreName) == "") {
        if (delimiter == 0.asInstanceOf[Char])
          requester ! ResultMsg(header, result)
        else {
          val key = result + delimiter + templateContext.get(extensionName)
          requester ! ResultMsg(header, key)
        }
      } else {
        wseq ! NextMsg(this, null, null)
      }
    } catch {
      case ex: Throwable => error(requester, ex)
    }
  }

  override def templateFilterSequence(context: Context, wrappedSequence: SequenceConvenience, templatePath: String, delimiter: Char) =
    TemplateFilterSequenceActor(localContext, context, wrappedSequence, templatePath, delimiter)
}

object TemplateFilterSequenceActor {
  def apply(systemContext: SystemComposite, context: Context, wseq: SequenceConvenience, templatePath: String, delimiter: Char) = {
    val seq = Actors(systemContext).actorFromClassName(ClassName(classOf[TemplateFilterSequenceActor])).asInstanceOf[TemplateFilterSequenceActor]
    seq.context = context
    seq.extendedContext = context.getSpecial(".extendedContext").asInstanceOf[ExtendedContext]
    seq.wseq = wseq
    val i = templatePath.lastIndexOf("/")
    val activeTemplateDirectory = templatePath.substring(0, i)
    val directory = new File(CommandLayer(systemContext).mainDirectory, activeTemplateDirectory)
    seq.activeTemplate = templatePath
    seq.activeTemplateDirectory = activeTemplateDirectory
    seq.activeTemplatePathname = directory.toString
    seq.url = CommandLayer(systemContext).fileUrl(templatePath)
    seq.userUuid = context.get("user.uuid")
    var loopPrefix = context.get("loopPrefix")
    if (loopPrefix != "") loopPrefix += "."
    seq.ignoreName = loopPrefix + "ignore"
    seq.keyName = loopPrefix + "key"
    seq.extensionName = loopPrefix + "extension"
    seq.delimiter = delimiter
    seq
  }
}