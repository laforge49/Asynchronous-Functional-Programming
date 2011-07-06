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

class LiteReactor
  extends Reactor[LiteMsg]
  with SystemContextGetter {
  private var curMsg: LiteMsg = null
  private var _systemContext: SystemContext = null

  start

  def systemContext = _systemContext

  def systemContext(sc: SystemContext) {
    _systemContext = sc
  }

  def isMailboxEmpty = mailboxSize == 0

  def newReactor: LiteReactor = {
    if (_systemContext == null) new LiteReactor
    else _systemContext.newReactor
  }

  override def scheduler = super.scheduler

  override def act {
    loop{
      react{
        case msg: LiteReqMsg => {
          curMsg = msg
          val target = msg.target
          val reqFunction = target.messageFunctions.get(msg.content.getClass)
          reqFunction(msg.content, back)
        }
        case msg: LiteRspMsg => {
          curMsg = msg
          val sender = currentRequestMessage.sender
          msg.responseProcess(msg.content)
        }
        case msg => {
          curMsg = null
          throw new IllegalArgumentException
        }
      }
    }
  }

  override def exceptionHandler: PartialFunction[Exception, Unit] = {
    case ex: TransparentException => {
      reply(ErrorRsp(ex.toString))
    }
    case ex: Exception => {
      ex.printStackTrace
      reply(ErrorRsp(ex.toString))
    }
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

  def send(targetActor: LiteActor, content: AnyRef)
          (responseProcess: PartialFunction[Any, Unit]) {
    val oldReq = currentRequestMessage
    val sender = oldReq.target
    val targetReactor = targetActor.liteReactor
    val req = new LiteReqMsg(
      targetActor,
      responseProcess,
      oldReq,
      content,
      sender)
    targetReactor.request(req)
  }

  private def reply(content: Any) {
    val req = currentRequestMessage
    if (!req.active) return
    req.active = false
    val sender = req.sender
    val rsp = new LiteRspMsg(
      req.responseProcess,
      req.oldRequest,
      content)
    sender.response(rsp)
  }

  def back: PartialFunction[Any, Unit] = {
    case msg => reply(msg)
  }
}

sealed abstract class LiteMsg(pf: PartialFunction[Any, Unit],
                              oldReq: LiteReqMsg) {

  def responseProcess = pf

  def oldRequest = oldReq
}

final class LiteReqMsg(destination: LiteActor,
                       pf: PartialFunction[Any, Unit],
                       oldReq: LiteReqMsg,
                       data: AnyRef,
                       src: LiteSrc)
  extends LiteMsg(pf, oldReq) {
  var active = true

  def sender = src

  def target = destination

  def content = data
}

final class LiteRspMsg(pf: PartialFunction[Any, Unit],
                       oldReq: LiteReqMsg,
                       data: Any)
  extends LiteMsg(pf, oldReq) {

  def content = data
}

case class ActiveActor(actor: LiteActor)
