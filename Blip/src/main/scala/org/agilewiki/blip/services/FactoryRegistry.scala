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
package services

import bind._
import seq.NavMapSeq

class FactoryRegistryComponentFactory extends ComponentFactory {
  val factories = new java.util.TreeMap[String, Factory]

  def registerFactory(factory: Factory) {
    factories.put(factory.id.value, factory)
  }

  def getFactory(id: FactoryId) = {
    val factory = factories.get(id.value)
    factory
  }

  override def instantiate(actor: Actor) = new FactoryRegistryComponent(actor)
}

class SafeInstantiate(factoryRegistryComponentFactory: FactoryRegistryComponentFactory)
  extends MessageLogic {
  override def func(target: BindActor, msg: AnyRef, rf: Any => Unit)(implicit sender: ActiveActor) {
    val factoryId = msg.asInstanceOf[Instantiate].factoryId
    val factory = factoryRegistryComponentFactory.getFactory(factoryId)
    if (factory != null) {
      val mailbox = msg.asInstanceOf[Instantiate].mailbox
      val actor = factory.newActor(mailbox)
      actor.setSystemServices(target.asInstanceOf[Actor].systemServices)
      rf(actor)
      return
    }
    val superior = target.asInstanceOf[Actor].superior
    if (superior == null) throw new IllegalArgumentException("Unknown factory id: "+factoryId.value)
    superior.asInstanceOf[Actor](msg)(rf)
  }
}

class FactoryRegistryComponent(actor: Actor)
  extends Component(actor) {

  override def setComponentFactory(componentFactory: ComponentFactory) {
    super.setComponentFactory(componentFactory)
    val cf = componentFactory.asInstanceOf[FactoryRegistryComponentFactory]
    bindSafe(classOf[Instantiate], new SafeInstantiate(cf))
    bindSafe(classOf[Factories], new ConcurrentData(new NavMapSeq(cf.factories)))
  }

  override def open {
    val cf = componentFactory.asInstanceOf[FactoryRegistryComponentFactory]
    val factories = cf.factories
    val it = factories.keySet.iterator
    while (it.hasNext) {
      val factory = factories.get(it.next)
      factory.configure(actor.systemServices, cf)
    }
  }
}
