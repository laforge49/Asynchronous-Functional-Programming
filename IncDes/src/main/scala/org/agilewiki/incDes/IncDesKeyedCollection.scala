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

class IncDesKeyedCollectionFactory[K](id: FactoryId, keyId: FactoryId, valueId: FactoryId)
  extends IncDesValueCollectionFactory(id, valueId) {
  var keyFactory: IncDesKeyFactory[K] = null

  override def configure(systemServices: SystemServices, factoryRegistryComponentFactory: FactoryRegistryComponentFactory) {
    super.configure(systemServices, factoryRegistryComponentFactory)
    keyFactory = factoryRegistryComponentFactory.getFactory(keyId).asInstanceOf[IncDesKeyFactory[K]]
  }
}

abstract class IncDesKeyedCollection[K, V <: IncDesItem[V1], V1]
  extends IncDesValueCollection[K, V, V1] {

  bind(classOf[Put[K, V, V1]], put)
  bind(classOf[MakePut[K]], makePut)
  bind(classOf[MakePutSet[K, V1]], makePutSet)
  if (isInstanceOf[V1]) {
    bind(classOf[MakePutMakeSet[K]], makePutMakeSet)
    bind(classOf[PutInt[K]], putInt)
    bind(classOf[PutLong[K]], putLong)
    bind(classOf[PutString[K]], putString)
  }

  def keyFactory = factory.asInstanceOf[IncDesKeyedCollectionFactory[K]].keyFactory

  def put(msg: AnyRef, rf: Any => Unit)

  def makePut(msg: AnyRef, rf: Any => Unit)

  def makePutSet(msg: AnyRef, rf: Any => Unit)

  def makePutMakeSet(msg: AnyRef, rf: Any => Unit)

  def putInt(msg: AnyRef, rf: Any => Unit) {
    val req = msg.asInstanceOf[PutInt[K]]
    val tc = req.transactionContext
    makePutMakeSet(MakePutMakeSet[K](tc, req.key, INC_DES_INT_FACTORY_ID), {
      rsp => rsp.asInstanceOf[Actor](Set(tc, req.value))(rf)
    })
  }

  def putLong(msg: AnyRef, rf: Any => Unit) {
    val req = msg.asInstanceOf[PutLong[K]]
    val tc = req.transactionContext
    makePutMakeSet(MakePutMakeSet[K](tc, req.key, INC_DES_LONG_FACTORY_ID), {
      rsp => rsp.asInstanceOf[Actor](Set(tc, req.value))(rf)
    })
  }

  def putString(msg: AnyRef, rf: Any => Unit) {
    val req = msg.asInstanceOf[PutString[K]]
    val tc = req.transactionContext
    makePutMakeSet(MakePutMakeSet[K](tc, req.key, INC_DES_STRING_FACTORY_ID), {
      rsp => rsp.asInstanceOf[Actor](Set(tc, req.value))(rf)
    })
  }
}
