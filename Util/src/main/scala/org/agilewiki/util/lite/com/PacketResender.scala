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

class PacketResender(reactor: ContextReactor, uuid: Uuid, insideActor: LiteActor, outsideActor: LiteActor)
  extends InternalAddressActor(reactor, uuid) {
  private val pinger = Lite(systemContext).pinger

  private val timeoutMin = Configuration(systemContext).
    requiredIntProperty(Udp.SHORT_TIMEOUT_MIN_PROPERTY) + 0L
  private val timeoutInc = Configuration(systemContext).
    requiredIntProperty(Udp.SHORT_TIMEOUT_INC_PROPERTY) + 0L
  private val timeoutMax = Configuration(systemContext).
    requiredIntProperty(Udp.SHORT_TIMEOUT_MAX_PROPERTY) + 0L
  private val limit = Configuration(systemContext).
    requiredIntProperty(Udp.SHORT_LIMIT_PROPERTY)
  private val cacheSize = Configuration(systemContext).
    requiredIntProperty(Udp.MAX_SHORT_MSG_UUID_CACHE_SIZE_PROPERTY)

  private var timeLastMsgReceived = 0L
  private var timeoutDelay = timeoutMin
  private var pendingTimer: TimerTask = null
  private val sendRequests = new LinkedHashMap[String, LiteReqMsg]
  private val incomingReqUuids = new HashSet[String]
  private val outgoingRspCache = new CanonicalMap[OutgoingPacketReq](cacheSize)

  requestHandler = {
    case packet: OutgoingPacketReq if packet.retry => resend(packet)
    case packet: OutgoingPacketReq if !packet.retry => sendOut(packet)
    case packet: IncomingPacketReq if packet.isReply => forwardInReply(packet)
    case packet: IncomingPacketReq if !packet.isReply => forwardInReq(packet)
  }

  def sendOut(packet: OutgoingPacketReq) {
    packet.retry = true
    timeLastMsgReceived = System.currentTimeMillis
    if (pendingTimer == null) {
      timeoutDelay = timeoutMin
      scheduleRetry
    }
    val msgUuid = packet.msgUuid.value
    if (packet.isReply) {
      incomingReqUuids.remove(msgUuid)
      outgoingRspCache.put(msgUuid, packet)
    }
    else
      sendRequests.put(msgUuid, currentReactor.currentRequestMessage)
    send(outsideActor, packet) {
      case rsp: OutgoingPacketRsp => reply(rsp)
    }
  }

  def resend(packet: OutgoingPacketReq) {
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
        send(insideActor, rejectedPacket) {
          case null =>
        }
      }
      sendRequests.clear
    } else {
      timeoutDelay += timeoutInc
      if (timeoutDelay > timeoutMax) timeoutDelay = timeoutMax
      scheduleRetry
      send(outsideActor, packet) {
        case rsp: OutgoingPacketRsp =>
      }
    }
  }

  def forwardInReq(packet: IncomingPacketReq) {
    timeLastMsgReceived = System.currentTimeMillis
    val msgUuid = packet.msgUuid.value
    if (incomingReqUuids.contains(msgUuid)) {
      reply(IncomingPacketRsp())
    } else if (outgoingRspCache.has(msgUuid)) {
      val rspPacket = outgoingRspCache.get(msgUuid)
      send(outsideActor, rspPacket) { case rsp: OutgoingPacketRsp => }
      reply(IncomingPacketRsp())
    } else {
      incomingReqUuids.add(msgUuid)
      send(insideActor, packet) {
        case rsp: IncomingPacketRsp => reply(rsp)
      }
    }
  }

  def forwardInReply(packet: IncomingPacketReq) {
    timeLastMsgReceived = System.currentTimeMillis
    val msgUuid = packet.msgUuid.value
    if (sendRequests.containsKey(msgUuid)) {
      sendRequests.remove(msgUuid)
      send(insideActor, packet) {
        case rsp: IncomingPacketRsp => reply(rsp)
      }
    } else reply(IncomingPacketRsp())
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
    send(pinger, extendedRetryReq) {
      case prsp: ExtendedRetryRsp => {
        val tt = prsp.tt
        if (pendingTimer == null) pendingTimer = tt
        else tt.cancel
      }
    }
  }
}