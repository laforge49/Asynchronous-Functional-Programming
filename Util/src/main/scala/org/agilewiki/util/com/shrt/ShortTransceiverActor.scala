/*
 * Copyright 2010 Bill La Forge
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

package org.agilewiki.util
package com
package shrt

import java.util.HashSet
import java.util.LinkedHashMap
import java.util.UUID
import udp.{ReceiveBasicRequest, SendBasicReply, ReceiveBasicReply, SendBasicRequest}
import actors.res.{ClassName, ResourceName}
import cache.Cache
import actors._
import ark.actor._
import ark.Ark

class ShortTransceiverActor(systemContext: SystemComposite, uuid: String)
        extends AsynchronousActor(systemContext, uuid) {
  
  private val timeoutMin = Configuration(localContext).
          requiredIntProperty(ShortProtocol.SHORT_TIMEOUT_MIN_PROPERTY) + 0L
  private val timeoutInc = Configuration(localContext).
          requiredIntProperty(ShortProtocol.SHORT_TIMEOUT_INC_PROPERTY) + 0L
  private val timeoutMax = Configuration(localContext).
          requiredIntProperty(ShortProtocol.SHORT_TIMEOUT_MAX_PROPERTY) + 0L
  private val limit = Configuration(localContext).
          requiredIntProperty(ShortProtocol.SHORT_LIMIT_PROPERTY)
  private val shortMsgUuidCache = new Cache[String](Configuration(localContext).
          requiredIntProperty(ShortProtocol.MAX_SHORT_MSG_UUID_CACHE_SIZE_PROPERTY))
  private[shrt] var _destinationArkName: String = null

  def destinationArkName = _destinationArkName

  private[shrt] var local = false
  private var hostPort: HostPort = null
  private var timeLastMsgReceived = 0L
  private var timeoutDelay = timeoutMin
  private var pendingTimer: Cancellable = null
  private val sendRequests = new LinkedHashMap[String, SendShortReq]
  private val receivedMsgUuids = new HashSet[String]
  private[shrt] var udpSender: InternalAddress = null
  private[shrt] var shortResourceName: ResourceName = null
  private var requesterUuid: String = null
  private var error = false

  final override def messageHandler = {
    case msg: ArkLookupRsp => {
      //      debug(msg)
      hostPort = msg.hostPort
    }
    case msg: SendShortReq if hostPort != null => sendReq(msg)
    case msg: ReceiveBasicRequest if hostPort != null => receiveBasicRequest(msg)
    case msg: SendShortRsp if hostPort != null => sendRsp(msg)
    case msg: ReceiveBasicReply if hostPort != null => receiveBasicReply(msg)
    case msg: SendShortError if hostPort != null => sendError(msg)
    case msg: ShortTimeout if hostPort != null => timeout(msg)
    case msg: ArkErrorMsg => {
      unexpectedMsg(msg)
      error = true
    }
    case msg if error || hostPort != null => unexpectedMsg(msg)
  }

  private def sendReq(msg: SendShortReq) {
    val msgUuid = UUID.randomUUID.toString
    //    debug(" added to short sendRequests: [" + msgUuid + "];" + msg)
    sendRequests.put(msgUuid, msg)
    if (local) sendLocalReq(msgUuid, msg)
    else {
      val dataOutput = msg.payload.clone
      val dest = msg.destinationActorName.toString
      dataOutput.writeUTF(dest)
      dataOutput.writeUTF(msgUuid)
      val sendBasicRequest = SendBasicRequest(this,
        msg.destinationArkName,
        hostPort,
        shortResourceName,
        dataOutput)
      udpSender ! sendBasicRequest
      if (pendingTimer == null) startTimer
    }
  }

  private def sendLocalReq(msgUuid: String, msg: SendShortReq) {
    val dataInput = DataInputStack(msg.payload)
    val req = ShortReq(this, msgUuid, dataInput)
    val actor = msg.destinationActorName.actor(localContext)
    actor ! req
  }

  private def receiveBasicRequest(msg: ReceiveBasicRequest) {
    //    debug(msg)
    timeLastMsgReceived = System.currentTimeMillis
    val p = msg.payload
    val msgUuid = p.readUTF()
    if (!receivedMsgUuids.contains(msgUuid)) {
      requesterUuid = msg.reqUuid
      receivedMsgUuids.add(msgUuid)
      val destinationActorName = p.readUTF
      val req = ShortReq(this, msgUuid, p)
      var actor: InternalAddressActor = null
      try {
        actor = ResourceName(destinationActorName).actor(localContext)
      } catch {
        case ex: Throwable => error("Unable to instantiate " + destinationActorName)
      }
      if (actor != null) actor ! req
      else debug("null actor")
    }
  }

  private[shrt] def sendError(msg: SendShortError) {
    //    debug(msg)
    if (local) sendLocalError(msg)
    else if (!receivedMsgUuids.remove(msg.msgUuid))
      error("unexpected msgUuid in SendShortRsp")
    else {
      shortMsgUuidCache.apply(msg.msgUuid)
      val payload = DataOutputStack()
      payload.writeUTF(msg.resourceName.toString)
      payload.writeUTF(msg.arkName)
      payload.writeUTF(msg.txt)
      payload.writeUTF(msg.msgUuid)
      payload.writeBoolean(true)
      val sendBasicReply = SendBasicReply(_destinationArkName,
        hostPort,
        requesterUuid,
        payload)
      udpSender ! sendBasicReply
    }
  }

  private def sendLocalError(msg: SendShortError) {
    val msgUuid = msg.msgUuid
    if (!sendRequests.containsKey(msgUuid))
      error("Unexpected local msgUuid in SendShortError: " + String.valueOf(msg))
    else {
      //      debug("got short error: "+msgUuid)
      val req = sendRequests.remove(msgUuid)
      val actor = req.requester
      val error = ShortError(req.header, msg.txt, msg.arkName, msg.resourceName)
      actor ! error
    }
  }

  private[shrt] def sendRsp(msg: SendShortRsp) {
    //    debug(msg)
    if (local) sendLocalRsp(msg)
    else if (!receivedMsgUuids.remove(msg.msgUuid))
      error("unexpected msgUuid in SendShortRsp")
    else {
      shortMsgUuidCache.apply(msg.msgUuid)
      msg.payload.writeUTF(msg.msgUuid)
      msg.payload.writeBoolean(false)
      val sendBasicReply = SendBasicReply(_destinationArkName,
        hostPort,
        requesterUuid,
        msg.payload)
      udpSender ! sendBasicReply
    }
  }

  private def sendLocalRsp(msg: SendShortRsp) {
    val msgUuid = msg.msgUuid
    if (!sendRequests.containsKey(msgUuid))
      error("Unexpected msgUuid in SendShortRsp: " + String.valueOf(msgUuid))
    else {
      //      debug("got local response: "+msgUuid)
      val req = sendRequests.remove(msgUuid)
      val dataInput = DataInputStack(msg.payload)
      val actor = req.requester
      val rsp = ShortRsp(req.header, dataInput)
      actor ! rsp
    }
  }

  private def receiveBasicReply(msg: ReceiveBasicReply) {
    try {
      debug(msg)
      timeLastMsgReceived = System.currentTimeMillis
      val p = msg.payload
      val isError = p.readBoolean
      val msgUuid = p.readUTF
      if (!sendRequests.containsKey(msgUuid))
        warn("Unexpeced msgUuid in ReceiveBasicReply: " + String.valueOf(msg))
      else {
        debug("got remote reply: " + msgUuid)
        val req = sendRequests.remove(msgUuid)
        if (req != null) {
          val actor = req.requester
          if (isError) {
            val txt = p.readUTF
            val arkName = p.readUTF
            val resourceName = ResourceName(p.readUTF)
            val err = ShortError(req.header, txt, arkName, resourceName)
            actor ! err
          } else {
            val rsp = ShortRsp(req.header, p)
            actor ! rsp
          }
        } else {
          debug("null req msg")
        }
      }
      if (pendingTimer != null)
        pendingTimer.cancel
      if (sendRequests.size == 0)
        pendingTimer = null
      else {
        timeoutDelay = timeoutMin
        pendingTimer = Pinger.schedule(this, ShortTimeout(), timeoutDelay)
      }
    } catch {
      case ex: Throwable => error(ex)
    }
  }

  private def startTimer {
    timeLastMsgReceived = System.currentTimeMillis.asInstanceOf[Long]
    timeoutDelay = timeoutMin
    //    debug("[Timer-" + timeLastMsgReceived + "];" + "Scheduled after:" + timeoutDelay)
    pendingTimer = Pinger.schedule(this, ShortTimeout(), timeoutDelay)
  }

  private def timeout(msg: ShortTimeout) {
    //    debug("[Timer-" + timeLastMsgReceived + "];" + "Timeouted")
    val currentTime = System.currentTimeMillis.asInstanceOf[Long]
    val expireTime = timeLastMsgReceived + limit
    if (sendRequests.size == 0) {
      pendingTimer = null
    } else if (currentTime - timeLastMsgReceived > limit) {
      //      debug("big timeout--clearing all sendRequests")
      pendingTimer = null
      val it = sendRequests.keySet.iterator
      while (it.hasNext) {
        val msgUuid = it.next
        val msg = sendRequests.get(msgUuid).asInstanceOf[SendShortReq]
        msg.requester ! ShortError(msg.header, "Timeout", Configuration(localContext).localServerName, ClassName(getClass))
      }
      sendRequests.clear
    } else {
      val it = sendRequests.keySet.iterator
      val msgUuid = it.next
      //      debug("resending oldest msg: "+msgUuid)
      val msg = sendRequests.get(msgUuid)
      val list = msg.payload.clone
      list.writeUTF(msg.destinationActorName.toString)
      list.writeUTF(msgUuid)
      val sendBasicRequest = SendBasicRequest(this,
        _destinationArkName,
        hostPort,
        shortResourceName,
        list)
      udpSender ! sendBasicRequest
      timeoutDelay += timeoutInc
      if (timeoutDelay > timeoutMax) timeoutDelay = timeoutMax
      //      debug("[Timer-" + timeLastMsgReceived + "];" + "Scheduled after:" + timeoutDelay)
      pendingTimer = Pinger.schedule(this, ShortTimeout(), timeoutDelay)
    }
  }

  private[shrt] def sendRsp(msgUuid: String, payload: DataOutputStack) {
    this ! SendShortRsp(msgUuid, payload)
  }

  private[shrt] def sendError(msgUuid: String, txt: String, arkName: String,
                              resourceName: ResourceName) {
    this ! SendShortError(msgUuid, txt, arkName, resourceName)
  }

  def sendReq(sender: InternalAddress,
              destinationActorName: ResourceName,
              headers: Any,
              payload: DataOutputStack) {
    this ! SendShortReq(sender,
      _destinationArkName,
      destinationActorName,
      headers,
      payload)
  }
}

object ShortTransceiverActor {
  def apply(systemContext: SystemComposite,
            destinationArkName: String,
            udpSender: InternalAddress,
            shortResourceName: ResourceName) = {
    val shortTransceiverActor = Actors(systemContext).actorFromClassName(ClassName(classOf[ShortTransceiverActor])).
            asInstanceOf[ShortTransceiverActor]
    shortTransceiverActor._destinationArkName = destinationArkName
    shortTransceiverActor.local = destinationArkName == Configuration(systemContext).localServerName
    shortTransceiverActor.udpSender = udpSender
    shortTransceiverActor.shortResourceName = shortResourceName
    Ark(systemContext).actor ! ArkLookupReq(shortTransceiverActor, destinationArkName)
    shortTransceiverActor
  }
}
