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
package util
package lite


class LiteActor(reactor: LiteReactor, _factory: ActorFactory)
  extends LiteResponder
  with LiteSrc {
  private var actorId: ActorId = null
  private val _activeActor = ActiveActor(this)

  implicit def activeActor: ActiveActor = _activeActor

  override def id = actorId

  def id(_id: ActorId) {
    if (actorId != null) throw new UnsupportedOperationException
    actorId = _id
  }

  override def factory = _factory

  override def actor = this

  override def liteReactor = reactor

  override def response(msg: LiteRspMsg) {
    liteReactor.response(msg)
  }

  def send(msg: AnyRef)
          (responseProcess: PartialFunction[Any, Unit])
          (implicit activeActor: ActiveActor) {
    val senderReactor = {
      if (activeActor == null) null
      else activeActor.actor.liteReactor
    }
    if (senderReactor == null && reactor != null) throw new UnsupportedOperationException(
      "synchronous actor can only send to another synchronous actor"
    )
    val reqFunction = messageFunctions.get(msg.getClass)
    if (reqFunction == null) throw new UnsupportedOperationException(msg.getClass.getName)
    if ((reactor == null || senderReactor.eq(reactor))) {
      val rp = responseWrapper(responseProcess orElse uncaughtMsg)
      try {
        reqFunction(msg, rp)
      } catch {
        case ex: TransparentException => throw new WrappedException(ex.getMessage)
        case ex: Exception => {
          ex.printStackTrace
          rp(ErrorRsp(ex.toString))
        }
      }
    }
    else senderReactor.send(actor, msg)(responseProcess orElse uncaughtMsg)
  }

  private def responseWrapper(responseProcess: PartialFunction[Any, Unit]): PartialFunction[Any, Unit] = {
    case msg => {
      try {
        responseProcess(msg)
      } catch {
        case ex: Exception => {
          throw new TransparentException(ex.toString)
        }
      }
    }
  }

  private def uncaughtMsg: PartialFunction[Any, Unit] = {
    case data: ErrorRsp => throw new WrappedException(data.text)
    case data: AnyRef => {
      throw new IllegalArgumentException(data.getClass.getName)
    }
    case data => throw new IllegalArgumentException
  }
}

class WrappedException(text: String) extends Exception(text)

class TransparentException(text: String) extends Exception(text)
