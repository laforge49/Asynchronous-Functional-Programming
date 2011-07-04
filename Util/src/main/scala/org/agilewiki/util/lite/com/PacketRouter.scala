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

class PacketRouter(reactor: LiteReactor)
  extends LiteActor(reactor, null) {
  val serversActor = Udp(systemContext).serversActor
  val serverSequenceActor = serversActor.serverSequenceActor
  val map = new HashMap[String, LiteActor]

  bind(classOf[PacketReq], _packet)
  bind(classOf[IncomingPacketReq], _incomingPacket)

  private def _packet(msg: AnyRef, responseProcess: PartialFunction[Any, Unit]) {
    val req = msg.asInstanceOf[PacketReq]
    val server = req.server
    if (map.containsKey(server)) packetReq(req, responseProcess)
    else serversActor.send(HostPortQueryReq(server)) {
      case sar: HostPortQueryRsp => {
        add(server, sar.hostPort)
        packetReq(req, responseProcess)
      }
    }
  }

  private def _incomingPacket(msg: AnyRef, responseProcess: PartialFunction[Any, Unit]) {
    val req = msg.asInstanceOf[IncomingPacketReq]
    val server = req.server
    if (map.containsKey(server)) incomePacket(req, responseProcess)
    else serversActor.send(HostPortQueryReq(server)) {
      case sar: HostPortQueryRsp => {
        add(server, sar.hostPort)
        incomePacket(req, responseProcess)
      }
    }
  }

  private def add(server: ServerName, hostPort: HostPort) {
    val packetResponder = PacketResponder(newReactor, server, hostPort)
    map.put(server.name, packetResponder)
  }

  private def packetReq(req: PacketReq, responseProcess: PartialFunction[Any, Unit]) {
    val server = req.server
    val packetResponder = map.get(server)
    if (packetResponder == null) throw new IllegalArgumentException("Unknown server: " + server.name)
    packetResponder.send(req) {
      case prr => responseProcess(prr)
    }
  }

  private def incomePacket(req: IncomingPacketReq, responseProcess: PartialFunction[Any, Unit]) {
    val server = req.server
    val packetResponder = map.get(server)
    if (packetResponder == null) throw new IllegalArgumentException("Unknown server: " + server.name)
    packetResponder.send(req) {
      case prr => responseProcess(prr)
    }
  }
}
