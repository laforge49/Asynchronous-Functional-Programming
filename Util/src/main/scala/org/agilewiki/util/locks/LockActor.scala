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
package locks

import util.actors.AsynchronousActor

class LockActor(systemContext: SystemComposite, uuid: String) extends AsynchronousActor(systemContext, uuid) {
  var locked = false

  final override def messageHandler = {
    case msg: LockMsg if !locked => lock(msg)
    case msg: UnlockMsg => unlock(msg)
    case msg if !msg.isInstanceOf[LockMsg] => unexpectedMsg(msg)
  }

  def lock(msg: LockMsg) {
    debug(msg)
    require(!locked,"double locking on: " + uuid + " @ " + msg.requester)
    locked = true
    msg.requester ! LockedMsg()
  }

  def unlock(msg: UnlockMsg) {
    debug(msg)
    locked = false
    msg.requester ! UnlockedMsg()
  }
}
