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
package actors
package sequences

import util.actors._
import util.actors.res._
import util.sequence.actors._
import util.com.DataOutputStack
import util.SystemComposite
import util.com.shrt.{ShortProtocol, ShortRsp, ShortError}

trait SequenceAgent
        extends SequenceConvenience {
  this: InternalAddressActor =>

  private var remoteArkName: String = null
  private var sequenceResourceName: ResourceName = null

  private var rspActor: Option[InternalAddress] = None

  private var commonArgs: DataOutputStack = DataOutputStack()

  def messageHandler: PartialFunction[AnyRef, Unit] = {
    case msg: CurrentMsg if rspActor.isEmpty => currentMsg(msg)
    case msg: NextMsg if rspActor.isEmpty => nextMsg(msg)
    case msg: ShortRsp if rspActor.isDefined => shortRspMsg(msg)
    case msg: ShortError if rspActor.isDefined => shortErrorMsg(msg)
    case msg => unexpectedMsg(msg)
  }

  private def currentMsg(msg: CurrentMsg) {
    debug(msg)
    rspActor = Some(msg.requester)
    try {
      var key = msg.key
      if (key == null) key = lastResult
      if (key == lastResult && lastResult != null) {
        rspActor.get ! ResultMsg(msg.header, key)
      } else {
        if (key == null) {
          key = ""
        }
        val dataOutput = commonArgs.clone
        dataOutput.writeUTF(key)
        dataOutput.writeUTF("current")
        ShortProtocol(localContext).actor.sendReq(this,
          remoteArkName,
          sequenceResourceName,
          msg.header,
          dataOutput)
      }
    } catch {
      case ex: Throwable => shortErrorMsg(ShortError(msg.header, ex.toString, util.Configuration(localContext).localServerName, ClassName(getClass)))
    }
  }

  private def nextMsg(msg: NextMsg) {
    debug(msg)
    rspActor = Some(msg.requester)
    try {
      var key = msg.key
      if (key == null) key = lastResult
      if (key == null) key = ""
      val dataOutput = commonArgs.clone
      dataOutput.writeUTF(key)
      dataOutput.writeUTF("next")
      ShortProtocol(localContext).actor.sendReq(this,
        remoteArkName,
        sequenceResourceName,
        msg.header,
        dataOutput)
    } catch {
      case ex: Throwable => shortErrorMsg(ShortError(msg.header, ex.toString, util.Configuration(localContext).localServerName, ClassName(getClass)))
    }
  }

  private def shortRspMsg(msg: ShortRsp) {
    try {
      debug(msg)
      val result = msg.payload.readUTF
      if (result.length > 0) {
        lastResult = result
        rspActor.get ! ResultMsg(msg.headers, result)
      } else rspActor.get ! EndMsg(msg.headers)
    } catch {
      case ex: Throwable => shortErrorMsg(ShortError(msg.headers, ex.toString, util.Configuration(localContext).localServerName, ClassName(getClass)))
    }
    rspActor = None
  }

  private def shortErrorMsg(msg: ShortError) {
    debug(msg)
    rspActor.get ! msg
    rspActor = None
  }

}

class SynchronousSequenceAgent(systemContext: SystemComposite, uuid: String) extends AsynchronousActor(systemContext, uuid)
        with SequenceAgent

class SequenceActor(systemContext: SystemComposite, uuid: String) extends AsynchronousActor(systemContext, uuid)
        with SequenceAgent

object SequenceAgent {
  private def apply(systemContext: SystemComposite, inputArgs: DataOutputStack,
                    remoteArkName: String, sequenceResourceName: ResourceName, agent: ClassName) = {
    val sequenceAgent = Actors(systemContext).actorFromClassName(agent).asInstanceOf[SequenceAgent]
    sequenceAgent.commonArgs = inputArgs
    sequenceAgent.remoteArkName = remoteArkName
    sequenceAgent.sequenceResourceName = sequenceResourceName
    sequenceAgent
  }

  def actor(systemContext: SystemComposite, inputArgs: DataOutputStack,
            remoteArkName: String, sequenceResourceName: ResourceName) =
    apply(systemContext, inputArgs, remoteArkName, sequenceResourceName, ClassName(classOf[SequenceActor]))

  def pseudoActor(systemContext: SystemComposite, inputArgs: DataOutputStack,
                  remoteArkName: String, sequenceResourceName: ResourceName) =
    apply(systemContext, inputArgs, remoteArkName, sequenceResourceName, ClassName(classOf[SynchronousSequenceAgent]))

}