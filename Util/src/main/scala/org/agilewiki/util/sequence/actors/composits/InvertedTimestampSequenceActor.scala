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
package util
package sequence
package actors
package basic
package composites

import util.actors._
import msgs.ErrorMsg
import util.Timestamp
import res.ClassName

class InvertedTimestampSequenceActor(systemContext: SystemComposite, uuid: String)
        extends SingleSequenceComposite(systemContext, uuid) {

  protected override def process(msg: CurrentMsg) {
    debug(msg)
    requester = Some(msg.requester)
    header = msg.header
    var key = msg.key
    if (key == null && lastResult != null) {
      requester.get ! ResultMsg(msg.header, lastResult)
      requester = None
      header = null
    }
    else {
      if (key != null) key = Timestamp.invert(key)
      sequence.sendCurrent(this, key, msg.header)
    }
  }

  protected override def process(msg: NextMsg) {
    debug(msg)
    header = msg.header
    requester = Some(msg.requester)
    var key = msg.key
    if (key != null) key = Timestamp.invert(key)
    sequence.sendNext(this, key, msg.header)
  }

  protected override def process(msg: ResultMsg) {
    debug(msg)
    lastResult = Timestamp.invert(msg.result)
    requester.get ! ResultMsg(msg.headers, lastResult)
    requester = None
    header = null
  }

  protected override def process(msg: EndMsg) {
    debug(msg)
    requester.get ! msg
    requester = None
    header = null
  }

  protected override def process(msg: ErrorMsg) {
    debug(msg)
    requester.get ! msg
    requester = None
    header = null
  }
}

object InvertedTimestampSequenceActor {
  def apply(systemContext: SystemComposite, sequence: SequenceConvenience): InvertedTimestampSequenceActor = {
    val rv: InvertedTimestampSequenceActor = Actors(systemContext).actorFromClassName(
      ClassName(classOf[InvertedTimestampSequenceActor])).asInstanceOf[InvertedTimestampSequenceActor]
    rv.sequence = sequence
    rv.reverse = !sequence.isReverse
    rv
  }
}