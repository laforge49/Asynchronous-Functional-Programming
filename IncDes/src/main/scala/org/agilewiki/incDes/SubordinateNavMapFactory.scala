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

class SubordinateIntIntMapFactory(id: FactoryId)
  extends SubordinateNavMapFactory[Int, IncDesInt](
    id,
    INC_DES_INT_FACTORY_ID,
    INC_DES_INT_FACTORY_ID)

object IncDesIntIntMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val f = new SubordinateIntIntMapFactory(INC_DES_INT_INT_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[Int, IncDesInt]]
    a.setSystemServices(systemServices)
    a
  }
}

class SubordinateLongIntMapFactory(id: FactoryId)
  extends SubordinateNavMapFactory[Long, IncDesInt](
    id,
    INC_DES_LONG_FACTORY_ID,
    INC_DES_INT_FACTORY_ID)

object IncDesLongIntMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val f = new SubordinateLongIntMapFactory(INC_DES_LONG_INT_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[Long, IncDesInt]]
    a.setSystemServices(systemServices)
    a
  }
}

class SubordinateStringIntMapFactory(id: FactoryId)
  extends SubordinateNavMapFactory[String, IncDesInt](
    id,
    INC_DES_STRING_FACTORY_ID,
    INC_DES_INT_FACTORY_ID)

object IncDesStringIntMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val f = new SubordinateStringIntMapFactory(INC_DES_STRING_INT_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[String, IncDesInt]]
    a.setSystemServices(systemServices)
    a
  }
}

class SubordinateIntLongMapFactory(id: FactoryId)
  extends SubordinateNavMapFactory[Int, IncDesLong](
    id,
    INC_DES_INT_FACTORY_ID,
    INC_DES_LONG_FACTORY_ID)

object IncDesIntLongMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val f = new SubordinateIntLongMapFactory(INC_DES_INT_LONG_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[Int, IncDesLong]]
    a.setSystemServices(systemServices)
    a
  }
}

class SubordinateLongLongMapFactory(id: FactoryId)
  extends SubordinateNavMapFactory[Long, IncDesLong](
    id,
    INC_DES_LONG_FACTORY_ID,
    INC_DES_LONG_FACTORY_ID)

object IncDesLongLongMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val f = new SubordinateLongLongMapFactory(INC_DES_LONG_LONG_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[Long, IncDesLong]]
    a.setSystemServices(systemServices)
    a
  }
}

class SubordinateStringLongMapFactory(id: FactoryId)
  extends SubordinateNavMapFactory[String, IncDesLong](
    id,
    INC_DES_STRING_FACTORY_ID,
    INC_DES_LONG_FACTORY_ID)

object IncDesStringLongMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val f = new SubordinateStringLongMapFactory(INC_DES_STRING_LONG_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[String, IncDesLong]]
    a.setSystemServices(systemServices)
    a
  }
}

class SubordinateIntStringMapFactory(id: FactoryId)
  extends SubordinateNavMapFactory[Int, IncDesString](
    id,
    INC_DES_INT_FACTORY_ID,
    INC_DES_STRING_FACTORY_ID)

object IncDesIntStringMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val f = new SubordinateIntStringMapFactory(INC_DES_INT_STRING_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[Int, IncDesString]]
    a.setSystemServices(systemServices)
    a
  }
}

class SubordinateLongStringMapFactory(id: FactoryId)
  extends SubordinateNavMapFactory[Long, IncDesString](
    id,
    INC_DES_LONG_FACTORY_ID,
    INC_DES_STRING_FACTORY_ID)

object IncDesLongStringMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val f = new SubordinateLongStringMapFactory(INC_DES_LONG_STRING_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[Long, IncDesString]]
    a.setSystemServices(systemServices)
    a
  }
}

class SubordinateStringStringMapFactory(id: FactoryId)
  extends SubordinateNavMapFactory[String, IncDesString](
    id,
    INC_DES_STRING_FACTORY_ID,
    INC_DES_STRING_FACTORY_ID)

object IncDesStringStringMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val f = new SubordinateStringStringMapFactory(INC_DES_STRING_STRING_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[String, IncDesString]]
    a.setSystemServices(systemServices)
    a
  }
}

class SubordinateIntBooleanMapFactory(id: FactoryId)
  extends SubordinateNavMapFactory[Int, IncDesBoolean](
    id,
    INC_DES_INT_FACTORY_ID,
    INC_DES_BOOLEAN_FACTORY_ID)

object IncDesIntBooleanMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val f = new SubordinateIntBooleanMapFactory(INC_DES_INT_BOOLEAN_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[Int, IncDesBoolean]]
    a.setSystemServices(systemServices)
    a
  }
}

class SubordinateLongBooleanMapFactory(id: FactoryId)
  extends SubordinateNavMapFactory[Long, IncDesBoolean](
    id,
    INC_DES_LONG_FACTORY_ID,
    INC_DES_BOOLEAN_FACTORY_ID)

object IncDesLongBooleanMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val f = new SubordinateLongBooleanMapFactory(INC_DES_LONG_BOOLEAN_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[Long, IncDesBoolean]]
    a.setSystemServices(systemServices)
    a
  }
}

class SubordinateStringBooleanMapFactory(id: FactoryId)
  extends SubordinateNavMapFactory[String, IncDesBoolean](
    id,
    INC_DES_STRING_FACTORY_ID,
    INC_DES_BOOLEAN_FACTORY_ID)

object IncDesStringBooleanMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val f = new SubordinateStringBooleanMapFactory(INC_DES_STRING_BOOLEAN_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[String, IncDesBoolean]]
    a.setSystemServices(systemServices)
    a
  }
}

class SubordinateIntBytesMapFactory(id: FactoryId)
  extends SubordinateNavMapFactory[Int, IncDesBytes](
    id,
    INC_DES_INT_FACTORY_ID,
    INC_DES_BYTES_FACTORY_ID)

object IncDesIntBytesMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val f = new SubordinateIntBytesMapFactory(INC_DES_INT_BYTES_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[Int, IncDesBytes]]
    a.setSystemServices(systemServices)
    a
  }
}

class SubordinateLongBytesMapFactory(id: FactoryId)
  extends SubordinateNavMapFactory[Long, IncDesBytes](
    id,
    INC_DES_LONG_FACTORY_ID,
    INC_DES_BYTES_FACTORY_ID)

object IncDesLongBytesMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val f = new SubordinateLongBytesMapFactory(INC_DES_LONG_BYTES_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[Long, IncDesBytes]]
    a.setSystemServices(systemServices)
    a
  }
}

class SubordinateStringBytesMapFactory(id: FactoryId)
  extends SubordinateNavMapFactory[String, IncDesBytes](
    id,
    INC_DES_STRING_FACTORY_ID,
    INC_DES_BYTES_FACTORY_ID)

object IncDesStringBytesMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val f = new SubordinateStringBytesMapFactory(INC_DES_STRING_BYTES_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[String, IncDesBytes]]
    a.setSystemServices(systemServices)
    a
  }
}

class SubordinateIntIncDesMapFactory(id: FactoryId)
  extends SubordinateNavMapFactory[Int, IncDesIncDes](
    id,
    INC_DES_INT_FACTORY_ID,
    INC_DES_INCDES_FACTORY_ID)

object IncDesIntIncDesMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val f = new SubordinateIntIncDesMapFactory(INC_DES_INT_INCDES_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[Int, IncDesIncDes]]
    a.setSystemServices(systemServices)
    a
  }
}

class SubordinateLongIncDesMapFactory(id: FactoryId)
  extends SubordinateNavMapFactory[Long, IncDesIncDes](
    id,
    INC_DES_LONG_FACTORY_ID,
    INC_DES_INCDES_FACTORY_ID)

object SubordinateLongIncDesMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val f = new SubordinateLongIncDesMapFactory(INC_DES_LONG_INCDES_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[Long, IncDesIncDes]]
    a.setSystemServices(systemServices)
    a
  }
}

class SubordinateStringIncDesMapFactory(id: FactoryId)
  extends SubordinateNavMapFactory[String, IncDesIncDes](
    id,
    INC_DES_STRING_FACTORY_ID,
    INC_DES_INCDES_FACTORY_ID)

object IncDesStringIncDesMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val f = new SubordinateStringIncDesMapFactory(INC_DES_STRING_INCDES_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[String, IncDesIncDes]]
    a.setSystemServices(systemServices)
    a
  }
}

class SubordinateNavMapFactory[K, V <: IncDes](id: FactoryId, keyId: FactoryId, valueId: FactoryId)
  extends SubordinateKeyedCollectionFactory(id, keyId, valueId) {
  override protected def instantiate = new IncDesNavMap[K, V]
}
