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
package org.agilewiki
package util
package actors

abstract class SynchronousActor(context: SystemComposite, uuid: String)
        extends SynchronousActorCapability {
  require(uuid != null && !uuid.isEmpty,"A SynchronousActor must have a uuid")
  require(context != null, "A SynchronousActor must have a context")

  override def localContext = context

  override def getUuid: String = uuid
}

class AnonymousSynchronousActor(context: SystemComposite, doit: PartialFunction[AnyRef, Unit]) extends SynchronousActorCapability {

  override def localContext = context

  override def getUuid: String = {
    throw new UnsupportedOperationException
  }

  override protected def messageHandler: PartialFunction[AnyRef, Unit] = {
    doit
  }
}

trait SynchronousActorCapability extends FullInternalAddressActor {

  private var active: Boolean = false
  private var pending: AnyRef = null

  def unexpectedMsg(msg: AnyRef)

  def !(msg: AnyRef) {
    synchronized {
      if (pending != null) {
        println(this + " - Pseudo actors received asynchronous message")
        throw new IllegalStateException("Pseudo actors received asynchronous message")
      }
      if (active) {
        pending = msg
      } else {
        active = true
        pending = msg
        while (pending != null) {
          val m = pending
          pending = null
          processMessage(m)
        }
        active = false
      }
    }
  }

  protected def processMessage(msg: AnyRef) {
    messageHandler(msg)
  }

  protected def fallback: PartialFunction[AnyRef, Unit] = {
    case msg => unexpectedMsg(msg)
  }
}
