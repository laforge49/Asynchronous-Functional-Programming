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

class RemoteAccess(reactor: LiteReactor, packetRouter: LiteActor)
  extends LiteActor(reactor, null) {
  private val localServerName = LocalServerName(systemContext).name
  private var maxPayloadSize: Int = Udp(systemContext).maxPayloadSize
  private val liteManager = Udp(systemContext).liteManager
  addRequestHandler{
    case pkt: PacketReq => packetReq(pkt)
  }

  private def packetReq(pkt: PacketReq) {
    if (pkt.server == localServerName) process(pkt)
    else remoteReq(pkt)
  }

  private def process(pkt: PacketReq) {
    pkt.actorName match {
      case rn: FactoryName => {
        val actor = Lite(systemContext).newActor(rn, newReactor)
        send(actor, pkt) {
          case rsp => reply(rsp)
        }
      }
      case rn: ActorId => send(liteManager, ForwardReq(rn, pkt)) {
        case rsp => reply(rsp)
      }
    }
  }

  private def remoteReq(pkt: PacketReq) {
    val outputPayload = pkt.outputPayload
    outputPayload.writeUTF(pkt.actorName.toString)
    if (outputPayload.size <= maxPayloadSize) smallReq(pkt.server, outputPayload)
    else largeReq(pkt.server, outputPayload)
  }

  private def smallReq(server: ServerName, outputPayload: DataOutputStack) {
    outputPayload.writeInt(1)
    val req = PacketReq(server, UdpFactory.LOCAL_RESPONDER_FACTORY_NAME, outputPayload)
    send(packetRouter, req) {
      case rsp: DataStack => packetRsp(server, rsp)
      case rsp => reply(rsp)
    }
  }

  private def largeReq(server: ServerName, outputPayload: DataOutputStack) {
    val bytes = outputPayload.getBytes
    val count: Int = (bytes.size + maxPayloadSize - 1) / maxPayloadSize
    partReq(server, UdpFactory.LOCAL_RESPONDER_FACTORY_NAME, count, 1, bytes)
  }

  private def partReq(server: ServerName,
                      actorName: ActorName,
                      count: Int,
                      ndx: Int,
                      bytes: Array[Byte]) {
    val outputPayload = new DataOutputStack
    val pos = ndx * maxPayloadSize
    var len = bytes.size - pos
    if (len > maxPayloadSize) len = maxPayloadSize
    outputPayload.write(bytes, pos, len)
    val last = ndx == count
    outputPayload.writeByte(last.asInstanceOf[Byte])
    outputPayload.writeInt(count)
    val req = PacketReq(server, actorName, outputPayload)
    send(packetRouter, req) {
      case rsp: DataStack => {
        if (last) packetRsp(server, rsp)
        else {
          val inputPayload = rsp.inputPayload
          val responderName = inputPayload.readId
          partReq(server, responderName, count, ndx + 1, bytes)
        }
      }
      case rsp => reply(rsp)
    }
  }

  private def packetRsp(server: ServerName, payload: DataStack) {
    val inputPayload = payload.inputPayload
    val count = inputPayload.readInt
    if (count > 1) largeRsp(server, count, inputPayload)
    else reply(inputPayload)
  }

  private def largeRsp(server: ServerName, count: Int, inputPayload: DataInputStack) {
    val responderName = inputPayload.readId
    val payload = new DataOutputStack
    partRsp(server, responderName, count, payload, inputPayload)
  }

  private def partRsp(server: ServerName,
                      responderName: ActorId,
                      count: Int,
                      payload: DataOutputStack,
                      inputPayload: DataInputStack) {
    val size = inputPayload.size
    val bytes = new Array[Byte](size)
    inputPayload.read(bytes)
    payload.write(bytes)
    if (count > 1) {
      val req = PacketReq(server, responderName, new DataOutputStack)
      send(packetRouter, req) {
        case rsp: DataStack => {
          partRsp(server, responderName, count - 1, payload, rsp.inputPayload)
        }
        case rsp => reply(rsp)
      }
    } else reply(payload)
  }
}
