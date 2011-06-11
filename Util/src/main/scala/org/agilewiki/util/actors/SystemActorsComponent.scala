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
package actors

import java.lang.reflect.Constructor
import res._
import java.util.{Properties, UUID}

object _Actors {
  def defaultConfiguration(serverName: String) = {
    val properties = new Properties
    Configuration.defaultConfiguration(properties, serverName)
    properties
  }
}

class _Actors(configurationProperties: Properties)
        extends SystemComposite
                with SystemConfigurationComponent
                with SystemActorsComponent {
  setProperties(configurationProperties)

  def close {}
}

object Actors {
  def apply(context: SystemComposite) = context.asInstanceOf[SystemActorsComponent].actors
}

trait SystemActorsComponent {
  this: SystemConfigurationComponent
          with SystemComposite =>

  protected lazy val _orgAgileWikiUtilActorsActors = defineActors

  protected def defineActors = new Actors

  def actors = _orgAgileWikiUtilActorsActors

  class Actors {
    val actorsCanonicalMap = new ActorsCanonicalMap

    //Map of actors that should not be garbage-collected
    private var longLivingActors = Map.empty[String,InternalAddressActor]

    private def remember(uuid: String, actor: InternalAddressActor){
      synchronized {
        if(actor != null) longLivingActors += (uuid -> actor)
        else if(longLivingActors.contains(uuid)) longLivingActors -= uuid
      }
    }

    def remember(actor: FullInternalAddressActor){
      remember(actor.getUuid, actor)
    }

    def forget(actor: FullInternalAddressActor){
      remember(actor.getUuid, null)
    }

    def canonicalActorFromUuid(uuid: String) = {
      synchronized {
        actorsCanonicalMap.get(uuid)
      }
    }

    def actorFromClassName(className: String): InternalAddressActor =
      actorFromClassName(className, UUID.randomUUID.toString)

    def actorFromClassName(className: ClassName): InternalAddressActor =
      actorFromClassName(className.value, UUID.randomUUID.toString)

    def actorFromClassName(className: ClassName, uuid: String): InternalAddressActor =
      actorFromClassName(className.value, uuid)

    def actorFromClassName(className: String, uuid: String): InternalAddressActor = {
      synchronized {
        var a: InternalAddressActor = null
        try {
          val c = ClassLoader.getSystemClassLoader.
                  loadClass(className).asInstanceOf[Class[InternalAddressActor]].getConstructors.find(c => {
            c.getGenericParameterTypes.size == 2 &&
                    c.getGenericParameterTypes.apply(1).isInstanceOf[Class[String]]
          }).get.asInstanceOf[Constructor[InternalAddressActor]]
          a = c.newInstance(SystemActorsComponent.this, uuid)
          actors.actorsCanonicalMap.put(uuid, a)
        } catch {
          case ex: Exception => {
            System.err.println(">class name = " + className)
            ex.printStackTrace
            throw new IllegalStateException(ex)
          }
        }
        val rv = actors.actorsCanonicalMap get uuid
        if (rv == null) {
          println("agent canonical map error")
          println("uuid = " + uuid)
          println("agent = " + a)
          println("map size = " + actors.actorsCanonicalMap.hashMap.size)
        }
        rv
      }
    }

    def actorException(actor: InternalAddressActor, exception: Throwable) {
      actor.error("Uncaught exception occurred", exception)
      //throw exception
      System.exit(1)
    }
  }
}