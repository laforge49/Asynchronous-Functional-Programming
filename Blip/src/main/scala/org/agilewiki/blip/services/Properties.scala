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
import annotation.tailrec

object SetProperties {
  def apply (systemServices: Actor, properties: java.util.TreeMap[String, String]) {
    val factory = systemServices.factory
    val propertiesComponentFactory = factory.componentFactory(classOf[PropertiesComponentFactory]).
      asInstanceOf[PropertiesComponentFactory]
    propertiesComponentFactory.properties = properties
  }
}

object GetProperty {
  @tailrec def apply(name: String)(implicit activeActor: ActiveActor): String = {
    val actor = activeActor.actor
    val systemServices = actor.systemServices
    val factory = systemServices.factory
    val propertiesComponentFactory = factory.componentFactory(classOf[PropertiesComponentFactory]).
      asInstanceOf[PropertiesComponentFactory]
    val properties = propertiesComponentFactory.properties
    val value = properties.get(name)
    if (value != null) return value
    val superior = systemServices.superior
    if (superior == null) throw new IllegalArgumentException("Unknown Property: "+name)
    apply(name)(superior.activeActor)
  }
}

class PropertiesComponentFactory extends ComponentFactory {
  var properties = new java.util.TreeMap[String, String]

  override def instantiate(actor: Actor) = new PropertiesComponent(actor)
}

class PropertiesComponent(actor: Actor)
  extends Component(actor) {

  override def setComponentFactory(componentFactory: ComponentFactory) {
    super.setComponentFactory(componentFactory)
    val cf = componentFactory.asInstanceOf[PropertiesComponentFactory]
    bindSafe(classOf[Properties], new SafeConstant(new NavMapSeq(cf.properties)))
  }
}
