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

class IncDesIntListFactory(id: FactoryId)
  extends IncDesListFactory[IncDesInt, Int](id, INC_DES_INT_FACTORY_ID)

object IncDesIntList {
  def apply(mailbox: Mailbox, systemServices: SystemServices) = {
    val f = new IncDesIntListFactory(INC_DES_INT_LIST_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesList[IncDesInt, Int]]
    a.setSystemServices(systemServices)
    a
  }
}

class IncDesLongListFactory(id: FactoryId)
  extends IncDesListFactory[IncDesLong, Long](id, INC_DES_LONG_FACTORY_ID)

object IncDesLongList {
  def apply(mailbox: Mailbox, systemServices: SystemServices) = {
    val f = new IncDesLongListFactory(INC_DES_LONG_LIST_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesList[IncDesLong, Long]]
    a.setSystemServices(systemServices)
    a
  }
}

class IncDesStringListFactory(id: FactoryId)
  extends IncDesListFactory[IncDesString, String](id, INC_DES_STRING_FACTORY_ID)

object IncDesStringList {
  def apply(mailbox: Mailbox, systemServices: SystemServices) = {
    val f = new IncDesStringListFactory(INC_DES_STRING_LIST_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesList[IncDesString, String]]
    a.setSystemServices(systemServices)
    a
  }
}

class IncDesBooleanListFactory(id: FactoryId)
  extends IncDesListFactory[IncDesBoolean, Boolean](id, INC_DES_BOOLEAN_FACTORY_ID)

object IncDesBooleanList {
  def apply(mailbox: Mailbox, systemServices: SystemServices) = {
    val f = new IncDesBooleanListFactory(INC_DES_BOOLEAN_LIST_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesList[IncDesBoolean, Boolean]]
    a.setSystemServices(systemServices)
    a
  }
}

class IncDesBytesListFactory(id: FactoryId)
  extends IncDesListFactory[IncDesBytes, Array[Byte]](id, INC_DES_BYTES_FACTORY_ID)

object IncDesBytesList {
  def apply(mailbox: Mailbox, systemServices: SystemServices) = {
    val f = new IncDesBytesListFactory(INC_DES_BYTES_LIST_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesList[IncDesBytes, Array[Byte]]]
    a.setSystemServices(systemServices)
    a
  }
}

class IncDesIncDesListFactory(id: FactoryId)
  extends IncDesListFactory[IncDesIncDes, IncDes](id, INC_DES_INCDES_FACTORY_ID)

object IncDesIncDesList {
  def apply(mailbox: Mailbox, systemServices: SystemServices) = {
    val f = new IncDesIncDesListFactory(INC_DES_INCDES_LIST_FACTORY_ID)
    f.configure(systemServices)
    val a = f.newActor(mailbox).asInstanceOf[IncDesList[IncDesIncDes, IncDes]]
    a.setSystemServices(systemServices)
    a
  }
}

class IncDesListFactory[V <: IncDesItem[V1], V1](id: FactoryId, valueId: FactoryId)
  extends IncDesValueCollectionFactory(id, valueId) {
  override protected def instantiate = new IncDesList[V, V1]
}
