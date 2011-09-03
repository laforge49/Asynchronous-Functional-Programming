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

class IncDesComponentFactory
  extends ComponentFactory {
  addDependency(classOf[FactoryRegistryComponentFactory])

  override def configure(compositeFactory: Factory) {
    val factoryRegistryComponentFactory =
      compositeFactory.componentFactory(classOf[FactoryRegistryComponentFactory]).
        asInstanceOf[FactoryRegistryComponentFactory]

    factoryRegistryComponentFactory.registerFactory(new IncDesIntFactory(INC_DES_INT_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new IncDesLongFactory(INC_DES_LONG_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new IncDesStringFactory(INC_DES_STRING_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new IncDesBooleanFactory(INC_DES_BOOLEAN_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new IncDesBytesFactory(INC_DES_BYTES_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new IncDesIncDesFactory(INC_DES_INCDES_FACTORY_ID))

    factoryRegistryComponentFactory.registerFactory(new IncDesIntListFactory(INC_DES_INT_LIST_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new IncDesLongListFactory(INC_DES_LONG_LIST_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new IncDesStringListFactory(INC_DES_STRING_LIST_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new IncDesBooleanListFactory(INC_DES_BOOLEAN_LIST_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new IncDesBytesListFactory(INC_DES_BYTES_LIST_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new IncDesIncDesListFactory(INC_DES_INCDES_LIST_FACTORY_ID))

    factoryRegistryComponentFactory.registerFactory(new IncDesIntIntMapFactory(INC_DES_INT_INT_MAP_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new IncDesIntLongMapFactory(INC_DES_INT_LONG_MAP_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new IncDesIntStringMapFactory(INC_DES_INT_STRING_MAP_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new IncDesIntBooleanMapFactory(INC_DES_INT_BOOLEAN_MAP_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new IncDesIntBytesMapFactory(INC_DES_INT_BYTES_MAP_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new IncDesIntIncDesMapFactory(INC_DES_INT_INCDES_MAP_FACTORY_ID))

    factoryRegistryComponentFactory.registerFactory(new IncDesLongIntMapFactory(INC_DES_LONG_INT_MAP_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new IncDesLongLongMapFactory(INC_DES_LONG_LONG_MAP_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new IncDesLongStringMapFactory(INC_DES_LONG_STRING_MAP_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new IncDesLongBooleanMapFactory(INC_DES_LONG_BOOLEAN_MAP_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new IncDesLongBytesMapFactory(INC_DES_LONG_BYTES_MAP_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new IncDesLongIncDesMapFactory(INC_DES_LONG_INCDES_MAP_FACTORY_ID))

    factoryRegistryComponentFactory.registerFactory(new IncDesStringIntMapFactory(INC_DES_STRING_INT_MAP_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new IncDesStringLongMapFactory(INC_DES_STRING_LONG_MAP_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new IncDesStringStringMapFactory(INC_DES_STRING_STRING_MAP_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new IncDesStringBooleanMapFactory(INC_DES_STRING_BOOLEAN_MAP_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new IncDesStringBytesMapFactory(INC_DES_STRING_BYTES_MAP_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new IncDesStringIncDesMapFactory(INC_DES_STRING_INCDES_MAP_FACTORY_ID))

    factoryRegistryComponentFactory.registerFactory(new IncDesIntSetFactory(INC_DES_INT_SET_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new IncDesLongSetFactory(INC_DES_LONG_SET_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new IncDesStringSetFactory(INC_DES_STRING_SET_FACTORY_ID))
  }
}
