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

import seq.{SeqActor, LiteNavigableMapSeq}

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

  lazy val actorSequence: SeqActor[String, LiteActor] = new LiteNavigableMapSeq(reactor, actors)

  addRequestHandler{
    case req: RegisterActorReq => _registerActor(req.actor)(back)
    case req: UnregisterActorReq => reply(_unregisterActor(req.id))
    case req: GetActorReq => reply(_getActor(req.id))
  }

  private def _registerActor(actor: LiteActor)
                            (responseProcess: PartialFunction[Any, Unit])
                            (implicit sender: ActiveActor) {
    val _id = actor.id.value
    if (actors.containsKey(_id)) responseProcess(DuplicateActorIdRsp())
    else {
      actors.put(_id, actor)
      responseProcess(RegisteredActorRsp())
    }
  }

  def registerActor(actor: LiteActor)
                   (pf: PartialFunction[Any, Unit])
                   (implicit src: ActiveActor) {
    if (isSafe(src)) _registerActor(actor)(pf)(src)
    else send(RegisterActorReq(actor))(pf)(src)
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

  def unregisterActor(id: ActorId)
                     (pf: PartialFunction[Any, Unit])
                     (implicit srcActor: ActiveActor) {
    if (isSafe(srcActor)) pf(_unregisterActor(id))
    else send(UnregisterActorReq(id))(pf)(srcActor)
  }

  def getActor(id: ActorId)
              (pf: PartialFunction[Any, Unit])
              (implicit srcActor: ActiveActor) {
    if (isSafe(srcActor)) pf(_getActor(id))
    else send(GetActorReq(id))(pf)(srcActor)
  }
}
