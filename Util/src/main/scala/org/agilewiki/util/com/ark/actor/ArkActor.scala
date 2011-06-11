/*
 * Copyright 2010 M.Naji
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

package org.agilewiki.util
package com
package ark
package actor

import actors.{Actors, AsynchronousActor}
import actors.res.ClassName
import shrt.ShortReq
import udp.Udp

class ArkActor(systemContext: SystemComposite, uuid: String)
        extends AsynchronousActor(systemContext, uuid) {
  Actors(systemContext).remember(this)
  val arks = Servers(systemContext).map

  override def messageHandler = {
    case msg: ArkLookupReq => lookupReq(msg)
    case msg: ArksReq => arksReq(msg)
    case msg: StartArkServices => debug(msg); Ark.startArkServices(systemContext)
    case msg: ShortReq => debug(msg); handle(msg)
    case msg => unexpectedMsg(msg)
  }

  def lookupReq(msg: ArkLookupReq) {
    debug(msg)
    val hp = arks(msg.arkName)
    if (hp == null) msg.requester ! ArkErrorMsg(null, "no such ark: " + msg.arkName,
      Configuration(systemContext).localServerName, ClassName(getClass.getName))
    else msg.requester ! ArkLookupRsp(msg.arkName, hp)
  }

  def arksReq(msg: ArksReq) {
    debug(msg)
    var arkSet = Set[String]()
    for (ark <- arks.keySet) arkSet += ark
    msg.requester ! ArksRsp(arkSet)
  }

  def handle(msg: ShortReq) {
    if (validate(msg)) {
      val verb = msg.payload.readUTF
      verb match {
        case "shutdown" => {
          info("Shutting down: " + Configuration(systemContext).localServerName)
          System.exit(0)
        }
        case "closeUdp" => {
          Udp(systemContext).close
        }
        case _ => unexpectedMsg(msg)
      }
    } else unexpectedMsg(msg)
  }



  //TODO: This should be replaced by a security algorithm
  private def validate(msg: ShortReq) = true

}

object ArkActor {
  val UUID = "ARK"
}
