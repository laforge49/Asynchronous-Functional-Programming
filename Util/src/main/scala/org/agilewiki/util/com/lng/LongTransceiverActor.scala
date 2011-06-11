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
package org.agilewiki
package util
package com
package lng

import java.util.ArrayDeque
import java.util.UUID
import util.actors._
import util.actors.res._
import util.com.{DataInputStack, DataOutputStack}
import util.{SystemComposite, Configuration}
import util.com.shrt._

class LongTransceiverActor(systemContext: SystemComposite,
                           uuid: String)
        extends AsynchronousActor(systemContext, uuid) {
  private lazy val timeoutDelay = Configuration(localContext).requiredIntProperty(ShortProtocol.SHORT_LIMIT_PROPERTY)
  private var pendingTimer: Option[Cancellable] = None
  private val maxSize = Configuration(localContext).requiredIntProperty(ShortProtocol.MAX_PAYLOAD_SIZE_PROPERTY)
  private var sender: Option[InternalAddress] = None
  private var headers: Option[Any] = None
  private var local = false
  private var longMsgUuid: Option[String] = None
  private var longMsgType: Option[String] = None
  private var responder: Option[InternalAddressActor] = None
  private var bytes = Array[Byte]()
  private var pos = 0
  private var reqNbr = 0
  private var rspNbr = 0
  private var reqCount = 0
  private var rspCount = 0
  private var remoteArkName: Option[String] = None
  private var remoteTransceiverName: ResourceName = ClassName(getClass)
  private var shortTransceiverActor: Option[ShortTransceiverActor] = None

  final override def messageHandler = {
    case msg: ShortReq => shortReq(msg)
    case msg: ShortRsp => shortRsp(msg)
    case msg: SendLongReq if sender == None && responder == None => sendReq(msg)
    case msg: SendLongRsp if responder != None => sendRsp(msg)
    case msg: ShortError if !local => if (sender != None) shortError(msg)
    case msg: SendLongError if responder != None => sendError(msg)
    case msg: LongTimeout => timeout(msg)
    case msg => unexpectedMsg(msg)
  }

  private def sendReq(msg: SendLongReq) {
    longMsgUuid = Some(UUID.randomUUID.toString)
    debug("[" + longMsgUuid.get + "];" + msg)
    sender = Some(msg.requester)
    headers = Some(msg.header)
    remoteArkName = Some(msg.destinationArkName)
    if (remoteArkName == Some(util.Configuration(localContext).localServerName)) sendLocalReq(msg)
    else {
      longMsgType = Some("req")
      msg.payload.writeUTF(uuid)
      msg.payload.writeUTF(msg.destinationActorName.toString)
      sendRemote(msg.payload)
    }
  }

  private def sendRsp(msg: SendLongRsp) {
    debug(msg)
    if (local) sendLocalRsp(msg)
    else {
      longMsgType = Some("rsp")
      sendRemote(msg.payload)
    }
  }

  private def sendError(msg: SendLongError) {
    debug(msg)
    if (local) sendLocalError(msg)
    else {
      longMsgType = Some("err")
      val payload = DataOutputStack()
      payload.writeUTF(msg.txt)
      payload.writeUTF(msg.arkName)
      payload.writeUTF(msg.resourceName.toString)
      sendRemote(payload)
    }
  }

  private def sendLocalReq(msg: SendLongReq) {
    local = true
    val dataInput = DataInputStack(msg.payload)
    val req = LongReq(this, longMsgUuid.get, dataInput)
    responder = Some(msg.destinationActorName.actor(localContext))
    responder.get ! req
  }

  private def sendLocalRsp(msg: SendLongRsp) {
    responder = None
    val dataInput = DataInputStack(msg.payload)
    val rsp = LongRsp(if(headers.isEmpty)null else headers.get, dataInput)
    sender.get ! rsp
  }

  private def sendLocalError(msg: SendLongError) {
    responder = None
    val rsp = LongError(if(headers.isEmpty)null else headers.get, msg.txt, msg.arkName, msg.resourceName)
    sender.get ! rsp
  }

  private def sendRemote(payload: DataOutputStack) {
    if (payload.size > maxSize) sendLongRemote(payload)
    else sendShortRemote(payload)
  }

  private def sendShortRemote(payload: DataOutputStack) {
    payload.writeInt(payload.size)
    payload.writeUTF(longMsgType.get)
    payload.writeUTF(longMsgUuid.get)
    if (longMsgType == Some("req")) {
      reqNbr = 1
      reqCount = 1
    } else {
      rspNbr = 1
      rspCount = 1
    }
    sendBlock(payload)
  }

  private def sendLongRemote(payload: DataOutputStack) {
    bytes = payload.getBytes
    if (longMsgType == Some("req")) {
      reqNbr = 1
      reqCount = (bytes.length + maxSize - 1) / maxSize
    } else {
      rspNbr = 1
      rspCount = (bytes.length + maxSize - 1) / maxSize
    }
    val shortPayload = DataOutputStack()
    shortPayload.write(bytes, 0, maxSize)
    pos = maxSize
    shortPayload.writeInt(payload.size)
    shortPayload.writeUTF(longMsgType.get)
    shortPayload.writeUTF(longMsgUuid.get)
    sendBlock(shortPayload)
  }

  private def sendBlock(payload: DataOutputStack) {
    if (longMsgType == Some("req")) {
      payload.writeInt(reqNbr)
      payload.writeInt(reqCount)
    } else {
      payload.writeInt(rspNbr)
      payload.writeInt(rspCount)
    }
    if (shortTransceiverActor != None)
      shortTransceiverActor.get.sendReq(this,
        remoteTransceiverName,
        new ArrayDeque[Any](0),
        payload)
    else ShortProtocol(localContext).actor.sendReq(this,
      remoteArkName.get,
      remoteTransceiverName,
      new ArrayDeque[Any](0),
      payload)
  }

  private def shortRsp(msg: ShortRsp) {
    debug(msg)
    val msgNbr = msg.payload.readInt
    if (sender != None) {
      if (reqNbr == msgNbr) {
        if (reqNbr == 1) {
          val rspUuid = msg.payload.readUTF
          remoteTransceiverName = new Uuid(rspUuid)
        }
        if (reqNbr < reqCount) {
          reqNbr = reqNbr + 1
          val shortPayload = DataOutputStack()
          if (reqNbr == reqCount) {
            shortPayload.write(bytes, pos, bytes.length - pos)
            pos = bytes.length
          } else {
            shortPayload.write(bytes, pos, maxSize)
            pos = pos + maxSize
          }
          sendBlock(shortPayload)
        } else {
          pendingTimer = Some(Pinger.schedule(this, LongTimeout(), timeoutDelay))
        }
      } else if (msgNbr + 1 != reqNbr) {
        sender.get !
                LongError(if(headers.isEmpty)null else headers.get, "long sender expecting ack for block " + reqNbr + ", not " + msgNbr, util.Configuration(localContext).localServerName, ClassName(getClass))
      }
    } else {
      if (rspNbr == msgNbr) {
        if (rspNbr == 1) {
          val rspUuid = msg.payload.readUTF
          remoteTransceiverName = new Uuid(rspUuid)
        }
        if (rspNbr < rspCount) {
          rspNbr = rspNbr + 1
          val shortPayload = DataOutputStack()
          if (rspNbr == rspCount) {
            shortPayload.write(bytes, pos, bytes.length - pos)
            pos = bytes.length
          } else {
            shortPayload.write(bytes, pos, maxSize)
            pos = pos + maxSize
          }
          sendBlock(shortPayload)
        }
      } else if (msgNbr + 1 != rspNbr) {
        error("long sender expecting ack for block " + rspNbr + ", not " + msgNbr)
      }
    }
  }

  private def shortReq(msg: ShortReq) {
    debug(msg)
    shortTransceiverActor = Some(msg.requester)
    if (pendingTimer != None) {
      pendingTimer.get.cancel
      pendingTimer = None
    }
    val payload = msg.payload
    val msgCount = payload.readInt
    val msgNbr = payload.readInt

    if (sender == None) {
      if (reqNbr == msgNbr) sendAck(msg)
      else if (reqNbr + 1 != msgNbr)
        msg.sendError("long receiver expecting " + (reqNbr + 1) + " but got " + msgNbr, util.Configuration(localContext).localServerName, Uuid("LongTransceiverActor"))
      else {
        reqNbr = msgNbr
        if (msgNbr == 1) gotFirstBlock(msgCount, msg)
        if (msgNbr == msgCount) gotLastBlock(msg)
        else gotBlock(msg)
      }
    } else {
      if (rspNbr == msgNbr) sendAck(msg)
      else if (rspNbr + 1 != msgNbr)
        msg.sendError("long receiver expecting " + (rspNbr + 1) + " but got " + msgNbr, util.Configuration(localContext).localServerName, Uuid("LongTransceiverActor"))
      else {
        rspNbr = msgNbr
        if (msgNbr == 1) gotFirstBlock(msgCount, msg)
        if (msgNbr == msgCount) gotLastBlock(msg)
        else gotBlock(msg)
      }
    }
  }

  private def gotFirstBlock(msgCount: Int, msg: ShortReq) {
    val payload = msg.payload
    longMsgUuid = Some(payload.readUTF)
    longMsgType = Some(payload.readUTF)
    val size = payload.readInt
    bytes = new Array[Byte](size)
    pos = 0
  }

  private def gotLastBlock(msg: ShortReq) {
    msg.payload.readFully(bytes, pos, bytes.length - pos)
    pos = bytes.length
    sendAck(msg)
    val dataInput = DataInputStack(bytes)
    if (longMsgType == Some("req")) gotReq(dataInput)
    else if (longMsgType == Some("rsp")) gotRsp(dataInput)
    else if (longMsgType == Some("err")) gotErr(dataInput)
  }

  private def gotBlock(msg: ShortReq) {
    msg.payload.readFully(bytes, pos, maxSize)
    pos = pos + maxSize
    sendAck(msg)
    pendingTimer = Some(Pinger.schedule(this, LongTimeout(), timeoutDelay))
  }

  private def sendAck(msg: ShortReq) {
    val payload = DataOutputStack()
    if (sender == None) {
      if (reqNbr == 1) payload.writeUTF(uuid)
      payload.writeInt(reqNbr)
    } else {
      if (rspNbr == 1) payload.writeUTF(uuid)
      payload.writeInt(rspNbr)
    }
    msg.sendRsp(payload)
  }

  private def gotReq(payload: DataInputStack) {
    val destinationActorName = ResourceName(payload.readUTF)
    remoteTransceiverName = new Uuid(payload.readUTF)
    val req = LongReq(this, longMsgUuid.get, payload)
    responder = Some(destinationActorName.actor(localContext))
    responder.get ! req
  }

  private def gotRsp(payload: DataInputStack) {
    responder = None
    val rsp = LongRsp(if(headers.isEmpty)null else headers.get, payload)
    sender.get ! rsp
  }

  private def gotErr(payload: DataInputStack) {
    responder = None
    val resourceName = ResourceName(payload.readUTF)
    val arkName = payload.readUTF
    val txt = payload.readUTF
    val rsp = LongError(if(headers.isEmpty)null else headers.get, txt, arkName, resourceName)
    sender.get ! rsp
  }

  private def shortError(msg: ShortError) {
    if (pendingTimer != None) {
      pendingTimer.get.cancel
      pendingTimer = None
    }
    sender.get ! LongError(if(headers.isEmpty)null else headers.get, msg.error, msg.serverName, msg.resourceName)
  }

  private def timeout(msg: LongTimeout) {
    debug(msg)
    pendingTimer = None
    if (sender != None) sender.get ! LongError(if(headers.isEmpty)null else headers.get, "Timeout", util.Configuration(localContext).localServerName, ClassName(getClass))
    else error("Timeout")
  }

  private[lng] def sendRsp(payload: DataOutputStack) {
    this ! SendLongRsp(payload)
  }

  private[lng] def sendError(txt: String, arkName: String, resourceName: ResourceName) {
    this ! SendLongError(txt, arkName, resourceName)
  }
}

object LongTransceiverActor {
  def apply(systemContext: SystemComposite,
                            sender: InternalAddress,
                            destinationArkName: String,
                            destinationActorName: ResourceName,
                            headers: Any,
                            payload: DataOutputStack) {
    val longTransceiverActor = Actors(systemContext).
            actorFromClassName(ClassName(classOf[LongTransceiverActor])).
            asInstanceOf[LongTransceiverActor]
    longTransceiverActor ! SendLongReq(sender,
      destinationArkName,
      destinationActorName,
      headers,
      payload)
  }
}

