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

import scala.actors.Reactor
import java.util.ArrayList

class Mailbox
  extends Reactor[ArrayList[MailboxMsg]]
  with MsgCtrl {

  def isMailboxEmpty = mailboxSize == 0

  override def scheduler = super.scheduler

  private var curMsg: MailboxMsg = null

  def currentMessage = curMsg

  def currentRequestMessage = {
    if (curMsg.isInstanceOf[MailboxReq])
      curMsg.asInstanceOf[MailboxReq]
    else
      curMsg.asInstanceOf[MailboxRsp].oldRequest
  }

  var exceptionFunction: Exception => Unit = null

  def reqExceptionFunction(ex: Exception) {
    reply(ex)
  }

  val pending = new java.util.HashMap[MsgCtrl, ArrayList[MailboxMsg]]

  def addPending(target: MsgSrc, msg: MailboxMsg) {
    val ctrl = target.ctrl
    var blkmsg = pending.get(ctrl)
    if (blkmsg == null) {
      blkmsg = new ArrayList[MailboxMsg]
      pending.put(ctrl, blkmsg)
    }
    blkmsg.add(msg)
    if (blkmsg.size > 63) {
      pending.remove(ctrl)
      ctrl._send(blkmsg)
    }
  }

  override def act {
    loop{
      react{
        case blkmsg: ArrayList[MailboxMsg] => {
          val it = blkmsg.iterator
          while (it.hasNext) {
            it.next match {
              case msg: MailboxReq => req(msg)
              case msg: MailboxRsp => rsp(msg)
            }
          }
          if (isMailboxEmpty && !pending.isEmpty) {
            val it = pending.keySet.iterator
            while (it.hasNext) {
              val ctrl = it.next
              val blkmsg = pending.get(ctrl)
              ctrl._send(blkmsg)
            }
            pending.clear
          }
        }
      }
    }
  }

  def req(msg: MailboxReq) {
    curMsg = msg
    val target = msg.target
    exceptionFunction = reqExceptionFunction
    val bound = target.messageFunctions.get(msg.req.getClass)
    try {
      if (bound == null)
        throw new IllegalArgumentException("unbound message: " + msg.req.getClass.getName)
      bound.process(msg.req, reply)
    } catch {
      case ex: Exception => {
        reply(ex)
      }
    }
  }

  def rsp(msg: MailboxRsp) {
    curMsg = msg
    exceptionFunction = msg.senderExceptionFunction
    if (msg.rsp.isInstanceOf[Exception]) {
      exceptionFunction(msg.rsp.asInstanceOf[Exception])
    }
    else {
      try {
        msg.responseFunction(msg.rsp)
      } catch {
        case ex: Exception => {
          exceptionFunction(ex)
        }
      }
    }
  }

  def send(targetActor: Actor, content: AnyRef)
          (responseFunction: Any => Unit) {
    val oldReq = currentRequestMessage
    val sender = oldReq.target
    val req = new MailboxReq(
      targetActor,
      responseFunction,
      oldReq,
      content,
      sender,
      exceptionFunction)
    addPending(targetActor, req)
  }

  private def reply(content: Any) {
    val req = currentRequestMessage
    if (!req.active) return
    req.active = false
    val sender = req.sender
    val rsp = new MailboxRsp(
      req.responseFunction,
      req.oldRequest,
      content,
      req.senderExceptionFunction)
    addPending(sender, rsp)
  }

  override def _send(blkmsg: ArrayList[MailboxMsg]) {
    this ! blkmsg
  }

  start
}
