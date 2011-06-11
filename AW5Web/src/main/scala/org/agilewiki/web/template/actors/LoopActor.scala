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
package web.template.actors

import java.util.ArrayList
import org.agilewiki.web.template.saxmessages._
import org.agilewiki.actors.application.Context
import org.agilewiki.web.template.composer.{PopMsg, AckMsg, PushMsg}
import util.actors._
import util.actors.res._
import org.agilewiki.command.messages._
import groovy.lang.Script
import org.agilewiki.command.cmds.{RolonQry, UuidSequence, CmdFactory}
import command.CommandLayer
import util.sequence.actors._
import util.sequence.actors.basic.composits.SubSequenceActor
import util.SystemComposite
import web.groovyengine._

class LoopActor(systemContext: SystemComposite, uuid: String)
  extends ElementActor(systemContext, uuid) {
  private val saxMessages = new ArrayList[SaxMessage]
  private var userUuid: String = null
  private var count = 0
  private var limit = 1000
  private var limitName = ""
  private var indexName: String = null
  private var keyName: String = null
  private var key: String = null
  private var expectingAckFromPush = false
  private var saxMsgIndex: Int = 0
  private var prefix: String = null
  private var loopPrefix = ""
  private var previousName = ""
  private var nextName = ""
  private var resetName = ""
  private var seqCmd: InternalAddressActor = null
  private var seq: Agent = null
  private var filter: InternalAddressActor = null
  private var filterName: String = null
  private var pastKey = ""
  private var filterScript: Script = null
  private var groovyActor: GroovyActor = null
  private var savedContext = Context(null)
  private var oddName = ""
  private var className = ""
  private var stripeClassName = ""
  private var noStripeClassName = ""
  private var stripeClass = ""
  private var noStripeClass = ""
  private var timestamp = ""
  private var _uuid = ""

  override protected def firstStartElement(msg: StartElementMsg) {
    debug(msg)
    try {
      timestamp = context.get("timestamp")
      val attributes = msg.attributes
      var cmdName = attributes.getValue("cmd")
      if (cmdName == null) {
        error("missing attribute: cmd")
        return
      }
      cmdName = "seq-" + cmdName
      filterName = attributes.getValue("filter")
      if (filterName != null)
        filterName = "ftr-" + filterName
      loopPrefix = attributes.getValue("loopPrefix")
      if (loopPrefix == null)
        loopPrefix = ""
      else
        loopPrefix = loopPrefix + "."
      val attLen = attributes.getLength
      var i = 0
      while (i < attLen) {
        val nm = attributes.getQName(i)
        val v = attributes.getValue(i)
        context.setCon(nm, v)
        i += 1
      }
      prefix = context.get(loopPrefix + "prefix")
      limitName = loopPrefix + "limit"
      val limits = context.get(limitName)
      if (limits.length > 0) {
        try {
          limit = limits.toInt
        } catch {
          case ex: Exception => {
            error(router, "Not an integer: limit = \"" + limits + "\"")
            return
          }
        }
      }
      resetName = loopPrefix + "resetLink"
      previousName = loopPrefix + "previousLink"
      nextName = loopPrefix + "nextLink"
      indexName = loopPrefix + "index"
      keyName = loopPrefix + "key"
      oddName = loopPrefix + "odd"
      className = loopPrefix + "stripeClass"
      stripeClassName = loopPrefix + "stripeClassName"
      noStripeClassName = loopPrefix + "noStripeClassName"
      stripeClass = context.get(stripeClassName)
      noStripeClass = context.get(noStripeClassName)
      if (stripeClass.length == 0)
        stripeClass = "table-stripe-on"
      if (noStripeClass.length == 0)
        noStripeClass = "table-stripe-off"
      val oldIndex = context.get("_." + indexName)
      if (context.isVar(indexName))
        context.setVar(indexName, oldIndex)
      else
        context.newVar(indexName, oldIndex)
      val oldKey = context.get("_." + keyName)
      if (context.isVar(keyName))
        context.setVar(keyName, oldKey)
      else
        context.newVar(keyName, oldKey)
      context.setCon(loopPrefix + "isUuidSequence", "")
      userUuid = context.get("user.uuid")
      if (CommandLayer(localContext).cmds.containsKey(cmdName))
        CmdFactory(localContext, this, null, userUuid, cmdName)
      else {
        GroovyActor.run(systemContext, this, null, cmdName, context)
      }
    } catch {
      case ex: Throwable => error(router, ex)
    }
  }

  override protected def ranMsg(msg: RanResponse) {
    seq = context.getSpecial(loopPrefix + "sequence").asInstanceOf[Agent]
    processFilter
  }

  override protected def cmdRspMsg(msg: CmdRspMsg) {
    debug(msg)
    try {
      if (seqCmd == null && seq == null)
        seqCmd = msg.cmd
      else {
        filter = msg.cmd
      }
      processFilter
    } catch {
      case ex: Throwable => error(router, ex)
    }
  }

  def processFilter {
    if (filterName != null && filter == null && filterScript == null)
      if (CommandLayer(localContext).cmds.containsKey(filterName))
        CmdFactory(localContext, this, null, userUuid, filterName)
      else {
        groovyActor = GroovyActor(systemContext)
        groovyActor ! GroovyCreateRequest(this, null, filterName, context)
      }
    else if (seq == null)
      seqCmd ! InvokeCmdMsg(this, null, userUuid, context)
    else processPrefix
  }

  override protected def scriptMsg(msg: ScriptResponse) {
    filterScript = msg.groovyScript
    if (seq == null)
      seqCmd ! InvokeCmdMsg(this, null, userUuid, context)
    else processPrefix
  }

  override protected def sequenceMsg(msg: SequenceMsg) {
    debug(msg)
    try {
      seq = msg.seq
      processPrefix
    } catch {
      case ex: Throwable => error(router, ex)
    }
  }

  def processPrefix {
    if (prefix.length > 0) {
      seq = SubSequenceActor(localContext, seq, prefix + ".")
    }
    router ! CharactersMsg("")
  }

  override protected def startElement(msg: StartElementMsg) {
    debug(msg)
    try {
      saxMessages.add(msg)
      router ! CharactersMsg("")
    } catch {
      case ex: Throwable => error(router, ex)
    }
  }

  override protected def characters(msg: CharactersMsg) {
    debug(msg)
    try {
      saxMessages.add(msg)
      router ! CharactersMsg("")
    } catch {
      case ex: Throwable => error(router, ex)
    }
  }

  override protected def endElement(msg: EndElementMsg) {
    debug(msg)
    try {
      saxMessages.add(msg)
      router ! CharactersMsg("")
    } catch {
      case ex: Throwable => error(router, ex)
    }
  }

  override protected def lastEndElement(msg: EndElementMsg) {
    debug(msg)
    try {
      expectingAckFromPush = true
      savedContext.contextMap = context.contextMap
      router ! PushMsg(this)
    } catch {
      case ex: Throwable => error(router, ex)
    }
  }

  override protected def ack(msg: AckMsg) {
    debug(msg)
    try {
      if (expectingAckFromPush) {
        expectingAckFromPush = false
        startBlock
      }
      else if (saxMsgIndex < saxMessages.size) sendSaxMsg
      else startBlock
    } catch {
      case ex: Throwable => error(router, ex)
    }
  }

  private def startBlock {
    context.contextMap = savedContext.contextMap
    context.setCon(loopPrefix + "uuid", "")
    context.newVar(loopPrefix + "ignore", "")
    key = context.get(keyName)
    if (key.length == 0)
      seq ! CurrentMsg(this, null, null)
    else {
      seq ! NextMsg(this, key, null)
    }
  }

  override protected def resultMsg(msg: ResultMsg) {
    debug(msg)
    try {
      pastKey = key
      key = msg.result
      context.setVar(keyName, key)
      if (context.contains(loopPrefix + "isUuidSequence") || seqCmd.isInstanceOf[UuidSequence]) {
        _uuid = context.get(loopPrefix + "uuid")
        if (_uuid == "") {
          _uuid = key
          context.setCon(loopPrefix + "uuid", key)
        }
        if (_uuid.indexOf("_") > -1) {
          val rolonQuery = Actors(localContext).actorFromClassName(ClassName(RolonQry.cls))
          context.setCon("rolonUuid", _uuid)
          rolonQuery ! InvokeCmdMsg(this, "rq", userUuid, context)
        } else
          ready
      } else
        ready
    } catch {
      case ex: Throwable => {
        ex.printStackTrace
        error(router, ex)
      }
    }
  }

  private def ready {
    if (_uuid != "" && _uuid.indexOf("_") > -1) {
      val role = context.get(timestamp + "|" + _uuid + ".role")
      if (role == "") {
        startBlock
        return
      }
    }
    if (filter != null)
      filter ! InvokeCmdMsg(this, "filter", userUuid, context)
    else if (filterScript != null) {
      groovyActor ! RunScriptRequest(this, null, filterScript)
    }
    else
      beginProcessing
  }

  override protected def ranScriptMsg(msg: RanScriptResponse) {
    if (context.get(loopPrefix + "ignore").length == 0)
      beginProcessing
    else
      startBlock
  }

  override protected def contextUpdateMsg(msg: CommandCompletionMsg) {
    debug(msg)
    try {
      if (msg.headers == "rq") {
        ready
      } else {
        if (context.get(loopPrefix + "ignore").length == 0)
          beginProcessing
        else
          startBlock
      }
    } catch {
      case ex: Throwable => error(router, ex)
    }
  }

  private def beginProcessing {
    count += 1
    if (count > limit) {
      key = pastKey
      context.setVar(keyName, key)
      genNext
      finish
      return
    }
    val ndxs = context.get(indexName)
    var ndx = 0
    if (ndxs.length > 0) {
      try {
        ndx = ndxs.toInt
      } catch {
        case ex: Exception => error(router, "Not an integer: " + indexName + " = \"" + ndxs + "\"")
      }
    }
    ndx += 1
    context.setVar(indexName, "" + ndx)
    if (ndx % 2 == 0) {
      context.setCon(oddName, "")
      context.setCon(className, noStripeClass)
    }
    else {
      context.setCon(oddName, "true")
      context.setCon(className, stripeClass)
    }
    saxMsgIndex = 0
    sendSaxMsg
  }

  private def sendSaxMsg {
    val saxMsg = saxMessages.get(saxMsgIndex)
    saxMsgIndex += 1
    router ! saxMsg
  }

  override protected def endMsg(msg: EndMsg) {
    debug(msg)
    try {
      finish
    } catch {
      case ex: Throwable => error(router, ex)
    }
  }

  private def finish {
    genReset
    genPrevious
    router ! PopMsg()
  }

  private def genReset {
    var indexs = context.get(indexName)
    if (indexs.length == 0) indexs = "0"
    val index = Integer.valueOf(indexs).intValue
    if (index > limit && context.isVar(resetName)) {
      val sb = new StringBuilder
      sb.append(context.get("currentRequest"))
      sb.append("?xml=" + context.get("activeTemplate"))
      sb.append("&timestamp=" + timestamp)
      val anchor = context.get(loopPrefix + "anchor")
      if (anchor.length > 0)
        sb.append("#" + anchor)
      context.setVar(resetName, sb.toString)
    }
  }

  private def genPrevious {
    var indexs = context.get(indexName)
    if (indexs.length == 0) indexs = "0"
    val index = Integer.valueOf(indexs).intValue
    if (index > limit && context.isVar(previousName)) {
      val sb = new StringBuilder
      val currentRequest = context.get("currentRequest")
      sb.append(currentRequest)
      var ndx = 0
      var first = true
      var more = true
      while (more) {
        ndx += limit
        if (ndx + limit >= index)
          more = false
        else {
          if (first) {
            first = false
            sb.append("?")
          } else
            sb.append("&")
          val nm = loopPrefix + ndx
          val oldKey = context.get("_." + nm)
          if (ndx + 2 * limit >= index) {
            sb.append(indexName + "=" + ndx)
            sb.append("&" + keyName + "=" + oldKey)
          } else {
            sb.append(nm + "=" + oldKey)
          }
        }
      }
      if (first)
        sb.append("?xml=" + context.get("activeTemplate"))
      else
        sb.append("&xml=" + context.get("activeTemplate"))
      sb.append("&timestamp=" + timestamp)
      val anchor = context.get(loopPrefix + "anchor")
      if (anchor.length > 0)
        sb.append("#" + anchor)
      context.setVar(previousName, sb.toString)
    }
  }

  private def genNext {
    if (context.isVar(nextName)) {
      var indexs = context.get(indexName)
      if (indexs.length == 0) indexs = "0"
      val index = Integer.valueOf(indexs).intValue
      val key = context.get(keyName)
      val sb = new StringBuilder
      val currentRequest = context.get("currentRequest")
      sb.append(currentRequest)
      sb.append("?" + indexName + "=" + index)
      sb.append("&" + keyName + "=" + key)
      sb.append("&timestamp=" + timestamp)
      if (index > limit) {
        val oldIndex = context.get("_." + indexName)
        var oldKey = context.get("_." + keyName)
        sb.append("&" + loopPrefix + oldIndex + "=" + oldKey)
        var more = true
        var ndx = 0
        while (more) {
          ndx += limit
          val nm = loopPrefix + ndx
          oldKey = context.get("_." + nm)
          if (oldKey.length > 0)
            sb.append("&" + nm + "=" + oldKey)
          else
            more = false
        }
      }
      sb.append("&xml=" + context.get("activeTemplate"))
      val anchor = context.get(loopPrefix + "anchor")
      if (anchor.length > 0)
        sb.append("#" + anchor)
      context.setVar(nextName, sb.toString)
    }
  }
}

object LoopActor extends TemplateActor("aw:loop", classOf[LoopActor].getName)
