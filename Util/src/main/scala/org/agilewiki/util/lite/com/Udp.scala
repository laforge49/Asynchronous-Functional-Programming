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

case class ServerName(name: String)

object LocalServerName {
  def apply(systemContext: SystemContext) = Udp(systemContext).localServerName
}

object UdpFactory {
  val LOCAL_RESPONDER_FACTORY_NAME = FactoryName("localResponder")

  def apply(systemContext: SystemContext) =
    systemContext.factory(classOf[UdpFactory].asInstanceOf[Class[SystemComponentFactory]])
      .asInstanceOf[UdpFactory]
}

class UdpFactory
  extends SystemComponentFactory {
  var localServerName = "Master"
  val localHostName = "localHost"
  var localPort = 4444
  var timeOutMin = 200
  var timeOutInc = 10
  var timeOutMax = 1000
  var retryLimit = 5000
  var maxMessageUuidCacheSize = 1000
  var maxPayloadSize = 1000
  val servers = new java.util.TreeMap[String, HostPort]

  override def configure(systemContext: SystemContext) {
    val liteFactory = LiteFactory(systemContext)
    liteFactory.addFactory(LocalResponderFactory())
  }

  override def instantiate(systemContext: SystemContext) = new Udp(systemContext, this)
}

object Udp {
  def apply(systemContext: SystemContext) =
    systemContext.component(classOf[UdpFactory].asInstanceOf[Class[SystemComponentFactory]])
      .asInstanceOf[Udp]
}

class Udp(systemContext: SystemContext, udpFactory: UdpFactory)
  extends SystemComponent(systemContext) {
  val localServerName = ServerName(udpFactory.localServerName)
  val localHostPort = HostPort(udpFactory.localHostName, udpFactory.localPort)
  val maxPayloadSize = udpFactory.maxPayloadSize
  val retryLimit = udpFactory.retryLimit
  val timeOutMin = udpFactory.timeOutMin
  val timeOutInc = udpFactory.timeOutInc
  val timeOutMax = udpFactory.timeOutMax
  val maxMessageUuidCacheSize = udpFactory.maxMessageUuidCacheSize
  udpFactory.servers.put(localServerName.name, localHostPort)
  val serversActor = new ServersActor(newReactor, udpFactory.servers)
  var udpSender: UdpSender = new UdpSender(newReactor)
  private var udpListener: Option[UdpListener] = None
  private val packetRouter = new PacketRouter(newReactor)
  val remoteAccess = new RemoteAccess(newReactor, packetRouter)

  override def start {
    udpListener = Some(UdpListener(systemContext, packetRouter))
  }

  override def close {
    try {
      udpListener match {
        case Some(lsr) => lsr.stopUdp
        case None =>
      }
      udpSender.stopUdp
    } catch {
      case ex: Throwable => {}
    }
  }
}
