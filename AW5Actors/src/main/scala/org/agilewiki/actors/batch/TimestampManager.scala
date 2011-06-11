/*
 * Copyright 2011 B. La Forge
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
package batch

import util.Timestamp

class TimestampManager {
  private val pending = new java.util.TreeMap[String, BatchDriverActor]
  private val completed = new java.util.TreeSet[String]
  private var last: String = ""

  def openTransaction(batchDriverActor: BatchDriverActor) = {
    synchronized {
      val ts = Timestamp.timestamp
      pending.put(ts, batchDriverActor)
      ts
    }
  }

  def closeTransaction(ts: String) {
    synchronized {
      pending.remove(ts)
      completed.add(ts)
      if (pending.size == 0) {
        last = completed.last
        completed.clear
      } else while (completed.size > 0 && completed.first < pending.firstKey) {
        last = completed.first
        completed.remove(last)
      }
    }
  }

  def latestQueryTime = {
    synchronized {
      var ts: String = null
      if (pending.size == 0) {
        if (last.length == 0) {
          ts = Timestamp.timestamp
          last = ts
        } else ts = last
      }
      else {
        ts = pending.firstKey
        val lts = java.lang.Long.parseLong(ts, 16) - 1
        ts = lts.toHexString
      }
      ts
    }
  }
}