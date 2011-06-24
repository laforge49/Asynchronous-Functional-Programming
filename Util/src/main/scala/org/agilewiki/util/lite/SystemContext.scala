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

trait SystemContextGetter {
  def systemContext: SystemContext
}

case class SystemContextHolder(systemContext: SystemContext)
  extends SystemContextGetter

case class SystemComponentName(name: String)

abstract class SystemComponentFactory {
  val requiredFactoryClasses = new java.util.ArrayList[Class[SystemComponentFactory]]

  def configure(systemContext: SystemContext) {}

  def instantiate(systemContext: SystemContext): SystemComponent
}

abstract class SystemComponent(systemContext: SystemContext) extends SystemContextHolder(systemContext) {
  def start {}

  def close {}

  def newReactor = systemContext.newReactor
}

class SystemContext(rootFactory: SystemComponentFactory) {
  private val componentFactories =
    new java.util.LinkedHashMap[Class[SystemComponentFactory], SystemComponentFactory]
  private val components = new java.util.LinkedHashMap[Class[SystemComponentFactory], SystemComponent]

  include(rootFactory)
  val fit = componentFactories.keySet.iterator
  while (fit.hasNext) {
    val factoryClass = fit.next
    val factory = componentFactories.get(factoryClass)
    val component = factory.instantiate(this)
    components.put(factoryClass, component)
  }
  val componentList = new java.util.ArrayList(components.values)

  private def include(factory: SystemComponentFactory) {
    val requiredFactoryClasses = factory.requiredFactoryClasses
    var i = 0
    while (i < requiredFactoryClasses.size) {
      val requiredFactoryClass = requiredFactoryClasses.get(i)
      if (!componentFactories.containsKey(requiredFactoryClass)) {
        val requiredFactory = requiredFactoryClass.newInstance
        include(requiredFactory)
      }
      i += 1
    }
    componentFactories.put(factory.getClass.asInstanceOf[Class[SystemComponentFactory]], factory)
    factory.configure(this)
  }

  def factory(factoryClass: Class[SystemComponentFactory]) = componentFactories.get(factoryClass)

  def component(factoryClass: Class[SystemComponentFactory]) = components.get(factoryClass)

  def start {
    var i = 0
    while (i < componentList.size) {
      componentList.get(i).start
      i += 1
    }
  }

  def close {
    var i = componentList.size
    while (i > 0) {
      i -= 1
      componentList.get(i).close
    }
  }

  def newReactor = new LiteReactor(this)
}
