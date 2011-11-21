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
import bind._

class IncDesIntIntMapFactory(id: FactoryId)
  extends IncDesNavMapFactory[Int, IncDesInt, Int](
    id,
    INC_DES_INT_FACTORY_ID,
    INC_DES_INT_FACTORY_ID)

object IncDesIntIntMap {
  def apply(mailbox: Mailbox, systemServices: SystemServices) = {
    val f = new IncDesIntIntMapFactory(INC_DES_INT_INT_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[Int, IncDesInt, Int]]
    a.setSystemServices(systemServices)
    a
  }
}

class IncDesLongIntMapFactory(id: FactoryId)
  extends IncDesNavMapFactory[Long, IncDesInt, Int](
    id,
    INC_DES_LONG_FACTORY_ID,
    INC_DES_INT_FACTORY_ID)

object IncDesLongIntMap {
  def apply(mailbox: Mailbox, systemServices: SystemServices) = {
    val f = new IncDesLongIntMapFactory(INC_DES_LONG_INT_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[Long, IncDesInt, Int]]
    a.setSystemServices(systemServices)
    a
  }
}

class IncDesStringIntMapFactory(id: FactoryId)
  extends IncDesNavMapFactory[String, IncDesInt, Int](
    id,
    INC_DES_STRING_FACTORY_ID,
    INC_DES_INT_FACTORY_ID)

object IncDesStringIntMap {
  def apply(mailbox: Mailbox, systemServices: SystemServices) = {
    val f = new IncDesStringIntMapFactory(INC_DES_STRING_INT_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[String, IncDesInt, Int]]
    a.setSystemServices(systemServices)
    a
  }
}

class IncDesIntLongMapFactory(id: FactoryId)
  extends IncDesNavMapFactory[Int, IncDesLong, Long](
    id,
    INC_DES_INT_FACTORY_ID,
    INC_DES_LONG_FACTORY_ID)

object IncDesIntLongMap {
  def apply(mailbox: Mailbox, systemServices: SystemServices) = {
    val f = new IncDesIntLongMapFactory(INC_DES_INT_LONG_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[Int, IncDesLong, Long]]
    a.setSystemServices(systemServices)
    a
  }
}

class IncDesLongLongMapFactory(id: FactoryId)
  extends IncDesNavMapFactory[Long, IncDesLong, Long](
    id,
    INC_DES_LONG_FACTORY_ID,
    INC_DES_LONG_FACTORY_ID)

object IncDesLongLongMap {
  def apply(mailbox: Mailbox, systemServices: SystemServices) = {
    val f = new IncDesLongLongMapFactory(INC_DES_LONG_LONG_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[Long, IncDesLong, Long]]
    a.setSystemServices(systemServices)
    a
  }
}

class IncDesStringLongMapFactory(id: FactoryId)
  extends IncDesNavMapFactory[String, IncDesLong, Long](
    id,
    INC_DES_STRING_FACTORY_ID,
    INC_DES_LONG_FACTORY_ID)

object IncDesStringLongMap {
  def apply(mailbox: Mailbox, systemServices: SystemServices) = {
    val f = new IncDesStringLongMapFactory(INC_DES_STRING_LONG_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[String, IncDesLong, Long]]
    a.setSystemServices(systemServices)
    a
  }
}

class IncDesIntStringMapFactory(id: FactoryId)
  extends IncDesNavMapFactory[Int, IncDesString, String](
    id,
    INC_DES_INT_FACTORY_ID,
    INC_DES_STRING_FACTORY_ID)

object IncDesIntStringMap {
  def apply(mailbox: Mailbox, systemServices: SystemServices) = {
    val f = new IncDesIntStringMapFactory(INC_DES_INT_STRING_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[Int, IncDesString, String]]
    a.setSystemServices(systemServices)
    a
  }
}

class IncDesLongStringMapFactory(id: FactoryId)
  extends IncDesNavMapFactory[Long, IncDesString, String](
    id,
    INC_DES_LONG_FACTORY_ID,
    INC_DES_STRING_FACTORY_ID)

object IncDesLongStringMap {
  def apply(mailbox: Mailbox, systemServices: SystemServices) = {
    val f = new IncDesLongStringMapFactory(INC_DES_LONG_STRING_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[Long, IncDesString, String]]
    a.setSystemServices(systemServices)
    a
  }
}

class IncDesStringStringMapFactory(id: FactoryId)
  extends IncDesNavMapFactory[String, IncDesString, String](
    id,
    INC_DES_STRING_FACTORY_ID,
    INC_DES_STRING_FACTORY_ID)

object IncDesStringStringMap {
  def apply(mailbox: Mailbox, systemServices: SystemServices) = {
    val f = new IncDesStringStringMapFactory(INC_DES_STRING_STRING_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[String, IncDesString, String]]
    a.setSystemServices(systemServices)
    a
  }
}

class IncDesIntBooleanMapFactory(id: FactoryId)
  extends IncDesNavMapFactory[Int, IncDesBoolean, Boolean](
    id,
    INC_DES_INT_FACTORY_ID,
    INC_DES_BOOLEAN_FACTORY_ID)

