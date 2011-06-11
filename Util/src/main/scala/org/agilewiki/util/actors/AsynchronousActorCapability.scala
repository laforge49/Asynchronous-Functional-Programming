/*
 * Copyright 2010 M.Naji
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
package util
package actors

import msgs.ServiceRequestMsg
import res.ClassName
import scala.actors.Reactor

abstract class AsynchronousActor(context: SystemComposite, uuid: String)
  extends AsynchronousActorCapability {
  require(uuid != null && !uuid.isEmpty, "An AsynchronousActor must have a uuid")
  require(context != null, "An AsynchronousActor must have a context")

  override def localContext = context

  override def getUuid: String = uuid
}

trait AsynchronousActorCapability extends Reactor[AnyRef] with FullInternalAddressActor {
  start

  def act {
    loop{
      react{
        messageHandler
      }
    }
  }

  override def exceptionHandler = {
    case ex => Actors(localContext).actorException(this, ex)
  }

  protected def fallback: PartialFunction[AnyRef, Unit] = {
    case msg => unexpectedMsg(msg)
  }
}

class AsynchronousDataActor(context: SystemComposite, uuid: String)
  extends AsynchronousActor(context, uuid)
  with Exchange {
  private var data: AnyRef = null

  override protected def fallback: PartialFunction[AnyRef, Unit] = {
    case msg: DataRequestMsg => msg.func(context, this, data)
    case msg: AnyRef => data = msg
  }
}

object AsynchronousDataActor {
  def apply(systemContext: SystemComposite, data: AnyRef): AsynchronousDataActor = {
    val actors = Actors(systemContext)
    val actor = actors.actorFromClassName(ClassName(classOf[AsynchronousDataActor]),
      java.util.UUID.randomUUID.toString).asInstanceOf[AsynchronousDataActor]
    actor ! data
    actor
  }
}

class DataRequestMsg(requesterParameter: InternalAddress, f: (SystemComposite, AsynchronousDataActor, AnyRef) => Unit)
  extends ServiceRequestMsg(requesterParameter) {
  val func = f
}
