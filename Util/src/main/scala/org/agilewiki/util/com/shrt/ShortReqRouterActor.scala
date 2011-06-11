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

package org.agilewiki
package util
package com
package shrt

import java.util.HashMap
import actors._
import com.ark.actor.ArksRsp
import res.{ClassName, Uuid, ResourceName}
import com.DataOutputStack
import com.udp.{Udp, ReceiveBasicRequest}

class ShortReqRouterActor(systemContext: SystemComposite,
                          uuid: String)
        extends AsynchronousActor(systemContext, uuid) {
  Actors(systemContext).remember(this)
  private var state = 0

  private def pendingArks = state == 0

  private def ready = state == 1

  var arkNames = Set.empty[String]
  private val shortTransceiverActors = new HashMap[String, ShortTransceiverActor]

  final override def messageHandler = {
    case msg: ArksRsp if pendingArks => arks(msg)
    case msg: SendShortReq if ready => sendReq(msg)
    case msg: ReceiveBasicRequest if ready => receiveBasicRequest(msg)
    case msg if ready => unexpectedMsg(msg)
  }

  private def arks(msg: ArksRsp) {
//    debug(msg)
    arkNames = Set.empty[String]
    for (arkName <- msg.arkNames) {
      arkNames += arkName
      val shortTransceiverActor = ShortTransceiverActor(localContext,
        arkName,
        Udp(localContext).udpSenderActor,
        Uuid(ShortProtocol.SHORT_ACTOR))
      shortTransceiverActors.put(arkName, shortTransceiverActor)
    }
    state += 1
  }

  private def sendReq(msg: SendShortReq) {
//    debug(msg)
    val shortTransceiverActor = shortTransceiverActors.get(msg.destinationArkName)
    if (shortTransceiverActor == null)
      msg.requester ! ShortError(msg.header, "Unknown destination ark: " + msg.destinationArkName, util.Configuration(localContext).localServerName, ClassName(getClass))
    else
      shortTransceiverActor ! msg
  }

  private def receiveBasicRequest(msg: ReceiveBasicRequest) {
//    debug(msg)
    val shortTransceiverActor = shortTransceiverActors.get(msg.ark)
    if (shortTransceiverActor != null)
      shortTransceiverActor ! msg
  }

  def sendReq(sender: InternalAddress,
                              destinationArkName: String,
                              destinationActorName: ResourceName,
                              headers: Any,
                              payload: DataOutputStack) {
    this ! SendShortReq(sender,
      destinationArkName,
      destinationActorName,
      headers,
      payload)
  }
}

