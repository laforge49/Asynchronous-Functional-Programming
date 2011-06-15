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

import java.util.HashMap

class PacketRouter(systemContext: SystemComposite)
  extends LiteActor(new ContextReactor(systemContext)) {
  val serversActor = Udp(systemContext).serversActor
  val serverSequenceActor = serversActor.serverSequenceActor
  val map = new HashMap[String, LiteActor]

  addRequestHandler {
    case req: PacketReq => {
      val server = req.server
      if (map.containsKey(server)) packetReq(req)
      else send(serversActor, HostPortQueryReq(server)) {
        case sar: HostPortQueryRsp => {
          add(server, sar.hostPort)
          packetReq(req)
        }
      }
    }
    case req: IncomingPacketReq => {
      val server = req.server
      if (map.containsKey(server)) incomePacket(req)
      else send(serversActor, HostPortQueryReq(server)) {
        case sar: HostPortQueryRsp => {
          add(server, sar.hostPort)
          incomePacket(req)
        }
      }
    }
  }

  private def add(server: String, hostPort: HostPort) {
    val packetResponder = PacketResponder(systemContext, server, hostPort)
    map.put(server, packetResponder)
  }

  private def packetReq(req: PacketReq) {
    val server = req.server
    val packetResponder = map.get(server)
    if (packetResponder == null) throw new IllegalArgumentException("Unknown server: " + server)
    send(packetResponder, req) { case prr => reply(prr)}
  }

  private def incomePacket(req: IncomingPacketReq) {
    val server = req.server
    val packetResponder = map.get(server)
    if (packetResponder == null) throw new IllegalArgumentException("Unknown server: " + server)
    send(packetResponder, req) { case prr => reply(prr)}
  }
}
