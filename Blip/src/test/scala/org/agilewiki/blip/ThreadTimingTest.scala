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

class ThreadTimingTest extends SpecificationWithJUnit {
  "ThreadTimingTest" should {
    "synchronous hello world" in {
      val a = new TimingActor(null)
      println(Future(a, TimingReq("synchronous hello world")))
    }
    "asynchronous hello world" in {
      val a = new TimingActor(null)
      a.setMailbox(new ThreadMailbox)
      println(Future(a, TimingReq("asynchronous hello world")))
    }
    "synchronous timing" in {
      val c = 10//00000 //00
      val m = new ThreadMailbox
      val a1 = new TimingActor(null)
      a1.setMailbox(m)
      val a = new RepeatingActor(a1, c)
      a.setMailbox(m)
      Future(a, TimingReq("hello world"))
      val t0 = System.currentTimeMillis
      Future(a, TimingReq("hello world"))
      val t1 = System.currentTimeMillis
      if (t1 != t0) println("sync msgs per sec = " + (c * 2L * 1000L / (t1 - t0)))
    }
    "quad-synchronous timing" in {
      val c = 10//00000 //00
      val m = new ThreadMailbox
      val a = new ThreadParallelSyncActor(c)
      a.setMailbox(m)
      Future(a, TimingReq("hello world"))
      val t0 = System.currentTimeMillis
      Future(a, TimingReq("hello world"))
      val t1 = System.currentTimeMillis
      if (t1 != t0) println("quad sync msgs per sec = " + (c * 4L * 2L * 1000L / (t1 - t0)))
    }
    "asynchronous timing" in {
      val c = 10//0000
      val m = new ThreadMailbox
      val a = new ThreadParallelAsyncActor(c)
      a.setMailbox(m)
      Future(a, TimingReq("hello world"))
      val t0 = System.currentTimeMillis
      Future(a, TimingReq("hello world"))
      val t1 = System.currentTimeMillis
      if (t1 != t0) println("async msgs per sec = " + (c * 4L * 2L * 1000L / (t1 - t0)))
    }
  }
}