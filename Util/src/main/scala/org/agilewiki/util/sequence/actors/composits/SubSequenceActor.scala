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
package sequence
package actors
package basic
package composits

import util.actors._
import util.actors.res._

class SubSequenceActor(systemContext: SystemComposite, uuid: String)
        extends AsynchronousActor(systemContext, uuid) with SequenceConvenience {
  private[sequence] var sequence: Agent = null
  private[sequence] var prefix: String = null
  private var atEnd = false
  private var requester: InternalAddress = null
  private var header: Any = null

  final override def messageHandler = {
    case msg: CurrentMsg => currentMsg(msg)
    case msg: NextMsg => nextMsg(msg)
    case msg: ResultMsg => resultMsg(msg)
    case msg: EndMsg => endMsg(msg)
    case msg => unexpectedMsg(msg)
  }

  private def currentMsg(msg: CurrentMsg) {
    if (atEnd) throw new IllegalStateException("already at end")
    requester = msg.requester
    header = msg.header
    var key = msg.key
    if (key == null) key = lastResult
    if (key == null) key = prefix
    sequence ! CurrentMsg(this, key, null)
  }

  private def nextMsg(msg: NextMsg) {
    if (atEnd) throw new IllegalStateException("already at end")
    requester = msg.requester
    header = msg.header
    var key = msg.key
    if (key == null) key = lastResult
    if (key == null) {
      key = prefix
      sequence ! CurrentMsg(this, key, null)
    } else {
      sequence ! NextMsg(this, key, null)
    }
  }

  private def resultMsg(msg: ResultMsg) {
    val r = requester
    requester = null
    lastResult = msg.result
    if (lastResult.startsWith(prefix)) {
      r ! ResultMsg(header,lastResult)
    } else {
      atEnd = true
      r ! EndMsg(header)
    }
  }

  private def endMsg(msg: EndMsg) {
    atEnd = true
    val r = requester
    requester = null
    r ! EndMsg(header)
  }
}

object SubSequenceActor {
  def apply(systemContext: SystemComposite, sequence: Agent, prefix: String) = {
    val seq = Actors(systemContext).actorFromClassName(ClassName(classOf[SubSequenceActor])).asInstanceOf[SubSequenceActor]
    seq.sequence = sequence
    seq.prefix = prefix
    seq
  }
}
