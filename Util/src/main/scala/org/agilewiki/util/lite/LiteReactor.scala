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

import scala.actors.Reactor

final case class LiteReactor(systemContext: SystemContext)
  extends Reactor[LiteMsg]
  with SystemContextGetter {
  private var curMsg: LiteMsg = null
  start

  def isMailboxEmpty = mailboxSize == 0

  def newReactor = new LiteReactor(systemContext)

  override def scheduler = super.scheduler

  override def act {
    loop{
      react{
        case msg: LiteReqMsg => {
          curMsg = msg
          val target = msg.target
          (target.actor.requestHandler orElse uncaughtMsg)(msg.content)
        }
        case msg: LiteRspMsg => {
          curMsg = msg
          val sender = currentRequestMessage.sender
          (msg.responseProcess orElse uncaughtMsg)(msg.content)
        }
        case msg => {
          curMsg = null
          throw new IllegalArgumentException
        }
      }
    }
  }

  override def exceptionHandler: PartialFunction[Exception, Unit] = {
    case ex: Exception => {
      ex.printStackTrace
      if (curMsg != null) {
        val curReq = currentRequestMessage
        reply(new UncaughtExceptionRsp(
          ex.toString,
          curReq.sender.getClass.getName,
          curReq.target.actor.getClass.getName))
      }
    }
  }

  def uncaughtMsg: PartialFunction[Any, Unit] = {
    case data: ErrorRsp => reply(data)
    case data: Object => {
      throw new IllegalArgumentException(data.getClass.getName)
    }
    case data => throw new IllegalArgumentException
  }

  def request(msg: LiteReqMsg) {
    this ! msg
  }

  def response(msg: LiteRspMsg) {
    this ! msg
  }

  def currentMessage = curMsg

  def currentRequestMessage = {
    if (curMsg.isInstanceOf[LiteReqMsg])
      curMsg.asInstanceOf[LiteReqMsg]
    else
      curMsg.asInstanceOf[LiteRspMsg].oldRequest
  }

  def recursionDepth = currentRequestMessage.recursionDepth

  def send(targetActor: LiteActor, content: Any)
          (responseProcess: PartialFunction[Any, Unit]) {
    val oldReq = currentRequestMessage
    val sender = oldReq.target.actor
    val targetReactor = targetActor.liteReactor
    if (recursionDepth > 100 ||
      (!eq(targetReactor))) {
      val req = new LiteReqMsg(
        0,
        ActiveActor(targetActor),
        responseProcess,
        oldReq,
        content,
        sender)
      targetReactor.request(req)
    }
    else {
      val req = new LiteReqMsg(
        recursionDepth + 1,
        ActiveActor(targetActor),
        responseProcess,
        oldReq,
        content,
        sender)
      val oldMsg = curMsg
      curMsg = req
      try {
        (targetActor.requestHandler orElse uncaughtMsg)(content)
      } catch {
        case ex: Exception => exceptionHandler(ex)
      }
      curMsg = oldMsg
    }
  }

  def reply(content: Any) {
    val req = currentRequestMessage
    if (!req.active) return
    req.active = false
    val sender = req.sender
    if (recursionDepth > 100 ||
      !sender.isInstanceOf[LiteActor] ||
      (!eq(sender.asInstanceOf[LiteActor].liteReactor))) {
      val rsp = new LiteRspMsg(
        0,
        req.responseProcess,
        req.oldRequest,
        content)
      sender.response(rsp)
    }
    else {
      val rsp = new LiteRspMsg(
        recursionDepth + 1,
        req.responseProcess,
        req.oldRequest,
        content)
      val oldMsg = curMsg
      curMsg = rsp
      try {
        (req.responseProcess orElse uncaughtMsg)(content)
      } catch {
        case ex: Exception => exceptionHandler(ex)
      }
      curMsg = oldMsg
    }
  }
}

sealed abstract class LiteMsg(depth: Int,
                       pf: PartialFunction[Any, Unit],
                       oldReq: LiteReqMsg,
                       data: Any) {

  def recursionDepth = depth

  def responseProcess = pf

  def oldRequest = oldReq

  def content = data
}

final class LiteReqMsg(recursionDepth: Int,
                       destination: ActiveActor,
                       pf: PartialFunction[Any, Unit],
                       oldReq: LiteReqMsg,
                       data: Any,
                       src: LiteSrc)
  extends LiteMsg(recursionDepth, pf, oldReq, data) {
  var active = true

  def sender = src

  def target = destination
}

final class LiteRspMsg(recursionDepth: Int,
                       pf: PartialFunction[Any, Unit],
                       oldReq: LiteReqMsg,
                       data: Any)
  extends LiteMsg(recursionDepth, pf, oldReq, data)

case class ActiveActor(actor: LiteActor)
