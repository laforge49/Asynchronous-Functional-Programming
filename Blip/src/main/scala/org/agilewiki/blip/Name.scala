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
package blip

import java.util.UUID

object Name {
  def apply(s: String): Name = {
    s match {
      case res if res.startsWith("AID:") => ActorId(res)
      case res if res.startsWith("FID:") => FactoryId(res)
      case _ => throw new IllegalArgumentException("Wrong resource name format: " + s)
    }
  }
}

sealed abstract class Name {
  def value: String
}

case class ActorId(id: String) extends Name {
  override def toString = "AID:" + value

  override def value = if (id.startsWith("AID:")) id.substring(4) else id
}

object ActorId {
  def generate = ActorId(UUID.randomUUID.toString)
}

case class FactoryId(name: String) extends Name {

  override def value = if (name.startsWith("FID:")) name.substring(4) else name

  override def toString = "FID:" + value
}

trait IdActor {
  this: Actor =>
  private var actorId: ActorId = null

  def id = actorId

  def id(_id: ActorId) {
    if (actorId != null) throw new UnsupportedOperationException
    if (opened) throw new IllegalStateException
    actorId = _id
  }
}

class Id_Actor extends Actor with IdActor
