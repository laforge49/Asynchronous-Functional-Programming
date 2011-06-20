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

import java.net.InetAddress

final object HostPort {
  def apply(hostName: String, port: Int): HostPort = HostPort(InetAddress.getByName(hostName), port)
}

final case class HostPort(inetAddress: InetAddress, port: Int)

sealed abstract class Packet(_payload: DataStack) {
  def outputPayload = _payload.asInstanceOf[DataOutputStack].clone
  def inputPayload = _payload.inputPayload
}

final case class PacketReq(server: String, actorName: ResourceName, payload: DataStack)
  extends Packet(payload)

sealed abstract class ExternalPacket(_isReply: Boolean,
                              _msgUuid: Uuid,
                              _hostPort: HostPort,
                              _server: String,
                              _actorName: ResourceName,
                              _payload: DataStack)
  extends Packet(_payload)

final case class OutgoingPacketReq(isReply: Boolean,
                             msgUuid: Uuid,
                             hostPort: HostPort,
                             server: String,
                             actorName: ResourceName,
                             payload: DataStack)
  extends ExternalPacket(isReply, msgUuid, hostPort, server, actorName, payload) {
  var retry = false
}

final case class OutgoingPacketRsp()

final case class IncomingPacketReq(isReply: Boolean,
                             msgUuid: Uuid,
                             hostPort: HostPort,
                             server: String,
                             actorName: ResourceName,
                             payload: DataStack)
  extends ExternalPacket(isReply, msgUuid, hostPort, server, actorName, payload)

final case class IncomingPacketRsp()
