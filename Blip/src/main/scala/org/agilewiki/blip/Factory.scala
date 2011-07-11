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

abstract class Factory(_id: FactoryId) {
  def id = _id

  private var systemServices: SystemServices = null

  def systemServices(_systemServices: SystemServices) {
    systemServices = _systemServices
  }

  protected def instantiate(mailbox: Mailbox): Actor

  def newActor(mailbox: Mailbox) = {
    val actor = instantiate(mailbox)
    actor.systemServices(systemServices)
    actor
  }

  def addComponent(actor: Actor, component: Component) {
    val actorMsgFunctions = actor.messageFunctions
    val componentMsgFunctions = component.messageFunctions
    var it = componentMsgFunctions.keySet.iterator
    while (it.hasNext) {
      val k = it.next
      if (actorMsgFunctions.containsKey(k)) {
        throw new IllegalArgumentException("bind conflict on actor " +
          getClass.getName +
          "message " +
          k.getName)
      }
      val v = componentMsgFunctions.get(k)
      actorMsgFunctions.put(k, v)
    }
    val safeActorMsgFunctions = actor.safeMessageFunctions
    val safeComponentMsgFunctions = component.safeMessageFunctions
    var its = safeComponentMsgFunctions.keySet.iterator
    while (its.hasNext) {
      val k = its.next
      if (safeActorMsgFunctions.containsKey(k)) {
        throw new IllegalArgumentException("bindSafe conflict on actor " +
          getClass.getName +
          "message " +
          k.getName)
      }
      val v = safeComponentMsgFunctions.get(k)
      safeActorMsgFunctions.put(k, v)
    }
  }
}
