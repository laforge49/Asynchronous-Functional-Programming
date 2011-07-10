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

class SystemServicesFactory(factoryId: FactoryId, rootSystemComponentFactory: SystemComponentFactory)
  extends Factory(factoryId) {
  private val componentFactories =
    new java.util.LinkedHashMap[Class[_ <: SystemComponentFactory], SystemComponentFactory]

  include(rootSystemComponentFactory, new scala.collection.immutable.HashSet)

  private def include(factory: SystemComponentFactory,
                      _dependent: scala.collection.immutable.Set[Class[_ <: SystemComponentFactory]]) {
    val factoryClass = factory.getClass.asInstanceOf[Class[_ <: SystemComponentFactory]]
    val dependent = _dependent + (factoryClass)
    val requiredFactoryClasses = factory.requiredFactoryClasses
    var i = 0
    while (i < requiredFactoryClasses.size) {
      val requiredFactoryClass = requiredFactoryClasses.get(i)
      if (dependent.contains(requiredFactoryClass)) throw new IllegalArgumentException("circular dependency")
      if (!componentFactories.containsKey(requiredFactoryClass)) {
        val requiredFactory = requiredFactoryClass.newInstance
        include(requiredFactory, dependent)
      }
      i += 1
    }
    componentFactories.put(factory.getClass.asInstanceOf[Class[SystemComponentFactory]], factory)
    factory.configure(this)
  }

  def componentFactory(factoryClass: Class[_ <: SystemComponentFactory]) =
    componentFactories.get(factoryClass)

  protected def instantiate(mailbox: Mailbox) = {
    val systemServices = new SystemServices(mailbox, this)
    systemServices.systemServices(systemServices)
    systemServices.singleton
    val fit = componentFactories.keySet.iterator
    while (fit.hasNext) {
      val componentFactoryClass = fit.next
      val componentFactory = componentFactories.get(componentFactoryClass)
      systemServices.addComponent(componentFactory)
    }
    systemServices
  }
}
