/*
 * Copyright 2010 M.Naji
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
package udp

import java.net.{DatagramPacket, DatagramSocket, InetAddress}
import udpMsgs._
import util.actors.{AsynchronousActor, Actors}

class UdpSender(systemContext: SystemComposite, uuid: String) extends AsynchronousActor(systemContext, uuid) {
  var socket: DatagramSocket = null

  override def messageHandler = {
    case msg: SendBasicRequest => send(msg)
    case msg: SendBasicReply => send(msg)
    case (host: String, port: Int, msg: SendRequestMessage) => send(host, port, msg)
    case (host: String, port: Int, msg: SendReplyMessage) => send(host, port, msg)
    case msg => unexpectedMsg(msg)
  }

  private def send(msg: SendBasicRequest) {
    val srcArk = util.Configuration(localContext).localServerName
    val dstArk = msg.ark
    val reqUuid = msg.requester.getUuid
    val repActorName = msg.repActorName
    try {
      send(msg.hostPort.host,
        msg.hostPort.port,
        SendRequestMessage(srcArk, dstArk, reqUuid, repActorName, msg.payload))
    } catch {
      case ex: Throwable => error(msg, ex)
    }
  }

  private def send(msg: SendBasicReply) {
    val srcArk = util.Configuration(localContext).localServerName
    val dstArk = msg.ark
    val reqUuid = msg.reqUuid
    try {
      send(msg.hostPort.host,
        msg.hostPort.port,
        SendReplyMessage(srcArk, dstArk, reqUuid, msg.payload))
    } catch {
      case ex: Throwable => error(ex)
    }
  }

  private def send(host: String, port: Int, msg: SendRequestMessage) {
    val payload = msg.payload
    payload.writeUTF(msg.repActorName.toString)
    payload.writeUTF(msg.reqUuid)
    payload.writeUTF("0")
    payload.writeUTF(msg.dstArk)
    payload.writeUTF(msg.srcArk)
    val buffer = payload.getBytes
    //TODO: must throw an exception when the buffer is truncated
    //woops! sorry, no exceptions should be thrown. Only we need to send errors.
    //We also need to trap exceptions and send errors.
    val packet = new DatagramPacket(
      buffer, buffer.length,
      InetAddress.getByName(host), port)
    if (socket == null) socket = new DatagramSocket
    socket send packet
    debug("Basic Request;From: " + msg.srcArk +
            "->" + msg.reqUuid +
            ";To: " + msg.dstArk + "(" + host + ":" + port + ")" +
            "->" + msg.repActorName +
            " actors=" + Actors(localContext).actorsCanonicalMap +
            " actorLayer=" + localContext)
//    reply(0)
  }

  private def send(host: String, port: Int, msg: SendReplyMessage) {
    val payload = msg.payload
    payload.writeUTF(msg.reqUuid)
    payload.writeUTF("1")
    payload.writeUTF(msg.dstArk)
    payload.writeUTF(msg.srcArk)
    val buffer = payload.getBytes
    //TODO: must throw an ERROR when the buffer is truncated
    val packet = new DatagramPacket(
      buffer, buffer.length,
      InetAddress.getByName(host), port)
    if (socket == null) socket = new DatagramSocket
    socket send packet
    debug("Basic Reply;From: " + msg.srcArk +
            "->" + msg.reqUuid +
            ";To: " + msg.dstArk + "(" + host + ":" + port + ")")
//    reply(0)
  }

  def stopUdp {
    if (socket != null) {
      socket.close
      socket = null
    }
  }
}