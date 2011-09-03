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

import seq.NavMapSeq

object SetProperty {
  def apply (systemServices: Actor, name: String, value: String) {
    val factory = systemServices.factory
    val propertiesComponentFactory = factory.componentFactory(classOf[PropertiesComponentFactory]).
      asInstanceOf[PropertiesComponentFactory]
    val properties = propertiesComponentFactory.properties
    properties.put(name, value)
  }
}

class PropertiesComponentFactory extends ComponentFactory {
  val properties = new java.util.TreeMap[String, String]

  override def instantiate(actor: Actor) = new PropertiesComponent(actor)
}

class SafeProperties(properties: java.util.TreeMap[String, String])
  extends Safe {
  override def func(target: Actor, msg: AnyRef, rf: Any => Unit)(implicit sender: ActiveActor) {
    val name = msg.asInstanceOf[Property].name
    val value = properties.get(name)
    if (value != null) {
      rf(value)
      return
    }
    val superior = target.superior
    if (superior == null) throw new IllegalArgumentException("Unknown Property: "+name)
    superior(msg)(rf)
  }
}

class PropertiesComponent(actor: Actor)
  extends Component(actor) {

  override def setComponentFactory(componentFactory: ComponentFactory) {
    super.setComponentFactory(componentFactory)
    val cf = componentFactory.asInstanceOf[PropertiesComponentFactory]
    bindSafe(classOf[Property], new SafeProperties(cf.properties))
    bindSafe(classOf[Properties], new SafeConstant(new NavMapSeq(cf.properties)))
  }
}