object IncDesIntBooleanMap {
  def apply(mailbox: Mailbox, systemServices: SystemServices) = {
    val f = new IncDesIntBooleanMapFactory(INC_DES_INT_BOOLEAN_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[Int, IncDesBoolean, Boolean]]
    a.setSystemServices(systemServices)
    a
  }
}

class IncDesLongBooleanMapFactory(id: FactoryId)
  extends IncDesNavMapFactory[Long, IncDesBoolean, Boolean](
    id,
    INC_DES_LONG_FACTORY_ID,
    INC_DES_BOOLEAN_FACTORY_ID)

object IncDesLongBooleanMap {
  def apply(mailbox: Mailbox, systemServices: SystemServices) = {
    val f = new IncDesLongBooleanMapFactory(INC_DES_LONG_BOOLEAN_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[Long, IncDesBoolean, Boolean]]
    a.setSystemServices(systemServices)
    a
  }
}

class IncDesStringBooleanMapFactory(id: FactoryId)
  extends IncDesNavMapFactory[String, IncDesBoolean, Boolean](
    id,
    INC_DES_STRING_FACTORY_ID,
    INC_DES_BOOLEAN_FACTORY_ID)

object IncDesStringBooleanMap {
  def apply(mailbox: Mailbox, systemServices: SystemServices) = {
    val f = new IncDesStringBooleanMapFactory(INC_DES_STRING_BOOLEAN_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[String, IncDesBoolean, Boolean]]
    a.setSystemServices(systemServices)
    a
  }
}

class IncDesIntBytesMapFactory(id: FactoryId)
  extends IncDesNavMapFactory[Int, IncDesBytes, Array[Byte]](
    id,
    INC_DES_INT_FACTORY_ID,
    INC_DES_BYTES_FACTORY_ID)

object IncDesIntBytesMap {
  def apply(mailbox: Mailbox, systemServices: SystemServices) = {
    val f = new IncDesIntBytesMapFactory(INC_DES_INT_BYTES_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[Int, IncDesBytes, Array[Byte]]]
    a.setSystemServices(systemServices)
    a
  }
}

class IncDesLongBytesMapFactory(id: FactoryId)
  extends IncDesNavMapFactory[Long, IncDesBytes, Array[Byte]](
    id,
    INC_DES_LONG_FACTORY_ID,
    INC_DES_BYTES_FACTORY_ID)

object IncDesLongBytesMap {
  def apply(mailbox: Mailbox, systemServices: SystemServices) = {
    val f = new IncDesLongBytesMapFactory(INC_DES_LONG_BYTES_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[Long, IncDesBytes, Array[Byte]]]
    a.setSystemServices(systemServices)
    a
  }
}

class IncDesStringBytesMapFactory(id: FactoryId)
  extends IncDesNavMapFactory[String, IncDesBytes, Array[Byte]](
    id,
    INC_DES_STRING_FACTORY_ID,
    INC_DES_BYTES_FACTORY_ID)

object IncDesStringBytesMap {
  def apply(mailbox: Mailbox, systemServices: SystemServices) = {
    val f = new IncDesStringBytesMapFactory(INC_DES_STRING_BYTES_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[String, IncDesBytes, Array[Byte]]]
    a.setSystemServices(systemServices)
    a
  }
}

class IncDesIntIncDesMapFactory(id: FactoryId)
  extends IncDesNavMapFactory[Int, IncDesIncDes, IncDes](
    id,
    INC_DES_INT_FACTORY_ID,
    INC_DES_INCDES_FACTORY_ID)

object IncDesIntIncDesMap {
  def apply(mailbox: Mailbox, systemServices: SystemServices) = {
    val f = new IncDesIntIncDesMapFactory(INC_DES_INT_INCDES_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[Int, IncDesIncDes, IncDes]]
    a.setSystemServices(systemServices)
    a
  }
}

class IncDesLongIncDesMapFactory(id: FactoryId)
  extends IncDesNavMapFactory[Long, IncDesIncDes, IncDes](
    id,
    INC_DES_LONG_FACTORY_ID,
    INC_DES_INCDES_FACTORY_ID)

object IncDesLongIncDesMap {
  def apply(mailbox: Mailbox, systemServices: SystemServices) = {
    val f = new IncDesLongIncDesMapFactory(INC_DES_LONG_INCDES_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[Long, IncDesIncDes, IncDes]]
    a.setSystemServices(systemServices)
    a
  }
}

class IncDesStringIncDesMapFactory(id: FactoryId)
  extends IncDesNavMapFactory[String, IncDesIncDes, IncDes](
    id,
    INC_DES_STRING_FACTORY_ID,
    INC_DES_INCDES_FACTORY_ID)

object IncDesStringIncDesMap {
  def apply(mailbox: Mailbox, systemServices: SystemServices) = {
    val f = new IncDesStringIncDesMapFactory(INC_DES_STRING_INCDES_MAP_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesNavMap[String, IncDesIncDes, IncDes]]
    a.setSystemServices(systemServices)
    a
  }
}

class IncDesNavMapFactory[K, V <: IncDesItem[V1], V1](id: FactoryId, keyId: FactoryId, valueId: FactoryId)
  extends IncDesKeyedCollectionFactory(id, keyId, valueId) {
  override protected def instantiate = new IncDesNavMap[K, V, V1]
}
