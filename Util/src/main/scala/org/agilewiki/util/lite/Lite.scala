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

import seq.LiteNavigableMapSeq

abstract case class ActorFactory(name: FactoryName) {
  def instantiate(reactor: LiteReactor): LiteActor
}

object LiteFactory {
  def apply(systemContext: SystemContext) =
    systemContext.factory(classOf[LiteFactory])
      .asInstanceOf[LiteFactory]
}

class LiteFactory
  extends SystemComponentFactory {
  val actorFactories = new java.util.TreeMap[String, ActorFactory]

  def registerActorFactory(factory: ActorFactory) {
    actorFactories.put(factory.name.value, factory)
  }

  def getActorFactory(name: FactoryName) = actorFactories.get(name.value)

  override def instantiate(systemContext: SystemContext) = new Lite(systemContext, this)
}

object Lite {
  def apply(systemContext: SystemContext) =
    systemContext.component(classOf[LiteFactory])
      .asInstanceOf[Lite]

  def serviceReactor(implicit systemContext: SystemContext) = apply(systemContext).serviceReactor

  def factorySequence(implicit systemContext: SystemContext) = apply(systemContext).factorySequence

  def actorRegistry(implicit systemContext: SystemContext) = apply(systemContext).actorRegistry

  def newActor(factoryName: FactoryName, reactor: LiteReactor)
              (implicit systemContext: SystemContext) =
    apply(systemContext).newActor(factoryName, reactor)

  def getActor(name: ActorName, reactor: LiteReactor)
              (pf: PartialFunction[Any, Unit])
              (implicit srcActor: ActiveActor, systemContext: SystemContext) =
    apply(systemContext).getActor(name, reactor)(pf)(srcActor)
}

class Lite(systemContext: SystemContext, liteFactory: LiteFactory)
  extends SystemComponent(systemContext) {
  val serviceReactor = newReactor

  lazy val factorySequence = new LiteNavigableMapSeq(serviceReactor, liteFactory.actorFactories)

  val actorRegistry = new ActorRegistry(serviceReactor)

  def newActor(factoryName: FactoryName, reactor: LiteReactor) = {
    val actorFactory = liteFactory.getActorFactory(factoryName)
    val actor = actorFactory.instantiate(reactor)
    actor
  }

  def getActor(name: ActorName, reactor: LiteReactor)
              (pf: PartialFunction[Any, Unit])
              (implicit srcActor: ActiveActor) {
    name match {
      case n: ActorId => actorRegistry.send(GetActorReq(n))(pf)(srcActor)
      case n: FactoryName => pf(ActorRsp(newActor(n, reactor)))
    }
  }
}
