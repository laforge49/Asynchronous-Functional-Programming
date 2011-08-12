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

object SubordinateIntIntMapFactory
  extends SubordinateNavMapFactory[Int, IncDesInt](
    INC_DES_INT_INT_MAP_FACTORY_ID,
    INC_DES_INT_FACTORY_ID,
    INC_DES_INT_FACTORY_ID)

object IncDesIntIntMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val a = SubordinateIntIntMapFactory.newActor(mailbox).asInstanceOf[IncDesNavMap[Int, IncDesInt]]
    a.setSystemServices(systemServices)
    a
  }
}

object SubordinateLongIntMapFactory
  extends SubordinateNavMapFactory[Long, IncDesInt](
    INC_DES_LONG_INT_MAP_FACTORY_ID,
    INC_DES_LONG_FACTORY_ID,
    INC_DES_INT_FACTORY_ID)

object IncDesLongIntMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val a = SubordinateLongIntMapFactory.newActor(mailbox).asInstanceOf[IncDesNavMap[Long, IncDesInt]]
    a.setSystemServices(systemServices)
    a
  }
}

object SubordinateStringIntMapFactory
  extends SubordinateNavMapFactory[String, IncDesInt](
    INC_DES_STRING_INT_MAP_FACTORY_ID,
    INC_DES_STRING_FACTORY_ID,
    INC_DES_INT_FACTORY_ID)

object IncDesStringIntMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val a = SubordinateStringIntMapFactory.newActor(mailbox).asInstanceOf[IncDesNavMap[String, IncDesInt]]
    a.setSystemServices(systemServices)
    a
  }
}

object SubordinateIntLongMapFactory
  extends SubordinateNavMapFactory[Int, IncDesLong](
    INC_DES_INT_LONG_MAP_FACTORY_ID,
    INC_DES_INT_FACTORY_ID,
    INC_DES_LONG_FACTORY_ID)

object IncDesIntLongMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val a = SubordinateIntLongMapFactory.newActor(mailbox).asInstanceOf[IncDesNavMap[Int, IncDesLong]]
    a.setSystemServices(systemServices)
    a
  }
}

object SubordinateLongLongMapFactory
  extends SubordinateNavMapFactory[Long, IncDesLong](
    INC_DES_LONG_LONG_MAP_FACTORY_ID,
    INC_DES_LONG_FACTORY_ID,
    INC_DES_LONG_FACTORY_ID)

object IncDesLongLongMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val a = SubordinateLongLongMapFactory.newActor(mailbox).asInstanceOf[IncDesNavMap[Long, IncDesLong]]
    a.setSystemServices(systemServices)
    a
  }
}

object SubordinateStringLongMapFactory
  extends SubordinateNavMapFactory[String, IncDesLong](
    INC_DES_STRING_LONG_MAP_FACTORY_ID,
    INC_DES_STRING_FACTORY_ID,
    INC_DES_LONG_FACTORY_ID)

object IncDesStringLongMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val a = SubordinateStringLongMapFactory.newActor(mailbox).asInstanceOf[IncDesNavMap[String, IncDesLong]]
    a.setSystemServices(systemServices)
    a
  }
}

object SubordinateIntStringMapFactory
  extends SubordinateNavMapFactory[Int, IncDesString](
    INC_DES_INT_STRING_MAP_FACTORY_ID,
    INC_DES_INT_FACTORY_ID,
    INC_DES_STRING_FACTORY_ID)

object IncDesIntStringMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val a = SubordinateIntStringMapFactory.newActor(mailbox).asInstanceOf[IncDesNavMap[Int, IncDesString]]
    a.setSystemServices(systemServices)
    a
  }
}

object SubordinateLongStringMapFactory
  extends SubordinateNavMapFactory[Long, IncDesString](
    INC_DES_LONG_STRING_MAP_FACTORY_ID,
    INC_DES_LONG_FACTORY_ID,
    INC_DES_STRING_FACTORY_ID)

object IncDesLongStringMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val a = SubordinateLongStringMapFactory.newActor(mailbox).asInstanceOf[IncDesNavMap[Long, IncDesString]]
    a.setSystemServices(systemServices)
    a
  }
}

object SubordinateStringStringMapFactory
  extends SubordinateNavMapFactory[String, IncDesString](
    INC_DES_STRING_STRING_MAP_FACTORY_ID,
    INC_DES_STRING_FACTORY_ID,
    INC_DES_STRING_FACTORY_ID)

object IncDesStringStringMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val a = SubordinateStringStringMapFactory.newActor(mailbox).asInstanceOf[IncDesNavMap[String, IncDesString]]
    a.setSystemServices(systemServices)
    a
  }
}

object SubordinateIntBooleanMapFactory
  extends SubordinateNavMapFactory[Int, IncDesBoolean](
    INC_DES_INT_BOOLEAN_MAP_FACTORY_ID,
    INC_DES_INT_FACTORY_ID,
    INC_DES_BOOLEAN_FACTORY_ID)

object IncDesIntBooleanMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val a = SubordinateIntBooleanMapFactory.newActor(mailbox).asInstanceOf[IncDesNavMap[Int, IncDesBoolean]]
    a.setSystemServices(systemServices)
    a
  }
}

object SubordinateLongBooleanMapFactory
  extends SubordinateNavMapFactory[Long, IncDesBoolean](
    INC_DES_LONG_BOOLEAN_MAP_FACTORY_ID,
    INC_DES_LONG_FACTORY_ID,
    INC_DES_BOOLEAN_FACTORY_ID)

object IncDesLongBooleanMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val a = SubordinateLongBooleanMapFactory.newActor(mailbox).asInstanceOf[IncDesNavMap[Long, IncDesBoolean]]
    a.setSystemServices(systemServices)
    a
  }
}

object SubordinateStringBooleanMapFactory
  extends SubordinateNavMapFactory[String, IncDesBoolean](
    INC_DES_STRING_BOOLEAN_MAP_FACTORY_ID,
    INC_DES_STRING_FACTORY_ID,
    INC_DES_BOOLEAN_FACTORY_ID)

object IncDesStringBooleanMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val a = SubordinateStringBooleanMapFactory.newActor(mailbox).asInstanceOf[IncDesNavMap[String, IncDesBoolean]]
    a.setSystemServices(systemServices)
    a
  }
}

object SubordinateIntBytesMapFactory
  extends SubordinateNavMapFactory[Int, IncDesBytes](
    INC_DES_INT_BYTES_MAP_FACTORY_ID,
    INC_DES_INT_FACTORY_ID,
    INC_DES_BYTES_FACTORY_ID)

object IncDesIntBytesMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val a = SubordinateIntBytesMapFactory.newActor(mailbox).asInstanceOf[IncDesNavMap[Int, IncDesBytes]]
    a.setSystemServices(systemServices)
    a
  }
}

object SubordinateLongBytesMapFactory
  extends SubordinateNavMapFactory[Long, IncDesBytes](
    INC_DES_LONG_BYTES_MAP_FACTORY_ID,
    INC_DES_LONG_FACTORY_ID,
    INC_DES_BYTES_FACTORY_ID)

object IncDesLongBytesMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val a = SubordinateLongBytesMapFactory.newActor(mailbox).asInstanceOf[IncDesNavMap[Long, IncDesBytes]]
    a.setSystemServices(systemServices)
    a
  }
}

object SubordinateStringBytesMapFactory
  extends SubordinateNavMapFactory[String, IncDesBytes](
    INC_DES_STRING_BYTES_MAP_FACTORY_ID,
    INC_DES_STRING_FACTORY_ID,
    INC_DES_BYTES_FACTORY_ID)

object IncDesStringBytesMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val a = SubordinateStringBytesMapFactory.newActor(mailbox).asInstanceOf[IncDesNavMap[String, IncDesBytes]]
    a.setSystemServices(systemServices)
    a
  }
}

object SubordinateIntIncDesMapFactory
  extends SubordinateNavMapFactory[Int, IncDesIncDes](
    INC_DES_INT_INCDES_MAP_FACTORY_ID,
    INC_DES_INT_FACTORY_ID,
    INC_DES_INCDES_FACTORY_ID)

object IncDesIntIncDesMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val a = SubordinateIntIncDesMapFactory.newActor(mailbox).asInstanceOf[IncDesNavMap[Int, IncDesIncDes]]
    a.setSystemServices(systemServices)
    a
  }
}

object SubordinateLongIncDesMapFactory
  extends SubordinateNavMapFactory[Long, IncDesIncDes](
    INC_DES_LONG_INCDES_MAP_FACTORY_ID,
    INC_DES_LONG_FACTORY_ID,
    INC_DES_INCDES_FACTORY_ID)

object SubordinateLongIncDesMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val a = SubordinateLongIncDesMapFactory.newActor(mailbox).asInstanceOf[IncDesNavMap[Long, IncDesIncDes]]
    a.setSystemServices(systemServices)
    a
  }
}

object SubordinateStringIncDesMapFactory
  extends SubordinateNavMapFactory[String, IncDesIncDes](
    INC_DES_STRING_INCDES_MAP_FACTORY_ID,
    INC_DES_STRING_FACTORY_ID,
    INC_DES_INCDES_FACTORY_ID)

object IncDesStringIncDesMap {
  def apply(mailbox: Mailbox, systemServices: Actor) = {
    val a = SubordinateStringIncDesMapFactory.newActor(mailbox).asInstanceOf[IncDesNavMap[String, IncDesIncDes]]
    a.setSystemServices(systemServices)
    a
  }
}

class SubordinateNavMapFactory[K, V <: IncDes](id: FactoryId, keyId: FactoryId, valueId: FactoryId)
  extends SubordinateKeyedCollectionFactory(id, keyId, valueId) {
  override protected def instantiate = new IncDesNavMap[K, V]
}
