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

import org.agilewiki.actors.ActorLayer
import org.agilewiki.actors.application.Context
import groovy.lang.Binding
import util.actors._
import nonblocking.NBActor
import util.actors.res._
import util.sequence.actors._
import util.SystemComposite

class ContextFilterSequenceActor(systemContext: SystemComposite, uuid: String)
        extends NBActor(systemContext, uuid) with SequenceConvenience {
  val commandLayer = CommandLayer(localContext)
  var context: Context = null
  var wseq: SequenceConvenience = null
  var filterName: String = null
  var requester: InternalAddress = null
  var header: Any = null
  var ignoreName: String = null
  var keyName: String = null
  var extensionName: String = null
  var delimiter: Char = 0.asInstanceOf[Char]

  final override def messageHandler = {
    case msg: NextMsg => nextMsg(msg)
    case msg: CurrentMsg => currentMsg(msg)
    case msg: ResultMsg => resultMsg(msg)
    case msg: EndMsg => endMsg(msg)
    case msg => unexpectedMsg(requester, msg)
  }

  def resultMsg(msg: ResultMsg) {
    try {
      val result = msg.result
      val gb = new Binding
      if (context.isVar(ignoreName))
        context.setVar(ignoreName, "")
      else
        context.newVar(ignoreName, "")
      if (delimiter != 0.asInstanceOf[Char])
        if (context.isVar(extensionName))
          context.setVar(extensionName, "")
        else
          context.newVar(extensionName, "")
      if (context.isVar(keyName))
        context.setVar(keyName, result)
      else
        context.setCon(keyName, result)
      gb.setVariable("context", context)
      commandLayer.gse.run(filterName, gb)
      if (context.get(ignoreName) == "")
        if (delimiter == 0.asInstanceOf[Char])
          requester ! ResultMsg(header, result)
        else
          requester ! ResultMsg(header, result + delimiter + context.get(extensionName))
      else
        wseq ! NextMsg(this, null, null)
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
}

object ContextFilterSequenceActor {
  def apply(systemContext: SystemComposite, context: Context, wseq: SequenceConvenience, filterName: String, delimiter: Char) = {
    val seq = Actors(systemContext).actorFromClassName(ClassName(classOf[ContextFilterSequenceActor])).asInstanceOf[ContextFilterSequenceActor]
    seq.context = context
    seq.wseq = wseq
    seq.filterName = "ftr_" + filterName + ".groovy"
    var loopPrefix = context.get("loopPrefix")
    if (loopPrefix != "") loopPrefix += "."
    seq.ignoreName = loopPrefix + "ignore"
    seq.keyName = loopPrefix + "key"
    seq.delimiter = delimiter
    seq.extensionName = loopPrefix + "extension"
    seq
  }
}