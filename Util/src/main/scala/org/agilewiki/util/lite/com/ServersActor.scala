/*
 * Copyright 2011 Bill La Forge
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
package util
package lite
package com

import java.util.TreeMap
import seq.LiteNavigableSetSeq

case class HostPortQueryReq(serverName: String)
case class HostPortQueryRsp(hostPort: HostPort)

case class HostPortUpdateReq(serverName: String, hostPort: HostPort)
case class HostPortUpdateRsp()

class ServersActor(reactor: ContextReactor) extends LiteActor(reactor) {
  private val map = new TreeMap[Comparable[AnyRef], HostPort]
  val serverSequenceActor = new LiteNavigableSetSeq(reactor, map.navigableKeySet)

  init

  private def init{
    val configuration = Configuration(systemContext)
    var ndx = 1
    var more = true
    while (more) {
      val prefix = "server." + ndx + "."
      val serverName = configuration.property(prefix + "name").asInstanceOf[Comparable[AnyRef]]
      if (serverName == null) more = false
      else {
        val host = configuration.requiredProperty(prefix + "host")
        val port = configuration.requiredIntProperty(prefix + "port")
        map.put(serverName, HostPort(host, port))
        ndx += 1
      }
    }
  }

  requestHandler = {
    case req: HostPortQueryReq => {
      reply(HostPortQueryRsp(map.get(req.serverName)))
    }
    case req: HostPortUpdateReq => {
      reply(HostPortUpdateRsp())
    }
  }
}