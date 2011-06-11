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

import org.specs.SpecificationWithJUnit
import msgs.ServiceRequestMsg

class AsyncTest extends SpecificationWithJUnit {
  "nba" should {
    "run 100 * 100" in {
      val M = 100
      val N = 100
      val properties = _Actors.defaultConfiguration("FUN")
      val systemContext = new _Actors(properties)
      val actors = new Array[AsyncTestActor](1000)
      var j = 0
      while (j < M) {
        var actor: AsyncTestActor = null
        var i = 0
        while (i < N) {
          actor = AsyncTestActor(systemContext, actor)
          i = i + 1
        }
        actors.update(j, actor)
        j = j + 1
      }
      j = 0
      val futures = new Array[InternalAddressFuture](M)
      val t0 = System.currentTimeMillis
      while (j < M) {
        val future = new InternalAddressFuture(systemContext)
        actors(j) ! ServiceRequestMsg(future)
        futures.update(j, future)
        j = j + 1
      }
      j = 0
      while (j < M) {
        futures(j).get
        j = j + 1
      }
      val t1 = System.currentTimeMillis
      val t = t1 - t0
      val mps = 2 * M * N * 1000L / t
      println("messages per second = "+mps)
    }
  }
}