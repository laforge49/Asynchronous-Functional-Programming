/*
 * Copyright 2010 M.Naji
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
package actors
package application

import util.actors.res._
import exchange.ExternalAddress
import util.actors.msgs.ErrorMsg
import util.actors.AsynchronousActor
import util.{SystemComposite, Configuration}
import util.com.shrt.{ShortRsp, ShortReq}
import util.com.lng.{LongRsp, LongReq}

abstract class ApplicationActor(systemContext: SystemComposite, uuid: String) extends AsynchronousActor(systemContext, uuid) {

  val externalAddress = ExternalAddress(Configuration(localContext).localServerName,Uuid(uuid))

  override def act {
    loop {
      react{
        case msg: ReceivableMessage => localMessageReceived(msg)
        case msg: LongReq => requestReceived(msg)
        case msg: ShortReq =>requestReceived(msg)
        case msg: LongRsp => responseReceived(msg)
        case msg: ShortRsp => responseReceived(msg)
        case msg: ErrorMsg => errorReceived(msg)
        case msg: AnyRef => messageHandler(msg)
      }
    }
  }

  protected def localMessageReceived(msg: ReceivableMessage){
    this ! msg.content
  }

  protected def requestReceived(msg: Any) {
    debug(msg.asInstanceOf[AnyRef])
    val message = Message.repliableMessage(msg)
    this ! message.content
  }

  protected def responseReceived(msg: LongRsp) {
    debug(msg)
    val message = Message.receivableMessage(msg)
    this ! message.content
  }

  protected def responseReceived(msg: ShortRsp) {
    debug(msg)
    val message = Message.receivableMessage(msg)
    this ! message.content
  }

  protected def errorReceived(msg: ErrorMsg) {
    error(msg)
    this ! Message.errorMessage(msg.header,msg.error,ExternalAddress(msg.serverName,msg.resourceName))
  }
}