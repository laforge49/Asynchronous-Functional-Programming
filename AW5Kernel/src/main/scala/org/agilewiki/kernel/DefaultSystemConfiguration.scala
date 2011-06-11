/*
 * Copyright 2010  Bill La Forge
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
package kernel

import operation.Config
import java.util.Properties
import util.jit.Jits
import util.{UtilNames, Configuration}
import util.actors.nonblocking.NonBlocking

object DefaultSystemConfiguration {
  def apply(properties: Properties, dbName: String, serverName: String) {

    Configuration.defaultConfiguration(properties, serverName)
    Jits.defaultConfiguration(properties)
    NonBlocking.defaultConfiguration(properties)

    properties.put(MAX_ROOT_BLOCK_SIZE_PARAMETER, "" + 100000)
    properties.put(DATABASE_ACCESS_RELATIONSHIP_MODE, "rw")
    properties.put(MAX_BTREE_ROOT_SIZE_PARAMETER, "" + 1000)
    properties.put(MAX_BTREE_LEAF_SIZE_PARAMETER, "" + 10000)
    properties.put(MAX_BTREE_INODE_SIZE_PARAMETER, "" + 10000)
    properties.put(MAX_KERNEL_INODE_CACHE_SIZE_PARAMETER, "" + 100)
    properties.put(MAX_QUERY_NAME_CACHE_SIZE_PARAMETER, "" + 100)
    properties.put(MAX_QUERY_CACHE_SIZE_PARAMETER, "" + 100)
    properties.put(MAX_COPY_CACHE_SIZE_PARAMETER, "" + 100)
    properties.put(DATABASE_PATHNAME, dbName)

    var config = new Config(properties)
    config.arkManager(UtilNames.PAGE_TYPE, "Master")
    config.arkManager(KernelNames.CHANGE_TYPE, "Master")
    config.arkManager(KernelNames.HOME_TYPE, "Master")

  }

}
