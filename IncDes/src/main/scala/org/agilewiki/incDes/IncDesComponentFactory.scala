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

    factoryRegistryComponentFactory.registerFactory(new SubordinateIntFactory(INC_DES_INT_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new SubordinateLongFactory(INC_DES_LONG_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new SubordinateStringFactory(INC_DES_STRING_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new SubordinateBooleanFactory(INC_DES_BOOLEAN_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new SubordinateBytesFactory(INC_DES_BYTES_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new SubordinateIncDesFactory(INC_DES_INCDES_FACTORY_ID))

    factoryRegistryComponentFactory.registerFactory(new SubordinateIntListFactory(INC_DES_INT_LIST_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new SubordinateLongListFactory(INC_DES_LONG_LIST_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new SubordinateStringListFactory(INC_DES_STRING_LIST_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new SubordinateBooleanListFactory(INC_DES_BOOLEAN_LIST_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new SubordinateBytesListFactory(INC_DES_BYTES_LIST_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new SubordinateIncDesListFactory(INC_DES_INCDES_LIST_FACTORY_ID))

    factoryRegistryComponentFactory.registerFactory(new SubordinateIntIntMapFactory(INC_DES_INT_INT_MAP_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new SubordinateIntLongMapFactory(INC_DES_INT_LONG_MAP_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new SubordinateIntStringMapFactory(INC_DES_INT_STRING_MAP_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new SubordinateIntBooleanMapFactory(INC_DES_INT_BOOLEAN_MAP_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new SubordinateIntBytesMapFactory(INC_DES_INT_BYTES_MAP_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new SubordinateIntIncDesMapFactory(INC_DES_INT_INCDES_MAP_FACTORY_ID))

    factoryRegistryComponentFactory.registerFactory(new SubordinateLongIntMapFactory(INC_DES_LONG_INT_MAP_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new SubordinateLongLongMapFactory(INC_DES_LONG_LONG_MAP_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new SubordinateLongStringMapFactory(INC_DES_LONG_STRING_MAP_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new SubordinateLongBooleanMapFactory(INC_DES_LONG_BOOLEAN_MAP_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new SubordinateLongBytesMapFactory(INC_DES_LONG_BYTES_MAP_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new SubordinateLongIncDesMapFactory(INC_DES_LONG_INCDES_MAP_FACTORY_ID))

    factoryRegistryComponentFactory.registerFactory(new SubordinateStringIntMapFactory(INC_DES_STRING_INT_MAP_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new SubordinateStringLongMapFactory(INC_DES_STRING_LONG_MAP_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new SubordinateStringStringMapFactory(INC_DES_STRING_STRING_MAP_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new SubordinateStringBooleanMapFactory(INC_DES_STRING_BOOLEAN_MAP_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new SubordinateStringBytesMapFactory(INC_DES_STRING_BYTES_MAP_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new SubordinateStringIncDesMapFactory(INC_DES_STRING_INCDES_MAP_FACTORY_ID))

    factoryRegistryComponentFactory.registerFactory(new SubordinateIntListFactory(INC_DES_INT_SET_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new SubordinateLongListFactory(INC_DES_LONG_SET_FACTORY_ID))
    factoryRegistryComponentFactory.registerFactory(new SubordinateStringListFactory(INC_DES_STRING_SET_FACTORY_ID))
  }
}
