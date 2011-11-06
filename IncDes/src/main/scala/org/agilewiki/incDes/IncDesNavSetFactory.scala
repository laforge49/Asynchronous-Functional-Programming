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
import services._

class IncDesIntSetFactory(id: FactoryId)
  extends IncDesNavSetFactory[Int](id, INC_DES_INT_FACTORY_ID)

object IncDesIntSet {
  def apply(mailbox: Mailbox, systemServices: SystemServices) = {
    val f = new IncDesIntSetFactory(INC_DES_INT_SET_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavSet[Int]]
    a.setSystemServices(systemServices)
    a
  }
}

class IncDesLongSetFactory(id: FactoryId)
  extends IncDesNavSetFactory[Long](id, INC_DES_LONG_FACTORY_ID)

object IncDesLongSet {
  def apply(mailbox: Mailbox, systemServices: SystemServices) = {
    val f = new IncDesLongSetFactory(INC_DES_LONG_SET_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavSet[Long]]
    a.setSystemServices(systemServices)
    a
  }
}

class IncDesStringSetFactory(id: FactoryId)
  extends IncDesNavSetFactory[String](id, INC_DES_STRING_FACTORY_ID)

object IncDesStringSet {
  def apply(mailbox: Mailbox, systemServices: SystemServices) = {
    val f = new IncDesStringSetFactory(INC_DES_STRING_SET_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavSet[String]]
    a.setSystemServices(systemServices)
    a
  }
}

class IncDesNavSetFactory[K](id: FactoryId, keyId: FactoryId)
  extends IncDesFactory(id) {
  var keyFactory: IncDesKeyFactory[K] = null

  override def configure(systemServices: SystemServices, factoryRegistryComponentFactory: FactoryRegistryComponentFactory) {
    super.configure(systemServices, factoryRegistryComponentFactory)
    keyFactory = factoryRegistryComponentFactory.getFactory(keyId).asInstanceOf[IncDesKeyFactory[K]]
  }

  override protected def instantiate = new IncDesNavSet[K]
}
