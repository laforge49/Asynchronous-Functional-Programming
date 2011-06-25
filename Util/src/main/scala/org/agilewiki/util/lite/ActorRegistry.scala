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

case class RegisterReq(actor: LiteActor)

sealed abstract class RegisterRsp

case class RegisteredRsp()
  extends RegisterRsp

case class DuplicateIdRsp()
  extends RegisterRsp

case class UnregisterReq(id: ActorId)

case class UnregisterRsp()

case class GetActorReq(id: ActorId)

sealed abstract class GetActorRsp

case class ActorRsp(actor: LiteActor)
  extends GetActorRsp

case class NoActor()
  extends GetActorRsp

class ActorRegistry(reactor: LiteReactor) extends LiteActor(reactor, null) {
  private var actors = new java.util.HashMap[String, LiteActor]

  def register(srcActor: LiteActor, actor: LiteActor)
              (pf: PartialFunction[Any, Unit]) = {
    if (isSafe(srcActor)) pf(_register(actor))
    else srcActor.send(this, RegisterReq(actor))(pf)
  }

  def unregister(srcActor: LiteActor, id: ActorId)
                (pf: PartialFunction[Any, Unit]) {
    if (isSafe(srcActor)) pf(_unregister(id))
    else srcActor.send(this, UnregisterReq(id))(pf)
  }

  def getActor(srcActor: LiteActor, id: ActorId)
              (pf: PartialFunction[Any, Unit]) {
    if (isSafe(srcActor)) pf(_getActor(id))
    else srcActor.send(this, GetActorReq(id))(pf)
  }

  private def _register(actor: LiteActor): RegisterRsp = {
    val _id = actor.id.value
    if (actors.containsKey(_id)) return DuplicateIdRsp()
    else return RegisteredRsp()
  }

  private def _unregister(id: ActorId): UnregisterRsp = {
    val _id = id.value
    actors.remove(_id)
    UnregisterRsp()
  }

  private def _getActor(id: ActorId): GetActorRsp = {
    val _id = id.value
    if (actors.containsKey(_id)) return ActorRsp(actors.get(_id))
    else return NoActor()
  }

  addRequestHandler{
    case req: RegisterReq => reply(_register(req.actor))
    case req: UnregisterReq => reply(_unregister(req.id))
    case req: GetActorReq => reply(_getActor(req.id))
  }
}
