/*
 * Copyright 2010 Bill La Forge
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
package org.agilewiki.util
package locks

import java.util.Properties
import cache.CanonicalMap
import actors._

object _Locks {
  def defaultConfiguration(serverName: String) = {
    val properties = new Properties
    Configuration.defaultConfiguration(properties, serverName)
    Locks.defaultConfiguration(properties)
    properties
  }
}

class _Locks(configurationProperties: Properties)
        extends SystemComposite
                with SystemConfigurationComponent
                with SystemActorsComponent
                with SystemLocksComponent {
  setProperties(configurationProperties)

  def close {}
}

object Locks {
  val MAX_LOCK_ACTORS_CACHE_SIZE_PARAMETER = "orgAgileWikiUtilLocksMaxLockActorsCacheSize"

  def defaultConfiguration(properties: Properties) {
    properties.put(MAX_LOCK_ACTORS_CACHE_SIZE_PARAMETER, "" + 64)
  }

  def apply(context: SystemComposite) = context.asInstanceOf[SystemLocksComponent].locks
}

trait SystemLocksComponent {
  this: SystemComposite
          with SystemConfigurationComponent
          with SystemActorsComponent =>

  protected lazy val _orgAgileWikiUtilLocksLocks = defineLocks

  protected def defineLocks = new Locks

  def locks = _orgAgileWikiUtilLocksLocks

  class Locks {
    private var lockActors: CanonicalMap[LockActor] = new CanonicalMap[LockActor](configuration.requiredIntProperty(Locks.MAX_LOCK_ACTORS_CACHE_SIZE_PARAMETER))

    def lockActor(uuid: String) = {
      synchronized {
        var rv = lockActors.get(uuid)
        if (rv == null) {
          rv = new LockActor(SystemLocksComponent.this, uuid)
          lockActors.put(uuid, rv)
        }
        rv
      }
    }
  }
}
