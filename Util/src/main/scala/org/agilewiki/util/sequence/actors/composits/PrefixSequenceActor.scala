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

import util.actors.res._
import util.actors.Actors
import util.actors.msgs.ErrorMsg

class PrefixSequenceActor(systemContext: SystemComposite, uuid: String)
        extends SingleSequenceComposite(systemContext, uuid){
  private var delimiter: Char = _
  private lazy val nextDelimiter = (if(reverse) delimiter else (delimiter + 1)).asInstanceOf[Char]

  protected override def process(msg: CurrentMsg) {
    debug(msg)
    requester = Some(msg.requester)
    header = msg.header
    var key = msg.key
    if (key == null && lastResult != null) {
      requester.get ! ResultMsg(msg.header, lastResult)
      requester = None
      header = null
    } else {
     if(key != null) key += delimiter
     sequence.sendCurrent(this, key, header)
    }

  }

  protected override def process(msg: NextMsg) {
    debug(msg)
    header = msg.header
    requester = Some(msg.requester)
    val key = if(msg.key != null) msg.key + nextDelimiter else null
    sequence.sendNext(this,key,header)
  }

  protected override def process(msg: ResultMsg) {
    debug(msg)
    var key = msg.result
    val i = key.indexOf(delimiter)
    if (i == -1) {
      sequence.sendNext(this,key,header)
      return
    }
    key = key.substring(0, i)
    lastResult = key
    requester.get ! ResultMsg(header, lastResult)
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

object PrefixSequenceActor {
  def apply(systemContext: SystemComposite, sequence: SequenceConvenience, delimiter: Char): PrefixSequenceActor = {
    require(sequence != null)
    val rv = Actors(systemContext).actorFromClassName(ClassName(classOf[PrefixSequenceActor])).asInstanceOf[PrefixSequenceActor]
    rv.delimiter = delimiter
    rv.sequence = sequence
    rv.reverse = sequence.isReverse
    rv
  }
}