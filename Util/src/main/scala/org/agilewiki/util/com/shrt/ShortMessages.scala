/*
 * Copyright 2010 Bill La Forge
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
package org.agilewiki.util
package com
package shrt

import actors.res._
import actors.msgs.{RequestMsg, ErrorMsg}
import actors.InternalAddress

case class ShortReq(requester: ShortTransceiverActor,
                    msgUuid: String,
                    payload: DataInputStack) extends RequestMsg(requester, null) {
  def remoteArkName = requester._destinationArkName

  def sendRsp(payload: DataOutputStack) {
    requester.asInstanceOf[ShortTransceiverActor].sendRsp(msgUuid, payload)
  }

  def sendError(txt: String, arkName: String, resourceName: ResourceName) {
    requester.asInstanceOf[ShortTransceiverActor].sendError(msgUuid, txt, arkName, resourceName)
  }

  def header = null
}

case class ShortRsp(headers: Any, payload: DataInputStack)

case class ShortError(header: Any, error: String, serverName: String, resourceName: ResourceName)
        extends ErrorMsg(header, error, serverName, resourceName)



/*private[shrt]*/ case class SendShortReq(requester: InternalAddress,
                                      destinationArkName: String,
                                      destinationActorName: ResourceName,
                                      header: Any,
                                      payload: DataOutputStack) extends RequestMsg(requester, header)


/*private[shrt]*/ case class SendShortRsp(msgUuid: String, payload: DataOutputStack)

/*private[shrt]*/ case class SendShortError(msgUuid: String, txt: String, arkName: String, resourceName: ResourceName)

/*private[shrt]*/ case class ShortTimeout()