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

import java.util.{TimerTask, HashSet, LinkedHashMap}
import cache.CanonicalMap

class PacketResender(reactor: LiteReactor,
                     insideActor: LiteActor,
                     outsideActor: LiteActor)
  extends LiteActor(reactor, null) {
  private val pinger = Udp(systemContext).pinger
  private val udp = Udp(systemContext)
  private val timeoutMin = udp.timeOutMin
  private val timeoutInc = udp.timeOutInc
  private val timeoutMax = udp.timeOutMax
  private val limit = udp.retryLimit
  private val cacheSize = udp.maxMessageUuidCacheSize

  private var timeLastMsgReceived = 0L
  private var timeoutDelay = timeoutMin
  private var pendingTimer: TimerTask = null
  private val sendRequests = new LinkedHashMap[String, LiteReqMsg]
  private val incomingReqUuids = new HashSet[String]
  private val outgoingRspCache = new CanonicalMap[OutgoingPacketReq](cacheSize)

  bind(classOf[OutgoingPacketReq], _outgoingPacket)
  bind(classOf[IncomingPacketReq], _incomingPacket)

  private def _outgoingPacket(msg: AnyRef, responseProcess: PartialFunction[Any, Unit]) {
    val req = msg.asInstanceOf[OutgoingPacketReq]
    if (req.retry) resend(req, responseProcess)
    else sendOut(req, responseProcess)
  }

  private def _incomingPacket(msg: AnyRef, responseProcess: PartialFunction[Any, Unit]) {
    val req = msg.asInstanceOf[IncomingPacketReq]
    if (req.isReply) forwardInReply(req, responseProcess)
    else forwardInReq(req, responseProcess)
  }

  def sendOut(packet: OutgoingPacketReq, responseProcess: PartialFunction[Any, Unit]) {
    packet.retry = true
    timeLastMsgReceived = System.currentTimeMillis
    if (pendingTimer == null) {
      timeoutDelay = timeoutMin
      scheduleRetry
    }
    val msgUuid = packet.msgUuid
    if (packet.isReply) {
      incomingReqUuids.remove(msgUuid)
      outgoingRspCache.put(msgUuid, packet)
    }
    else
      sendRequests.put(msgUuid, liteReactor.currentRequestMessage)
    outsideActor.send(packet) {
      case rsp: OutgoingPacketRsp => responseProcess(rsp)
    }
  }

  def resend(packet: OutgoingPacketReq, responseProcess: PartialFunction[Any, Unit]) {
    val currentTime = System.currentTimeMillis.asInstanceOf[Long]
    val expireTime = timeLastMsgReceived + limit
    pendingTimer = null
    if (sendRequests.size == 0) return
    if (currentTime - timeLastMsgReceived > limit) {
      val it = sendRequests.keySet.iterator
      while (it.hasNext) {
        val msgUuid = it.next
        val liteReqMsg = sendRequests.get(msgUuid)
        val rejectedPacket = liteReqMsg.content.asInstanceOf[OutgoingPacketReq]
        insideActor.send(rejectedPacket) {
          case null =>
        }
      }
      sendRequests.clear
    } else {
      timeoutDelay += timeoutInc
      if (timeoutDelay > timeoutMax) timeoutDelay = timeoutMax
      scheduleRetry
      outsideActor.send(packet) {
        case rsp: OutgoingPacketRsp =>
      }
    }
  }

  def forwardInReq(packet: IncomingPacketReq, responseProcess: PartialFunction[Any, Unit]) {
    timeLastMsgReceived = System.currentTimeMillis
    val msgUuid = packet.msgUuid
    if (incomingReqUuids.contains(msgUuid)) {
      responseProcess(IncomingPacketRsp())
    } else if (outgoingRspCache.has(msgUuid)) {
      val rspPacket = outgoingRspCache.get(msgUuid)
      outsideActor.send(rspPacket) {
        case rsp: OutgoingPacketRsp =>
      }
      responseProcess(IncomingPacketRsp())
    } else {
      incomingReqUuids.add(msgUuid)
      insideActor.send(packet) {
        case rsp: IncomingPacketRsp => responseProcess(rsp)
      }
    }
  }

  def forwardInReply(packet: IncomingPacketReq, responseProcess: PartialFunction[Any, Unit]) {
    timeLastMsgReceived = System.currentTimeMillis
    val msgUuid = packet.msgUuid
    if (sendRequests.containsKey(msgUuid)) {
      sendRequests.remove(msgUuid)
      insideActor.send(packet) {
        case rsp: IncomingPacketRsp => responseProcess(rsp)
      }
    } else responseProcess(IncomingPacketRsp())
    if (pendingTimer != null) {
      pendingTimer.cancel
      pendingTimer = null
    }
    if (sendRequests.size > 0) {
      timeoutDelay = timeoutMin
      scheduleRetry
    }
  }

  private def scheduleRetry {
    val oldestUuid = sendRequests.keySet.iterator.next
    val oldestReq = sendRequests.get(oldestUuid)
    val extendedRetryReq = ExtendedRetryReq(oldestReq, oldestReq.content, timeoutDelay)
    pinger.send(extendedRetryReq) {
      case prsp: ExtendedRetryRsp => {
        val tt = prsp.tt
        if (pendingTimer == null) pendingTimer = tt
        else tt.cancel
      }
    }
  }
}