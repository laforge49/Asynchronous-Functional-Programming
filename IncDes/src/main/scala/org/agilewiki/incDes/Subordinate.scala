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
package incDes

import blip._

class SubordinateFactory(id: FactoryId)
  extends IncDesFactory(id) {
  include(SubordinateComponentFactory())
}

class SubordinateNavSetFactory[K](id: FactoryId, keyId: FactoryId)
  extends IncDesNavSetFactory[K](id, keyId) {
  include(SubordinateComponentFactory())
}

class SubordinateValueCollectionFactory(id: FactoryId, valueId: FactoryId)
  extends IncDesValueCollectionFactory(id, valueId) {
  include(SubordinateComponentFactory())
}

class SubordinateKeyedCollectionFactory(id: FactoryId, keyId: FactoryId, valueId: FactoryId)
  extends IncDesKeyedCollectionFactory(id, keyId, valueId) {
  include(SubordinateComponentFactory())
}

class SubordinateComponentFactory extends ComponentFactory {
  override def instantiate(actor: Actor) = new SubordinateComponent(actor)
}

object SubordinateComponentFactory {
  val scf = new SubordinateComponentFactory

  def apply() = scf
}

class SubordinateComponent(actor: Actor) extends Component(actor) {
  bind(classOf[VisibleElement], passUp)
  bind(classOf[Writable], passUp)

  def incDes = actor.asInstanceOf[IncDes]

  def container = incDes.container

  def passUp(msg: AnyRef, rf: Any => Unit) {
    if (container == null) rf(null)
    else container(msg)(rf)
  }
}
