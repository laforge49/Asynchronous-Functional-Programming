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

class ActorRegistryComponentFactory extends ComponentFactory {
  override def instantiate(actor: Actor) = new ActorRegistryComponent(actor)
}

object SafeResolveName
  extends MessageLogic {
  override def func(target: BindActor, msg: AnyRef, rf: Any => Unit)(implicit sender: ActiveActor) {
    val req = msg.asInstanceOf[ResolveName]
    req.name match {
      case factoryId: FactoryId => target.asInstanceOf[Actor](Instantiate(factoryId, req.mailbox))(rf)
      case actorId: ActorId => target.asInstanceOf[Actor](GetActor(actorId))(rf)
    }
  }
}

object SafeRegister
  extends MessageLogic {
  override def func(target: BindActor, msg: AnyRef, rf: Any => Unit)(implicit sender: ActiveActor) {
    val actors = ActorRegistryComponent.actors(target)
    val req = msg.asInstanceOf[Register]
    val actor = req.actor
    val actorId = actor.id
    if (actorId == null)
      throw new IllegalArgumentException("IdActor has no id")
    val key = actorId.value
    if (actors.containsKey(key))
      throw new IllegalArgumentException("already registered: " + key)
    actors.put(key, actor)
    rf(null)
  }
}

object SafeUnregister
  extends MessageLogic {
  override def func(target: BindActor, msg: AnyRef, rf: Any => Unit)(implicit sender: ActiveActor) {
    val actors = ActorRegistryComponent.actors(target)
    val actorId = msg.asInstanceOf[Unregister].actorId
    val key = actorId.value
    if (!actors.containsKey(key))
      throw new IllegalArgumentException("not registered: " + key)
    actors.remove(key)
    rf(null)
  }
}

object SafeGetActor
  extends MessageLogic {
  override def func(target: BindActor, msg: AnyRef, rf: Any => Unit)(implicit sender: ActiveActor) {
    val actors = ActorRegistryComponent.actors(target)
    val actorId = msg.asInstanceOf[GetActor].actorId
    val key = actorId.value
    val a = actors.get(key)
    if (a != null) {
      rf(actors.get(key))
      return
    }
    if (target.superior == null) {
      rf(null)
      return
    }
    target.superior(msg)(rf)
  }
}

object ActorRegistryComponent {
  def actors(target: BindActor) = {
    val targetActor = target.asInstanceOf[Actor]
    val component = targetActor.component(classOf[ActorRegistryComponentFactory]).asInstanceOf[ActorRegistryComponent]
    component.asInstanceOf[ActorRegistryComponent].actors
  }
}

class ActorRegistryComponent(actor: Actor)
  extends Component(actor) {
  val actors = new java.util.concurrent.ConcurrentSkipListMap[String, IdActor]
  bindMessageLogic(classOf[Register], SafeRegister)
  bindMessageLogic(classOf[Unregister], SafeUnregister)
  bindMessageLogic(classOf[GetActor], SafeGetActor)
  bindMessageLogic(classOf[ResolveName], SafeResolveName)
  bindMessageLogic(classOf[Actors],
    new ConcurrentData(new NavMapSeq(actors)))

  override def open {
    actor.requiredService(classOf[Instantiate])
  }

  override def close {
    try {
      val it = actors.keySet.iterator
      while (it.hasNext) {
        val a = actors.get(it.next)
        a.asInstanceOf[Actor].close
      }
    } catch {
      case _ =>
    }
  }
}
