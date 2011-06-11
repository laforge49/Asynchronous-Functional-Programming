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
package router

import java.util.{ArrayDeque, ArrayList}
import org.xml.sax.helpers.AttributesImpl
import org.agilewiki.actors.application.Context
import org.agilewiki.web.template.composer.PushMsg
import org.agilewiki.web.template.composer.PopMsg
import org.agilewiki.web.template.saxmessages.CharactersMsg
import org.agilewiki.web.template.saxmessages.EndElementMsg
import org.agilewiki.web.template.saxmessages.StartElementMsg
import util.actors._
import util.actors.res._
import org.agilewiki.actors.exchange._
import org.agilewiki.actors.ActorLayer
import org.agilewiki.web
import util.SystemComposite

class TemplateRouterActor(systemContext: SystemComposite, uuid: String)
        extends SynchronousActor(systemContext, uuid) {
  var userUuid: String = null
  var context:Context = null
  val templateActorStack = new ArrayDeque[InternalAddress]
  val contextMapStack = new ArrayDeque[Map[String, Any]]
  var player: InternalAddress = null
  var composer: InternalAddress = null

  val elementMap = Web(localContext).elementMap

  def templateActor = templateActorStack.peekFirst

  var depth = 0
  var limit = -1

  override def messageHandler = {
    case msg: PushMsg => push(msg)
    case msg: PopMsg => pop(msg)
    case msg: StartElementMsg => startElement(msg)
    case msg: CharactersMsg => character(msg)
    case msg: EndElementMsg => endElement(msg)
    case msg => unexpectedMsg(composer, msg)
  }

  def push(msg: PushMsg) {
    debug(msg)
    templateActor ! msg
  }

  def pop(msg: PopMsg) {
    debug(msg)
    templateActor ! msg
  }

  def startElement(msg: StartElementMsg) {
    debug(msg)
    contextMapStack.addFirst(context.contextMap)
    depth += 1
    var m = msg
    if (limit == -1) {
      val atts = msg.attributes
      val n = atts.getLength
      var i = 0
      val indexes = new ArrayList[Int]
      while (i < n) {
        val an = atts.getQName(i)
        if (an.startsWith("aw:") ||
                an.startsWith("aw-rmpre:") ||
                (an.startsWith("aw.") && an.contains(":")) ||
                an.startsWith("special.") ||
                an.startsWith("setcon.") ||
                an.startsWith("setvar.") ||
                an.startsWith("newvar.")) indexes.add(i)
        i += 1
      }
      if (!indexes.isEmpty) {
        val as = new AttributesImpl(atts)
        var ii = 0
        val nn = indexes.size
        while (ii < nn) {
          i = indexes.get(ii)
          var an = as.getQName(i)
          var sv = as.getValue(i)
          var v:Any = sv
          if (an.startsWith("aw:")) {
            an = an.substring(3)
            as.setQName(i, an)
            v = context.getSpecial(sv)
          } else if (an.startsWith("aw-rmpre:")) {
            an = an.substring(9)
            as.setQName(i, an)
            sv = context.get(sv)
            val ndx = sv.indexOf(".")
            if (ndx > -1)
              v = sv.substring(ndx+1)
          } else if (an.startsWith("aw.") && an.contains(":")) {
            var pn = an.substring(3)
            val ndx = pn.indexOf(":")
            an = pn.substring(ndx + 1)
            as.setQName(i,an)
            pn = pn.substring(0,ndx)
            var pre = context.get(pn)
            if (pre.length > 0) pre = pre + "."
            v = context.getSpecial(pre + sv)
          }
          if (v == null)
            sv = ""
          else
            sv = String.valueOf(v)
          if (an.startsWith("special.")) {
            an = an.substring(8)
            context.setSpecial(an, v)
          } else if (an.startsWith("setcon.")) {
              an = an.substring(7)
              context.setCon(an, sv)
          } else if (an.startsWith("newvar.")) {
            an = an.substring(7)
            context.newVar(an, sv)
          } else if (an.startsWith("setvar.")) {
            an = an.substring(7)
            context.makeVar(an, sv)
          } else
            as.setValue(i,sv)
          ii += 1
        }
        ii = indexes.size
        while (ii > 0) {
          ii -= 1
          i = indexes.get(ii)
          val an = as.getQName(i)
          if (an.startsWith("special.") ||
                  an.startsWith("setcon.") ||
                  an.startsWith("setvar.") ||
                  an.startsWith("newvar.")) as.removeAttribute(i)
          else {
            val v = as.getValue(i)
            if (v.length == 0)
              as.removeAttribute(i)
          }
        }
        m = StartElementMsg(msg.uri, msg.localName, msg.qName, as)
      }
      val qName: String = msg.qName
      val actorClassName = elementMap.get(qName)
      if (actorClassName != null) spawnTemplateActor(actorClassName)
    }
    templateActor ! m
  }

  def character(msg: CharactersMsg) {
    debug(msg)
    templateActor ! msg
  }

  def endElement(msg: EndElementMsg) {
    debug(msg)
    context.contextMap = contextMapStack.removeFirst
    depth -= 1
    templateActor ! msg
    if (depth < limit) {
      limit = -1
      templateActorStack.removeFirst
    }
  }

  def spawnTemplateActor(actorClassName: String) {
    limit = depth
    val routerContext = Context(context.contextMap)
    val router = TemplateRouterActor(localContext, userUuid, routerContext, player, composer)
    var agent: InternalAddressActor = null
    try {
      agent = Actors(localContext).actorFromClassName(ClassName(actorClassName))
    } catch {
      case ex: Throwable => error(composer, ex)
    }
    templateActorStack.addFirst(agent)
    agent ! StartProcessMsg(userUuid, context.contextMap, player, router)
  }
}

object TemplateRouterActor {
  def apply(
          systemContext: SystemComposite,
          userUuid: String,
          context: Context,
          player: InternalAddress,
          composer: InternalAddress) = {
    val router = Actors(systemContext).actorFromClassName(ClassName(classOf[TemplateRouterActor])).
            asInstanceOf[TemplateRouterActor]
    router.userUuid = userUuid
    router.context = context
    router.templateActorStack.addFirst(composer)
    router.player = player
    router.composer = composer
    router
  }
}