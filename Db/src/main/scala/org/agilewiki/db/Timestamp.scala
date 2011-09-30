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
package db

import blip._
import org.joda.time.{DateTimeZone, DateTime}

class TimestampComponentFactory extends ComponentFactory {
  override def instantiate(actor: Actor) = new TimestampComponent(actor)
}

class TimestampComponent(actor: Actor)
  extends Component(actor) {
  private var previousTime: java.lang.Long = _
  private var previousSequence: Int = _
  private var master:Actor = null

  bind(classOf[GetTimestamp], timestamp)

  override def open {
    super.open
    var s = actor.superior
    while (s != null) {
      if (s.components.get(classOf[TimestampComponentFactory]) != null) {
        master = s
        return
      }
      s = s.superior
    }
  }

  private def timestamp(msg: AnyRef, rf: Any => Unit) {
    if (master != null) {
      master(msg)(rf)
      return
    }
    val dt = new DateTime
    val millisecondTime = dt.getMillis() << 10
    if (millisecondTime == previousTime) {
      previousSequence += 1
    } else {
      previousSequence = 0
      previousTime = millisecondTime
    }
    val uniqueTime = millisecondTime + previousSequence
    rf(uniqueTime)
  }
}

object Timestamp {
  def format(ts: Long, offset: Long): String = {
    format(ts, offset, "yyyy-MM-dd HH:mm:ss SSS")
  }

  def format(ts: Long, offset: Long, form: String): String = {
    val long2 = (ts / 1024)
    val long3 = (long2 + offset)
    val dt = new DateTime(long3, DateTimeZone.UTC)
    val rv = dt.toString(form)
    rv
  }
}