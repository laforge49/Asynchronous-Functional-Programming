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
    case req: RegisterActorReq => registerActor(req)(back)
    case req: UnregisterActorReq => unregisterActor(req)(back)
    case req: GetActorReq => getActor(req)(back)
  }

  private def registerActor(req: RegisterActorReq)
                           (responseProcess: PartialFunction[Any, Unit])
                           (implicit src: ActiveActor) {
    val _id = req.actor.id.value
    if (actors.containsKey(_id)) responseProcess(DuplicateActorIdRsp())
    else {
      actors.put(_id, req.actor)
      responseProcess(RegisteredActorRsp())
    }
  }

  private def unregisterActor(req: UnregisterActorReq)
                             (responseProcess: PartialFunction[Any, Unit])
                             (implicit src: ActiveActor) {
    val _id = req.id.value
    actors.remove(_id)
    responseProcess(UnregisteredActorRsp())
  }

  private def getActor(req: GetActorReq)
                      (responseProcess: PartialFunction[Any, Unit])
                      (implicit src: ActiveActor) {
    val _id = req.id.value
    responseProcess(
      if (actors.containsKey(_id)) ActorRsp(actors.get(_id))
      else NoActorRsp()
    )
  }
}
