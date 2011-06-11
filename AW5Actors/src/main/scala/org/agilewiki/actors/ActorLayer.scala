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
package actors

import batch.TimestampManager
import util._
import com.ark.SystemArkComponent
import com.lng.SystemLongProtocolComponent
import com.shrt.{ShortProtocol, SystemShortProtocolComponent}
import com.udp.SystemUdpComponent
import com.DataOutputStack
import util.actors._
import com.ark.actor.{StartArkServices, ArksReq}
import nonblocking.SystemNonBlockingComponent
import util.locks._
import util.actors.res._
import java.util.Properties
import kernel.operation.Role
import java.net.{DatagramPacket, InetAddress, DatagramSocket}
import kernel.element.operation.SystemElementsComponent
import core.SystemCoreComponent
import kernel.{KernelNames, SystemKernelComponent, Kernel}

class _ActorLayer(configurationProperties: Properties)
  extends SystemComposite
  with SystemShutdownComponent
  with SystemConfigurationComponent
  with SystemElementsComponent
  with SystemKernelComponent
  with SystemCoreComponent
  with Loggable
  with SystemActorsComponent
  with SystemNonBlockingComponent
  with SystemLocksComponent
  with SystemUdpComponent
  with SystemShortProtocolComponent
  with SystemLongProtocolComponent
  with SystemArkComponent
  with SystemActorLayerComponent
  with SystemServersComponent {
  setProperties(configurationProperties)
  kernel.start

  udp.start
  actorLayer.start
  logger info ("[SERVER:" + configuration.localServerName + "] STARTED")

  override def close {
    actorLayer.close
    kernel.close
    logger info ("[SERVER:" + configuration.localServerName + "] STOPPED")
  }

  override def initializeServer {
    core._initializeDb
  }
}

object ActorLayer {
  def apply(context: SystemComposite) = context.asInstanceOf[SystemActorLayerComponent].actorLayer
}

trait SystemActorLayerComponent {
  this: SystemConfigurationComponent
    with SystemElementsComponent
    with SystemKernelComponent
    with SystemCoreComponent
    with SystemActorsComponent
    with SystemNonBlockingComponent
    with SystemUdpComponent
    with SystemShortProtocolComponent
    with SystemLongProtocolComponent
    with SystemServersComponent
    with SystemArkComponent
    with SystemComposite =>

  protected lazy val _actorLayer = defineActorLayer

  protected def defineActorLayer = new ActorLayer

  def actorLayer = _actorLayer

  class ActorLayer {
    lazy val timestampManager = {
      val configuration = Configuration(SystemActorLayerComponent.this)
      if (configuration.localServerName != "Master")
        throw new UnsupportedOperationException("timestamp manager only available on Master")
      new TimestampManager
    }
    var arks = servers.map
    lazy val arkNames = arks.keySet

    def start {
      ark.actor ! ArksReq(shortProtocol.actor)
      ark.actor ! StartArkServices()
    }

    def close {
      val data = DataOutputStack()
      data writeUTF "closeUdp" //Command name
      data writeUTF Uuid("ARK").toString //Destination Actor Resource name
      data writeUTF "Manual Shutdown Message" //Message UUID
      data writeUTF Uuid(ShortProtocol.SHORT_ACTOR).toString //Reply Actor UUID
      data writeUTF "shutdown" //Request UUID
      data writeUTF "0" //Message type (request)
      data writeUTF configuration.localServerName //Destination Ark Name
      data writeUTF "Shutdowner" //Source Ark Name
      val buffer = data.getBytes
      val packet = new DatagramPacket(
        buffer,
        buffer.length,
        InetAddress.getByName(udp.host),
        udp.port)
      val socket = new DatagramSocket
      socket send packet
      udp.close
    }

    def singletonRolonName(roleName: String) = {
      RolonName(roleName + "_" + roleName)
    }

    /**
     * This method must be overridden by ark managers
     */
    def getRepositoryArkNameOfNew(roleName: String) = {
      val role = Kernel(SystemActorLayerComponent.this).role(roleName)
      if (role.getArkManager == configuration.localServerName) configuration.localServerName
      else throw new IllegalArgumentException("Ark manager of role '" +
        roleName + "' is '" + role.getArkManager + "', not '" + configuration.localServerName + "'")
    }

    private def childrenRolesOf(roleName: String): Set[Role] = {
      var childrenRoles = Set[Role]()
      if (!kernel.roles.contains(roleName)) return childrenRoles
      val parent = kernel.roles.role(roleName)
      val childRole = parent.property("childRole")
      if (childRole != null) {
        val it = kernel.roles.roleIterator
        while (it.hasNext) {
          val role = it.next
          if (role.property("parentRole") != null) {
            val pRole = role.property("parentRole").asInstanceOf[String]
            if (parent.isA(pRole) && role.isA(childRole.asInstanceOf[String])) {
              childrenRoles += role
            }
          }
        }
      }
      childrenRoles
    }

    private def childrenArkManagersOf(roleName: String) = {
      var arkMgrs = Set[String]()
      for (role <- childrenRolesOf(roleName)) arkMgrs ++= role.getArkManagers
      arkMgrs
    }

    def childrenArksOf(roleName: String) = {
      var arkMgrs = childrenArkManagersOf(roleName)
      arkNames.filter(ark => arkMgrs.find(mgr => ark startsWith mgr) != None)
    }

    private def qualifiedRolesOf(qualifierRoleName: String): Set[Role] = {
      var qualifiedRoles = Set[Role]()
      if (!kernel.roles.contains(qualifierRoleName)) return qualifiedRoles
      val qualifierRole = kernel.roles.role(qualifierRoleName)
      val qualifiedRoleName = qualifierRole.property("qualifiedType") match {
        case null => KernelNames.PAGE_TYPE
        case x => String valueOf x
      }
      val it1 = kernel.roles.roleIterator
      while (it1.hasNext) {
        val r = it1.next
        if (r.isA(qualifiedRoleName)) qualifiedRoles += r
      }
      qualifiedRoles
    }

    private def qualifiedArkManagersOf(qualifierRoleName: String) = {
      var arkMgrs = Set[String]()
      var roles = qualifiedRolesOf(qualifierRoleName)
      roles.foreach(x => arkMgrs ++= x.getArkManagers)
      arkMgrs
    }

    def qualifiedArksOf(qualifierRoleName: String) = {
      var arkMgrs = qualifiedArkManagersOf(qualifierRoleName)
      arkNames.filter(ark => arkMgrs.find(mgr => ark startsWith mgr) != None)
    }

    def allSecondaryArksOf(roleName: String) = {
      childrenArksOf(roleName) ++
        qualifiedArksOf(roleName)
    }
  }

}