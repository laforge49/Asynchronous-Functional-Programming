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

  bind(classOf[PacketReq], packetReq)

  private def packetReq(msg: AnyRef, responseProcess: PartialFunction[Any, Unit]) {
    val req = msg.asInstanceOf[PacketReq]
    if (req.server == localServerName) process(req, responseProcess)
    else remoteReq(req, responseProcess)
  }

  private def process(pkt: PacketReq, responseProcess: PartialFunction[Any, Unit]) {
    pkt.actorName match {
      case rn: FactoryName => {
        val actor = Lite(systemContext).newActor(rn, newReactor)
        actor.send(pkt) {
          case rsp => responseProcess(rsp)
        }
      }
      case rn: ActorId => liteManager.send(ForwardReq(rn, pkt)) {
        case rsp => responseProcess(rsp)
      }
    }
  }

  private def remoteReq(pkt: PacketReq, responseProcess: PartialFunction[Any, Unit]) {
    val outputPayload = pkt.outputPayload
    outputPayload.writeUTF(pkt.actorName.toString)
    if (outputPayload.size <= maxPayloadSize) smallReq(pkt.server, outputPayload, responseProcess)
    else largeReq(pkt.server, outputPayload, responseProcess)
  }

  private def smallReq(server: ServerName, outputPayload: DataOutputStack, responseProcess: PartialFunction[Any, Unit]) {
    outputPayload.writeInt(1)
    val req = PacketReq(server, UdpFactory.LOCAL_RESPONDER_FACTORY_NAME, outputPayload)
    packetRouter.send(req) {
      case rsp: DataStack => packetRsp(server, rsp, responseProcess)
      case rsp => responseProcess(rsp)
    }
  }

  private def largeReq(server: ServerName, outputPayload: DataOutputStack, responseProcess: PartialFunction[Any, Unit]) {
    val bytes = outputPayload.getBytes
    val count: Int = (bytes.size + maxPayloadSize - 1) / maxPayloadSize
    partReq(server, UdpFactory.LOCAL_RESPONDER_FACTORY_NAME, count, 1, bytes, responseProcess)
  }

  private def partReq(server: ServerName,
                      actorName: ActorName,
                      count: Int,
                      ndx: Int,
                      bytes: Array[Byte],
                      responseProcess: PartialFunction[Any, Unit]) {
    val outputPayload = new DataOutputStack
    val pos = ndx * maxPayloadSize
    var len = bytes.size - pos
    if (len > maxPayloadSize) len = maxPayloadSize
    outputPayload.write(bytes, pos, len)
    val last = ndx == count
    outputPayload.writeByte(last.asInstanceOf[Byte])
    outputPayload.writeInt(count)
    val req = PacketReq(server, actorName, outputPayload)
    packetRouter.send(req) {
      case rsp: DataStack => {
        if (last) packetRsp(server, rsp, responseProcess)
        else {
          val inputPayload = rsp.inputPayload
          val responderName = inputPayload.readId
          partReq(server, responderName, count, ndx + 1, bytes, responseProcess)
        }
      }
      case rsp => responseProcess(rsp)
    }
  }

  private def packetRsp(server: ServerName, payload: DataStack, responseProcess: PartialFunction[Any, Unit]) {
    val inputPayload = payload.inputPayload
    val count = inputPayload.readInt
    if (count > 1) largeRsp(server, count, inputPayload, responseProcess)
    else responseProcess(inputPayload)
  }

  private def largeRsp(server: ServerName, count: Int, inputPayload: DataInputStack, responseProcess: PartialFunction[Any, Unit]) {
    val responderName = inputPayload.readId
    val payload = new DataOutputStack
    partRsp(server, responderName, count, payload, inputPayload, responseProcess)
  }

  private def partRsp(server: ServerName,
                      responderName: ActorId,
                      count: Int,
                      payload: DataOutputStack,
                      inputPayload: DataInputStack,
                      responseProcess: PartialFunction[Any, Unit]) {
    val size = inputPayload.size
    val bytes = new Array[Byte](size)
    inputPayload.read(bytes)
    payload.write(bytes)
    if (count > 1) {
      val req = PacketReq(server, responderName, new DataOutputStack)
      packetRouter.send(req) {
        case rsp: DataStack => {
          partRsp(server, responderName, count - 1, payload, rsp.inputPayload, responseProcess)
        }
        case rsp => responseProcess(rsp)
      }
    } else responseProcess(payload)
  }
}
