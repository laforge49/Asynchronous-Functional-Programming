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
import annotation.tailrec

class Properties extends java.util.TreeMap[String, String]

object SetProperties {
  def apply(systemServicesFactory: Factory, properties: Properties) {
    val propertiesComponentFactory =
      systemServicesFactory.componentFactory(classOf[PropertiesComponentFactory]).
        asInstanceOf[PropertiesComponentFactory]
    if (properties != null && propertiesComponentFactory == null) throw new UnsupportedOperationException(
      "PropertiesComponentFactory is missing"
    )
    if (propertiesComponentFactory != null) propertiesComponentFactory.properties = {
      if (properties == null) new Properties
      else properties
    }
  }
}

object GetProperty {
  @tailrec def apply(name: String)(implicit activeActor: ActiveActor): String = {
    val bindActor = activeActor.bindActor
    val systemServices = bindActor.asInstanceOf[Actor].systemServices
    if (systemServices == null) return null
    val factory = systemServices.factory
    val propertiesComponentFactory = factory.componentFactory(classOf[PropertiesComponentFactory]).
      asInstanceOf[PropertiesComponentFactory]
    if (propertiesComponentFactory != null) {
      val properties = propertiesComponentFactory.properties
      if (properties.containsKey(name)) return properties.get(name)
    }
    val superior = systemServices.superior
    if (superior == null) return null
    apply(name)(superior.activeActor)
  }

  def required(name: String)(implicit activeActor: ActiveActor): String = {
    val p = apply(name)(activeActor)
    if (p == null) throw new IllegalArgumentException("No such property: " + name)
    p
  }

  def boolean(name: String, default: Boolean = false)(implicit activeActor: ActiveActor) = {
    val p = apply(name)(activeActor)
    if (p != null) p.toBoolean
    else default
  }

  def int(name: String, default: Int = 0)(implicit activeActor: ActiveActor) = {
    val p = apply(name)(activeActor)
    if (p != null) p.toInt
    else default
  }

  def string(name: String, default: String = "")(implicit activeActor: ActiveActor) = {
    val p = apply(name)(activeActor)
    if (p != null) p
    else default
  }
}

class PropertiesComponentFactory extends ComponentFactory {
  var properties = new Properties

  override def instantiate(actor: Actor) = new PropertiesComponent(actor)
}

class PropertiesComponent(actor: Actor)
  extends Component(actor) {

  override def setComponentFactory(componentFactory: ComponentFactory) {
    super.setComponentFactory(componentFactory)
    val cf = componentFactory.asInstanceOf[PropertiesComponentFactory]
    bindMessageLogic(classOf[PropertiesSeq], new ConcurrentData(new NavMapSeq(cf.properties)))
  }
}
