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
package org.agilewiki.util
package lite
package com

import java.util.Properties

object _Udp {
  def defaultConfiguration(serverName: String, host: String, port: Int) = {
    val properties = new Properties
    Configuration.defaultConfiguration(properties, serverName)
    Udp.defaultConfiguration(
      properties = properties,
      host = host, port = port,
      timeOutMin = 200, timeOutInc = 10, timeOutMax = 1000,
      limit = 500, maxMessageUuuidCacheSize = 1000, maxPayloadSize = 1000)
    properties
  }
}

class _Udp(configurationProperties: Properties)
  extends SystemComposite
  with SystemShutdownComponent
  with SystemConfigurationComponent
  with SystemLiteComponent
  with SystemUdpComponent {
  setProperties(configurationProperties)
  udp.start

  def close {
    udp.close
  }
}

object Udp {

  val UDP_BINDING_ADDRESS_PROPERTY = "orgAgileWikiUtilComUdpUdpBindingAddress"
  val UDP_BINDING_PORT_PROPERTY = "orgAgileWikiUtilComUdpUdpBindingPort"
  val UDP_DATAGRAM_BUFFER_SIZE_PROPERTY = "orgAgileWikiUtilComUdpUdpDatagramBufferSize"

  val SHORT_ACTOR = "short"
  val SHORT_TIMEOUT_MIN_PROPERTY = "shortTimeoutMin"
  val SHORT_TIMEOUT_INC_PROPERTY = "shortTimoutInc"
  val SHORT_TIMEOUT_MAX_PROPERTY = "shortTimeoutMax"
  val SHORT_LIMIT_PROPERTY = "shortLimit"

  val MAX_PAYLOAD_SIZE_PROPERTY = "maxPayloadSize"

  val MAX_SHORT_MSG_UUID_CACHE_SIZE_PROPERTY = "maxShortMsgUuidCacheSize"

  def defaultConfiguration(properties: Properties,
                           host: String,
                           port: Int,
                           timeOutMin: Int,
                           timeOutInc: Int,
                           timeOutMax: Int,
                           limit: Int,
                           maxMessageUuuidCacheSize: Int,
                           maxPayloadSize: Int) {
    properties.put(UDP_DATAGRAM_BUFFER_SIZE_PROPERTY, "" + 102400)
    properties.put(UDP_BINDING_ADDRESS_PROPERTY, host)
    properties.put(UDP_BINDING_PORT_PROPERTY, "" + port)

    properties.put(SHORT_TIMEOUT_MIN_PROPERTY, "" + timeOutMin)
    properties.put(SHORT_TIMEOUT_INC_PROPERTY, "" + timeOutInc)
    properties.put(SHORT_TIMEOUT_MAX_PROPERTY, "" + timeOutMax)
    properties.put(SHORT_LIMIT_PROPERTY, "" + limit)
    properties.put(MAX_SHORT_MSG_UUID_CACHE_SIZE_PROPERTY, "" + maxMessageUuuidCacheSize)
    properties.put(MAX_PAYLOAD_SIZE_PROPERTY, "" + maxPayloadSize)
  }

  def addServer(properties: Properties, serverName: String, host: String, port: Int) {
    var ndx = 1

    def serverNameKey = "server." + ndx + ".name"
    def hostKey = "server." + ndx + ".host"
    def portKey = "server." + ndx + ".port"
    def nxt {
      ndx += 1
    }

    while (properties.containsKey(serverNameKey)) nxt

    properties.put(serverNameKey, serverName)
    properties.put(hostKey, host)
    properties.put(portKey, "" + port)
    nxt
  }

  def apply(context: SystemComposite) = context.asInstanceOf[SystemUdpComponent].udp
}

trait SystemUdpComponent {
  this: SystemComposite
    with SystemConfigurationComponent
    with SystemLiteComponent =>

  lazy val udp = new Udp

  class Udp {
    private val systemContext = SystemUdpComponent.this
    private var host: String = configuration.requiredProperty(Udp.UDP_BINDING_ADDRESS_PROPERTY)
    private var port: Int = configuration.requiredIntProperty(Udp.UDP_BINDING_PORT_PROPERTY)
    val hostPort = HostPort(host, port)
    val serversActor = new ServersActor(new ContextReactor(systemContext))
    var udpSender: UdpSender = new UdpSender(systemContext)
    private var udpListener: Option[UdpListener] = None
    private val packetRouter = new PacketRouter(systemContext)
    val remoteAccess = new RemoteAccess(systemContext, packetRouter)

    def start {
      udpListener = Some(UdpListener(systemContext, packetRouter))
    }

    def close {
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

}