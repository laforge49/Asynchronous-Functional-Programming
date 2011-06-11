/*
 * Copyright 2010 Bill La Forge.
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
import msgs.ErrorMsg
import util.sequence.actors._
import util.sequence.actors.basic.composites.UnionSequenceActor
import util.{SystemComposite, Timestamp}

abstract class AllSequenceAgent(systemContext: SystemComposite, uuid: String)
        extends AsynchronousActor(systemContext, uuid) with SequenceConvenience {
  protected var timestamp: String = Timestamp.timestamp
  private var union: Option[UnionSequenceActor] = None
  private var reply: Option[InternalAddress] = None
  private lazy val unionSequenceActor = UnionSequenceActor(localContext, sequenceConvenienceCollection, reverse)
  private var finished = false


  protected def sequenceConvenienceCollection: Iterable[SequenceConvenience] =
    Iterable.empty[SequenceConvenience]

  final override def messageHandler = {
    case msg: CurrentMsg if !finished => sendCurrent(msg)
    case msg: NextMsg if !finished => sendNext(msg)
    case msg: ResultMsg if reply.isDefined => resultMsg(msg)
    case msg: EndMsg if reply.isDefined => endMsg(msg)
    case msg: ErrorMsg if reply.isDefined => errorMsg(msg)
    case msg => unexpectedMsg(msg)
  }

  private def sendCurrent(msg: CurrentMsg) {
    reply = Some(msg.requester)
    unionSequenceActor ! CurrentMsg(this, msg.key, msg.header)
  }

  private def sendNext(msg: NextMsg) {
    reply = Some(msg.requester)
    unionSequenceActor ! NextMsg(this, msg.key, msg.header)
  }

  private def resultMsg(msg: ResultMsg) {
    lastResult = msg.result
    reply.get ! msg
    reply = None
  }

  private def endMsg(msg: EndMsg) {
    reply.get ! msg
    reply = None
    finished = true
  }

  private def errorMsg(msg: ErrorMsg) {
    reply.get ! msg
    reply = null
    finished = true
  }
}