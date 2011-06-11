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
package player

import org.agilewiki.web.template.composer.PushMsg
import org.agilewiki.web.template.composer.PopMsg
import org.agilewiki.web.template.composer.AckMsg
import util.actors._
import util.actors.res._
import org.agilewiki.actors.ActorLayer
import util.SystemComposite

class SaxListPlayerActor(systemContext: SystemComposite, uuid: String)
        extends SynchronousActor(systemContext, uuid) {
  var router: InternalAddress = null
  var template: SaxMessageList = null
  var ndx = 0

  override def messageHandler = {
    case msg: StartPlayerMsg if router == null => start(msg)
    case msg: AckMsg => ack(msg)
    case msg => unexpectedMsg(router, msg)
  }

  def start(msg: StartPlayerMsg) {
    debug(msg)
    router = msg.requester
    router ! PushMsg(this)
  }

  def ack(msg: AckMsg) {
    debug(msg)
    if (template.size == ndx) router ! PopMsg()
    else {
      val msg = template.get(ndx)
      ndx += 1
      router ! msg
    }
  }
}

object SaxListPlayerActor {
  def apply(systemContext: SystemComposite, template: SaxMessageList) = {
    val player = Actors(systemContext).actorFromClassName(ClassName(classOf[SaxListPlayerActor])).
            asInstanceOf[SaxListPlayerActor]
    player.template = template
    player
  }
}
