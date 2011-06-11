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

class InternalAddressActor(reactor: LiteReactor, uuid: Uuid) extends LiteActor(reactor) {
  def getUuid = uuid
}

class LiteActor(reactor: LiteReactor)
  extends LiteSrc
  with SystemContext {
  var requestHandler: PartialFunction[Any, Unit] = null
  private var _currentReactor = reactor

  def liteReactor = reactor

  def systemContext = liteReactor.asInstanceOf[ContextReactor].systemContext

  def currentReactor = _currentReactor

  def currentReactor(activeReactor: LiteReactor) {
    if (reactor.eq(null)) _currentReactor = activeReactor
  }

  def send(actor: LiteActor, content: Any)
          (responseProcess: PartialFunction[Any, Unit]) {
    currentReactor.send(actor, content)(responseProcess)
  }

  def reply(content: Any) {
    if (currentReactor.currentRequestMessage.sender.eq(this)) throw new IllegalAccessError
    currentReactor.reply(content)
  }

  override def response(msg: LiteRspMsg) {
    currentReactor.response(msg)
  }

  def senderUuid = currentReactor.currentRequestMessage.sender.asInstanceOf[InternalAddressActor].getUuid
}
