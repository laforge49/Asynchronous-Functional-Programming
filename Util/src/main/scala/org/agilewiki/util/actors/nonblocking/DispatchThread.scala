/*
 * Copyright 2011 Bill La Forge
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
package actors
package nonblocking

import java.util.concurrent.LinkedBlockingQueue

class DispatchThread(dispatchManager: DispatchManager, next: Int, dispatchers: Array[DispatchThread]) extends Thread {
  private[DispatchThread] val pendingActors = new LinkedBlockingQueue[NBActorCapability]
  private[nonblocking] var idle: DispatchThread = null

  override def run {
    val nextDT = dispatchers(next)
    while (true) {
      val nba = pendingActors.poll
      if (nba != null) process(nba)
      else {
        nextDT.idle = this
        process(pendingActors.take)
      }
    }
  }

  private def process(nba: NBActorCapability) {
    if (nba.dispatchThread == this) {
      if (idle != null && idle != this) {
        if (pendingActors.size > 10) {
          nba._dispatchThread = idle
          idle.moreWork(nba)
        } else {
          nba.processMessages
        }
        idle = null
      }
      else nba.processMessages
    }
    else {
      nba.dispatchThread.moreWork(nba)
    }
  }

  private[nonblocking] def moreWork(nba: NBActorCapability) {
    pendingActors.put(nba)
  }
}
