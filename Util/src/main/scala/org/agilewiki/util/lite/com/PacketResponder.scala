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

import java.util.{UUID, HashMap}

object PacketResponder {
  def apply(reactor: LiteReactor, remoteServer: ServerName, hostPort: HostPort) = {
    val udpSender = Udp(reactor.systemContext).udpSender
    val packetResponder = new PacketResponder(reactor, hostPort)
    val packetResender = new PacketResender(
      reactor,
      packetResponder,
      udpSender)
    packetResender.id(ActorId(remoteServer.name))
    packetResponder.outsideActor = packetResender
    packetResponder
  }
}

class PacketResponder(reactor: LiteReactor, hostPort: HostPort)
  extends LiteActor(reactor, null) {
  var outsideActor: LiteActor = null
  val requestsSent = new HashMap[String, LiteReqMsg]
  private val liteManager = Udp(systemContext).liteManager
  private val defaultReactor = newReactor

  addRequestHandler{
    case packet: PacketReq => forwardOutgoingRequest(packet)(back)
    case packet: IncomingPacketReq => _incomingPacket(packet)(back)
    case packet: OutgoingPacketReq => timeout(packet)(back)
  }

    private def _incomingPacket(packet: IncomingPacketReq)
                               (responseProcess: PartialFunction[Any, Unit]) {
      if (packet.isReply) incomingReply(packet)(responseProcess)
      else incomingRequest(packet)(responseProcess)
    }

  def forwardOutgoingRequest(packet: PacketReq)
                               (responseProcess: PartialFunction[Any, Unit]) {
    val msgUuid = UUID.randomUUID.toString
    requestsSent.put(msgUuid, liteReactor.currentRequestMessage)
    val externalPacket = OutgoingPacketReq(
      false,
      msgUuid,
      hostPort,
      packet.server,
      packet.actorName,
      packet.payload)
    outsideActor.send(externalPacket) {
      case rsp: OutgoingPacketRsp =>
    }
  }

  def incomingRequest(packet: IncomingPacketReq)
                               (responseProcess: PartialFunction[Any, Unit]) {
    val req = PacketReq(packet.server, packet.actorName, packet.payload)
    packet.actorName match {
      case rn: FactoryName => {
        var actor: LiteActor = null
        try {
           actor = Lite(systemContext).newActor(rn, newReactor)
        } catch {
          case ex: Exception => sendErrorRsp(
            packet,
            new ErrorRsp("no such factory: "+rn.value, getClass.getName, ""),
            this)(responseProcess)
        }
        if (actor != null) actor.send(req) {
          case rsp: DataOutputStack => sendRsp(packet, rsp, actor)(responseProcess)
          case error: ErrorRsp => sendErrorRsp(packet, error, actor)(responseProcess)
        }
      }
      case rn: ActorId => liteManager.send(MapGetReq(rn)) {
        case rsp: MapGetRsp => {
          val actor = rsp.actor
          if (actor != null) {
            rsp.actor.send(req) {
              case rsp: DataOutputStack => sendRsp(packet, rsp, actor)(responseProcess)
              case error: ErrorRsp => sendErrorRsp(packet, error, actor)(responseProcess)
            }
          } else throw new IllegalArgumentException
        }
      }
    }
  }

  private def sendRsp(incomingPacket: IncomingPacketReq,
                      outputPayload: DataOutputStack,
                      actor: LiteActor)
                               (responseProcess: PartialFunction[Any, Unit]) {
    outputPayload.writeByte(false.asInstanceOf[Byte])
    val externalPacket = OutgoingPacketReq(
      true,
      incomingPacket.msgUuid,
      hostPort,
      incomingPacket.server,
      actor.id,
      outputPayload)
    outsideActor.send(externalPacket) {
      case rsp => reply(rsp)
    }
  }

  private def sendErrorRsp(incomingPacket: IncomingPacketReq,
                           error: ErrorRsp,
                           actor: LiteActor)
                               (responseProcess: PartialFunction[Any, Unit]) {
    val outputPayload = DataOutputStack()
    outputPayload.writeUTF(error.target.toString)
    outputPayload.writeUTF(error.source.toString)
    outputPayload.writeUTF(error.text)
    outputPayload.writeByte(true.asInstanceOf[Byte])
    val externalPacket = OutgoingPacketReq(
      true,
      incomingPacket.msgUuid,
      hostPort,
      incomingPacket.server,
      actor.id,
      outputPayload)
    outsideActor.send(externalPacket) {
      case rsp => reply(rsp)
    }
  }

  def incomingReply(packet: IncomingPacketReq)
                               (responseProcess: PartialFunction[Any, Unit]) {
    val liteReqMsg = requestsSent.remove(packet.msgUuid)
    val inputPayload = packet.inputPayload
    val isError = inputPayload.readByte.asInstanceOf[Boolean]
    var rsp: AnyRef = null
    if (isError) {
      val txt = inputPayload.readUTF
      val src = inputPayload.readId
      val target = inputPayload.readId
      rsp = new ErrorRsp(txt, src.value, target.value)
    } else {
      rsp = inputPayload
    }
    val actor = liteReqMsg.sender.asInstanceOf[LiteActor]
    val liteRspMsg = new LiteRspMsg(liteReqMsg.responseProcess, liteReqMsg, rsp)
    actor.liteReactor.response(liteRspMsg)
  }

  def timeout(packet: OutgoingPacketReq)
                               (responseProcess: PartialFunction[Any, Unit]) {
    val liteReqMsg = requestsSent.remove(packet.msgUuid)
    val actor = liteReqMsg.sender.asInstanceOf[LiteActor]
    val error = new ErrorRsp(
      "timeout",
      this.getClass.getName,
      outsideActor.getClass.getName)
    val liteRspMsg = new LiteRspMsg(liteReqMsg.responseProcess, liteReqMsg, error)
    actor.liteReactor.response(liteRspMsg)
  }
}