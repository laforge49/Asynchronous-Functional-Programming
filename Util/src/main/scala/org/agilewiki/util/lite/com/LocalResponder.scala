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

case class LocalResponderFactory() extends ActorFactory(UdpFactory.LOCAL_RESPONDER_FACTORY_NAME) {
  override def instantiate(reactor: LiteReactor) = {
    val lr = new LocalResponder(reactor)
    lr.factory(this)
    lr
  }
}

class LocalResponder(reactor: LiteReactor)
  extends LiteActor(reactor) {
  val udp = Udp(systemContext)
  private var maxPayloadSize = udp.maxPayloadSize
  private var retryLimit = udp.retryLimit
  val liteManager = Lite(systemContext).liteManager
  send(liteManager, MapPutReq(this)) {
    case rsp =>
  }
  send(liteManager, RememberReq(this, retryLimit)) {
    case rsp =>
  }
  addRequestHandler {
    case req: PacketReq => packetReq(req)
  }
  var largeReqPayload: DataOutputStack = null
  var largeRspBytes: Array[Byte] = null
  var rspPos = 0

  def packetReq(req: PacketReq) {
    val inputPayload = req.inputPayload
    if (inputPayload.size == 0) more
    val count = inputPayload.readInt
    if (count > 1) largeReq(count, req.server, inputPayload)
    else smallReq(req.server, inputPayload)
  }

  def smallReq(server: ServerName, inputPayload: DataInputStack) {
    val id = inputPayload.readId
    val pkt = new PacketReq(server, id, inputPayload)
    process(pkt)
  }

  def largeReq(count: Int, server: ServerName, inputPayload: DataInputStack) {
    if (largeReqPayload == null) largeReqPayload = new DataOutputStack
    val last = inputPayload.readByte.asInstanceOf[Boolean]
    val size = inputPayload.size
    val bytes = new Array[Byte](size)
    inputPayload.read(bytes)
    largeReqPayload.write(bytes)
    if (!last) {
      val ackRsp = new DataOutputStack
      ackRsp.writeId(id)
      reply(ackRsp)
    } else {
      val reqPayload = largeReqPayload.inputPayload
      largeReqPayload = null
      val id = reqPayload.readId
      val pkt = new PacketReq(server, id, reqPayload)
      process(pkt)
    }
  }

  def process(req: PacketReq) {
    req.actorName match {
      case rn: FactoryName => {
        val actor = Lite(systemContext).newActor(rn, newReactor)
        send(actor, req) {
          case rsp: DataOutputStack => packetRsp(rsp)
          case rsp => reply(rsp)
        }
      }
      case rn: ActorId => send(liteManager, ForwardReq(rn, req)) {
        case rsp: DataOutputStack => packetRsp(rsp)
        case rsp => reply(rsp)
      }
    }
  }

  def packetRsp(rsp: DataOutputStack) {
    if (rsp.size <= maxPayloadSize) smallRsp(rsp)
    else largeRsp(rsp)
  }

  def smallRsp(payload: DataOutputStack) {
    send(liteManager, ForgetReq(this)) {
      case _ =>
    }
    payload.writeInt(1)
    reply(payload)
  }

  def largeRsp(payload: DataOutputStack) {
    val size = payload.size
    largeRspBytes = new Array[Byte](size)
    val count: Int = (size + maxPayloadSize - 1) / maxPayloadSize
    val rspPayload = new DataOutputStack
    rspPayload.write(largeRspBytes, 0, maxPayloadSize)
    rspPayload.writeId(id)
    rspPayload.writeInt(count)
    rspPos = maxPayloadSize
    reply(rspPayload)
  }

  def more {
    val rspPayload = new DataOutputStack
    var len = largeRspBytes.size - rspPos
    if (len > maxPayloadSize) len = maxPayloadSize
    rspPayload.write(largeRspBytes, rspPos, len)
    rspPos += len
    reply(rspPayload)
  }
}