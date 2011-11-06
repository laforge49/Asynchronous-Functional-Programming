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
package blip

import services._

class SystemServices extends Actor

class RootSystemServices extends SystemServices {
  override def close {
    super.close
    mailbox.mailboxFactory.close
  }
}

class Subsystem extends SystemServices with IdActor

object SystemServices {
  def apply(rootComponentFactory: ComponentFactory = null,
            factoryId: FactoryId = new FactoryId("System"),
            properties: Properties = null) = {
    val systemServicesFactory = new CompositeFactory(factoryId, rootComponentFactory, classOf[RootSystemServices])
    SetProperties(systemServicesFactory, properties)
    val mailboxFactory = new MailboxFactory
    val systemServices = systemServicesFactory.newActor(mailboxFactory.syncMailbox).asInstanceOf[RootSystemServices]
    systemServices.setSystemServices(systemServices)
    systemServices._open
    systemServices
  }
}

object Subsystem {
  def apply(systemServices: Actor,
            rootComponentFactory: ComponentFactory,
            mailbox: Mailbox = null,
            factoryId: FactoryId = new FactoryId("System"),
            properties: Properties = null,
            actorId: ActorId = null) = {
    val subSystemFactory = new CompositeFactory(factoryId, rootComponentFactory, classOf[Subsystem])
    SetProperties(subSystemFactory, properties)
    val _mailbox = if (mailbox == null) systemServices.mailbox else mailbox
    val subSystem = subSystemFactory.newActor(_mailbox).asInstanceOf[Subsystem]
    if (actorId != null) {
      subSystem.id(actorId)
    }
    subSystem.setSystemServices(subSystem)
    subSystem.setSuperior(systemServices)
    subSystem._open
    subSystem
  }
}
