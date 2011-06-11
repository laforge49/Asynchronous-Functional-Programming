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

import java.util.NavigableSet
import util.actors._
import util.actors.res._

class NavigableSequenceActor(systemContext: SystemComposite, uuid: String)
        extends SynchronousActor(systemContext, uuid) with SequenceConvenience {
  var navigableSet: NavigableSet[String] = null

  final override def messageHandler = {
    case msg: CurrentMsg => current(msg)
    case msg: NextMsg => next(msg)
    case msg => unexpectedMsg(msg)
  }

  private def current(msg: CurrentMsg) {
    var tmp: String = null
    if (!navigableSet.isEmpty) {
      if (msg.key == null) {
        if (lastResult == null) {
          tmp = if (reverse) navigableSet.last
          else navigableSet.first
        } else tmp = lastResult
      } else {
        tmp = if (reverse) navigableSet.floor(msg.key)
        else navigableSet.ceiling(msg.key)
      }
    }
    if (tmp == null) {
      msg.requester ! EndMsg(msg.header)
    }
    else {
      lastResult = tmp
      msg.requester ! ResultMsg(msg.header, lastResult)
    }
  }

  private def next(msg: NextMsg) {
    var tmp: String = null
    if (!navigableSet.isEmpty) {
      if (msg.key == null) {
        if (lastResult == null) {
          tmp = if (reverse) navigableSet.last
          else navigableSet.first
        } else {
          tmp = if (reverse) navigableSet.lower(lastResult)
          else navigableSet.higher(lastResult)
        }
      } else {
        tmp = if (reverse) navigableSet.lower(msg.key)
        else navigableSet.higher(msg.key)
      }
    }
    if (tmp == null) {
      msg.requester ! EndMsg(msg.header)
    }
    else {
      lastResult = tmp
      msg.requester ! ResultMsg(msg.header, lastResult)
    }
  }

}

object NavigableSequenceActor {
  def apply (systemContext: SystemComposite, navigableSet: NavigableSet[String], reverse: Boolean) = {
    val seq = Actors(systemContext).actorFromClassName(ClassName(classOf[NavigableSequenceActor])).asInstanceOf[NavigableSequenceActor]
    seq.navigableSet = navigableSet
    seq.reverse = reverse
    seq
  }
}
