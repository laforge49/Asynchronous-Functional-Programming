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

class SubordinateIntSetFactory(id: FactoryId)
  extends SubordinateNavSetFactory[Int](id, INC_DES_INT_FACTORY_ID)

object IncDesIntSet {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val f = new SubordinateIntSetFactory(INC_DES_INT_SET_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavSet[Int]]
    a.setSystemServices(systemServices)
    a
  }
}

class SubordinateLongSetFactory(id: FactoryId)
  extends SubordinateNavSetFactory[Int](id, INC_DES_LONG_FACTORY_ID)

object IncDesLongSet {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val f = new SubordinateIntSetFactory(INC_DES_LONG_SET_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavSet[Long]]
    a.setSystemServices(systemServices)
    a
  }
}

class SubordinateStringSetFactory(id: FactoryId)
  extends SubordinateNavSetFactory[Int](id, INC_DES_STRING_FACTORY_ID)

object IncDesStringSet {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val f = new SubordinateIntSetFactory(INC_DES_STRING_SET_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavSet[String]]
    a.setSystemServices(systemServices)
    a
  }
}

class SubordinateNavSetFactory[K](id: FactoryId, keyId: FactoryId)
  extends SubordinateCollectionFactory(id) {
  override protected def instantiate = new IncDesNavSet[K]
}
