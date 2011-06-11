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

import java.io.ByteArrayInputStream
import java.net.{DatagramPacket, DatagramSocket, InetAddress, SocketException}
import udpMsgs._
import scala.collection.mutable.HashMap
import actors.res._
import actors.Actors

class UdpListener(address: HostPort) extends Logger {
  private var udpRunner = new Thread

  private def started = udpRunner.isAlive

  private var socket: DatagramSocket = null
  private var buffer: Array[Byte] = new Array[Byte](0)
  private var sc: Option[SystemComposite] = None

  def port = address.port

  def host = address.host

  def systemContext: SystemComposite = sc match {
    case None => null
    case Some(x) => x
  }

  def receiveIncomingMessages = {
    try {
      do {
        val datagram = new DatagramPacket(buffer, buffer.length)
        socket.receive(datagram)
        receive(datagram)
      } while (true)
    } catch {
      case ex: SocketException => {
        if (socket != null && !socket.isClosed) error("Unknown socket error", ex)
        else debug("Stopping UdpListener thread for: " + address)
      }
      case ex: Throwable => error("Unknown error", ex)
    } finally {
      socket.close
      socket = null
    }
  }

  def receive(packet: DatagramPacket) {
    try {
      if (packet.getLength > 0) {
        val message = getMessage(packet)
        message match {
          case msg: ReceivedRequestMessage => {
            if (util.Configuration(systemContext).localServerName == msg.dstArk) {
              val ark = msg.srcArk
              val host = packet.getAddress.getHostName
              val port = packet.getPort
              val repActorName = msg.repActorName
              val reqUuid = msg.reqUuid
              val actor = repActorName.actor(systemContext)
              debug("forwarding basic request to: " + repActorName)
              actor ! ReceiveBasicRequest(ark, HostPort(host, port), reqUuid, msg.payload)
            } else debug("ark names don't match--request ignored")
          }
          case msg: ReceivedReplyMessage => {
            if (util.Configuration(systemContext).localServerName == msg.dstArk) {
              val ark = msg.srcArk
              val host = packet.getAddress.getHostName
              val port = packet.getPort
              val reqUuid = msg.reqUuid
              val actor = Actors(systemContext).canonicalActorFromUuid(reqUuid)
              if (actor != null) {
                actor ! ReceiveBasicReply(ark, HostPort(host, port), msg.payload)
              }
            }
          }
          case _ => throw new UnsupportedOperationException
        }
      }
    } catch {
      case ex: Throwable => error("Unable to receive UDP packet", ex)
    }
  }

  private def getMessage(packet: DatagramPacket) = {
    var msg: Any = null
    if (packet.getLength != 0) {
      val l = packet.getLength
      val bytes = new Array[Byte](l)
      System.arraycopy(packet.getData, packet.getOffset, bytes, 0, l)
      val bais = new ByteArrayInputStream(bytes)
      val payload = new DataInputStack(bais)
      val srcArk = payload.readUTF
      val dstArk = payload.readUTF
      val msgType = payload.readUTF
      val reqActor = payload.readUTF
      if (msgType == "0") {
        val repActor = payload.readUTF
        msg = ReceivedRequestMessage(srcArk, dstArk, reqActor, ResourceName(repActor), payload)
        debug("Received Request;From: " + srcArk +
                "->" + reqActor + ";To: " + dstArk + "->" + repActor)
      } else {
        msg = ReceivedReplyMessage(srcArk, dstArk, reqActor, payload)
        debug("Received Reply;From: " + srcArk +
                "->" + reqActor + ";To: " + dstArk)
      }
    }
    msg
  }

  private def startReceivingIncomingMessages {
    val bufferSize = Configuration(systemContext).requiredIntProperty(Udp.UDP_DATAGRAM_BUFFER_SIZE_PROPERTY)
    try {
      socket = new DatagramSocket(port, InetAddress.getByName(host))
    } catch {
      case ex: Throwable => {
        ex.printStackTrace
      }
    }
    buffer = new Array[Byte](bufferSize)
    receiveIncomingMessages
  }

  def startUdp(systemContext: SystemComposite) {
    sc = Some(systemContext)
    stopUdp
    udpRunner = new Thread {
      override def run() {
        debug("Starting UdpListener thread for: "+address.toString)
        startReceivingIncomingMessages
      }
    }
    udpRunner.start
  }

  def stopUdp {
    try {
      if (socket != null) socket.close
    } catch {
      case ex: Throwable => error("Unable to close socket", ex)
    }
    while (started) {
      Thread.sleep(1)
    }
  }
}

object UdpListener {
  private val listeners = new HashMap[HostPort, UdpListener]

  def apply(host: String, port: Int): UdpListener = {
    apply(HostPort(host, port))
  }

  def apply(address: HostPort): UdpListener = {
    val listener = listeners.get(address)
    listener match {
      case None => listeners += (address -> new UdpListener(address))
      case Some(x) =>
    }
    listeners(address)
  }

  def apply(systemContext: SystemComposite): UdpListener = {
    val listener = apply(Udp(systemContext).host, Udp(systemContext).port)
    listener.startUdp(systemContext)
    listener
  }

}