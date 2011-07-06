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
package blip

import org.specs.SpecificationWithJUnit

class TimingTest extends SpecificationWithJUnit {
  "TimingTest" should {
    "do the hello world thing" in {
      val a = new TimingActor(null, null)
      println(Future(a, TimingReq("hello world")))
    }
    "do more" in {
      val c = 1000000
      val a = new RepeatingActor(null, new TimingActor(null, null), c)
      Future(a, TimingReq("hello world"))
      val t0 = System.currentTimeMillis
      Future(a, TimingReq("hello world"))
      val t1 = System.currentTimeMillis
      println("msgs per sec = "+(c * 2L * 1000L / (t1 - t0)))
    }
  }
}
