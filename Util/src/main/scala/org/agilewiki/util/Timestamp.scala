/*
 * Copyright 2010 Barrie McGuire
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

import java.lang.Long
import org.joda.time.{DateTimeZone, DateTime}

/**
 * Utility object for creating and working with unique timestamps.
 * @see <a href="http://sites.google.com/site/agilewiki/agilewiki5/kernel-layer/other-kernel-classes-and-objects/timestamp">
 * AgileWiki project documentation</a>
 *
 * @author <a href="mailto:barrie.mcguire@googlemail.com">Barrie McGuire</a>
 */
object Timestamp {
  val CURRENT_TIME = "7FFFFFFFFFFFF"
  val TIMESTAMP_LENGTH = 13
  private var previousTime: Long = _
  private var previousSequence: Int = _

  /**
   * Get a new hexadecimal representation of a timestamp.
   * @return a timestamp.
   */
  def timestamp = {
    synchronized {
      val dt = new DateTime
      // Bit shift the millisecond time as the upper bits are not used
      val millisecondTime = dt.getMillis() << 10

      if (millisecondTime == previousTime) {
        previousSequence += 1
      }
      else {
        previousSequence = 0
        // Save the new time values
        previousTime = millisecondTime
      }

      val uniqueTime = millisecondTime + previousSequence

      // Format the time as a hex string
      val rv = Long.toHexString(uniqueTime)
      rv
    }
  }

    def format(ts: String, offset: scala.Long): String = {
      format(ts, offset, "yyyy-MM-dd HH:mm:ss SSS")
    }

    def format(ts: String, offset: scala.Long, form: String): String = {
      val long1: scala.Long = Long.parseLong(ts,16)
      val long2: scala.Long = (long1 / 1024)
      val long3: scala.Long = (long2 + offset)
      val dt = new DateTime(long3, DateTimeZone.UTC)
      val rv = dt.toString(form)
      rv
    }

  /**
   * Returns the inverted form of the given timestamp string.
   * @param timestamp the hexadecimal timestamp representation to invert.
   * @return an inverted timestamp.
   */
  def invert(timestamp: String) = {
    val ndx = timestamp.indexOf('_')
    val value = if (ndx >= 0) timestamp.substring(0, ndx) else timestamp

    val ts = Long.toHexString(~Long.parseLong(value, 16))
    ts.substring(ts.length() - TIMESTAMP_LENGTH) + (if (ndx >= 0) timestamp.substring(ndx) else "")
  }
}
