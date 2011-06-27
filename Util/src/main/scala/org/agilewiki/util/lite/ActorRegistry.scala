/*
 * Copyright 2010 Alex K.
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
package lite

import seq.LiteNavigableMapSeq

case class RegisterActorReq(actor: LiteActor)

sealed abstract class RegisterActorRsp

case class RegisteredActorRsp()
  extends RegisterActorRsp

case class DuplicateActorIdRsp()
  extends RegisterActorRsp

case class UnregisterActorReq(id: ActorId)

case class UnregisteredActorRsp()

case class GetActorReq(id: ActorId)

sealed abstract class GetActorRsp

case class ActorRsp(actor: LiteActor)
  extends GetActorRsp

case class NoActorRsp()
  extends GetActorRsp

class ActorRegistry(reactor: LiteReactor) extends LiteActor(reactor, null) {
  private var actors = new java.util.TreeMap[String, LiteActor]

  def actorSequence = new LiteNavigableMapSeq(reactor, actors)

  def registerActor(srcActor: LiteActor, actor: LiteActor)
              (pf: PartialFunction[Any, Unit]) = {
    if (isSafe(srcActor)) pf(_registerActor(actor))
    else srcActor.send(this, RegisterActorReq(actor))(pf)
  }

  def unregisterActor(srcActor: LiteActor, id: ActorId)
                (pf: PartialFunction[Any, Unit]) {
    if (isSafe(srcActor)) pf(_unregisterActor(id))
    else srcActor.send(this, UnregisterActorReq(id))(pf)
  }

  def getActor(srcActor: LiteActor, id: ActorId)
              (pf: PartialFunction[Any, Unit]) {
    if (isSafe(srcActor)) pf(_getActor(id))
    else srcActor.send(this, GetActorReq(id))(pf)
  }

  private def _registerActor(actor: LiteActor): RegisterActorRsp = {
    val _id = actor.id.value
    if (actors.containsKey(_id)) return DuplicateActorIdRsp()
    else {
      actors.put(_id, actor)
      return RegisteredActorRsp()
    }
  }

  private def _unregisterActor(id: ActorId): UnregisteredActorRsp = {
    val _id = id.value
    actors.remove(_id)
    UnregisteredActorRsp()
  }

  private def _getActor(id: ActorId): GetActorRsp = {
    val _id = id.value
    if (actors.containsKey(_id)) return ActorRsp(actors.get(_id))
    else return NoActorRsp()
  }

  addRequestHandler{
    case req: RegisterActorReq => reply(_registerActor(req.actor))
    case req: UnregisterActorReq => reply(_unregisterActor(req.id))
    case req: GetActorReq => reply(_getActor(req.id))
  }
}
