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

import services.FactoryRegistryComponentFactory

abstract class Factory(_id: FactoryId) {

  def id = _id

  private val componentFactories =
    new java.util.LinkedHashMap[Class[_ <: ComponentFactory], ComponentFactory]

  protected def instantiate = new Actor

  def newActor(mailbox: Mailbox) = {
    val actor = instantiate
    actor.setExchangeMessenger(mailbox)
    actor.setFactory(this)
    val fit = componentFactories.keySet.iterator
    while (fit.hasNext) {
      val componentFactoryClass = fit.next
      val componentFactory = componentFactories.get(componentFactoryClass)
      val component = componentFactory.newComponent(actor)
      addComponent(component)
    }
    actor
  }

  def addComponent(component: Component) {
    val actor = component.actor
    if (actor.opened) throw new IllegalStateException("already opened")
    val actorMsgFunctions = actor.messageLogics
    val componentMsgFunctions = component.messageLogics
    var it = componentMsgFunctions.keySet.iterator
    while (it.hasNext) {
      val k = it.next
      if (actorMsgFunctions.containsKey(k)) {
        throw new IllegalArgumentException("bind conflict on bindActor " +
          getClass.getName +
          "message " +
          k.getName)
      }
      val v = componentMsgFunctions.get(k)
      actorMsgFunctions.put(k, v)
    }
    val componentFactory = component.componentFactory
    if (componentFactory == null) return
    val componentFactoryClass = componentFactory.getClass.asInstanceOf[Class[ComponentFactory]]
    if (actor.components.containsKey(componentFactoryClass))
      throw new IllegalArgumentException("Duplicate component: " + componentFactoryClass.getName)
    actor.components.put(componentFactoryClass, component)
  }

  protected def include(componentFactory: ComponentFactory) {
    include(componentFactory, new scala.collection.immutable.HashSet)
  }

  private def include(componentFactory: ComponentFactory,
                      _dependent: scala.collection.immutable.Set[Class[_ <: ComponentFactory]]) {
    val componentFactoryClass = componentFactory.getClass.asInstanceOf[Class[_ <: ComponentFactory]]
    val dependent = _dependent + (componentFactoryClass)
    val requiredComponentFactoryClasses = componentFactory.requiredComponentFactoryClasses
    var i = 0
    while (i < requiredComponentFactoryClasses.size) {
      val requiredFactoryClass = requiredComponentFactoryClasses.get(i)
      if (dependent.contains(requiredFactoryClass)) throw new IllegalArgumentException("circular dependency")
      if (!componentFactories.containsKey(requiredFactoryClass)) {
        val requiredFactory = requiredFactoryClass.newInstance
        include(requiredFactory, dependent)
      }
      i += 1
    }
    componentFactories.put(componentFactoryClass, componentFactory)
    componentFactory.configure(this)
  }

  def componentFactory(componentFactoryClass: Class[_ <: ComponentFactory]) =
    componentFactories.get(componentFactoryClass)

  def configure(systemServices: SystemServices) {
    val systemServicesFactory = systemServices.factory
    val cf = systemServicesFactory.componentFactory(classOf[FactoryRegistryComponentFactory]).
      asInstanceOf[FactoryRegistryComponentFactory]
    configure(systemServices, cf)
  }

  def configure(systemServices: SystemServices,
                factoryRegistryComponentFactory: FactoryRegistryComponentFactory) {}
}
