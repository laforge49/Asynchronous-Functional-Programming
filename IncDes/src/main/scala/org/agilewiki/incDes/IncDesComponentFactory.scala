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
    factoryRegistryComponentFactory.registerFactory(SubordinateLongFactory)
    factoryRegistryComponentFactory.registerFactory(SubordinateStringFactory)
    factoryRegistryComponentFactory.registerFactory(SubordinateBooleanFactory)
    factoryRegistryComponentFactory.registerFactory(SubordinateBytesFactory)
    factoryRegistryComponentFactory.registerFactory(SubordinateIncDesFactory)

    factoryRegistryComponentFactory.registerFactory(SubordinateIntListFactory)
    factoryRegistryComponentFactory.registerFactory(SubordinateLongListFactory)
    factoryRegistryComponentFactory.registerFactory(SubordinateStringListFactory)
    factoryRegistryComponentFactory.registerFactory(SubordinateBooleanListFactory)
    factoryRegistryComponentFactory.registerFactory(SubordinateBytesListFactory)
    factoryRegistryComponentFactory.registerFactory(SubordinateIncDesListFactory)

    factoryRegistryComponentFactory.registerFactory(SubordinateIntIntMapFactory)
    factoryRegistryComponentFactory.registerFactory(SubordinateIntLongMapFactory)
    factoryRegistryComponentFactory.registerFactory(SubordinateIntStringMapFactory)
    factoryRegistryComponentFactory.registerFactory(SubordinateIntBooleanMapFactory)
    factoryRegistryComponentFactory.registerFactory(SubordinateIntBytesMapFactory)
    factoryRegistryComponentFactory.registerFactory(SubordinateIntIncDesMapFactory)

    factoryRegistryComponentFactory.registerFactory(SubordinateLongIntMapFactory)
    factoryRegistryComponentFactory.registerFactory(SubordinateLongLongMapFactory)
    factoryRegistryComponentFactory.registerFactory(SubordinateLongStringMapFactory)
    factoryRegistryComponentFactory.registerFactory(SubordinateLongBooleanMapFactory)
    factoryRegistryComponentFactory.registerFactory(SubordinateLongBytesMapFactory)
    factoryRegistryComponentFactory.registerFactory(SubordinateLongIncDesMapFactory)

    factoryRegistryComponentFactory.registerFactory(SubordinateStringIntMapFactory)
    factoryRegistryComponentFactory.registerFactory(SubordinateStringLongMapFactory)
    factoryRegistryComponentFactory.registerFactory(SubordinateStringStringMapFactory)
    factoryRegistryComponentFactory.registerFactory(SubordinateStringBooleanMapFactory)
    factoryRegistryComponentFactory.registerFactory(SubordinateStringBytesMapFactory)
    factoryRegistryComponentFactory.registerFactory(SubordinateStringIncDesMapFactory)
  }
}
