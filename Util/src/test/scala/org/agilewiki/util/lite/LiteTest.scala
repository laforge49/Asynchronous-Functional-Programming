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
package lite

import org.specs.SpecificationWithJUnit

class LiteTestActor(reactor: LiteReactor, next: LiteActor)
  extends LiteActor(reactor) {
  addRequestHandler {
    case reqContent => {
      if (next == null) reply(reqContent)
      else send(next, reqContent) {
        case rspContent => reply(rspContent)
      }
    }
  }
}

class LiteTest extends SpecificationWithJUnit {
  "nba" should {
    "run 1000 * 1000" in {
      val M = 2
      val N = 2
      val actors = new Array[LiteActor](M)
      var j = 0
      while (j < M) {
        var actor: LiteActor = null
        val reactor = new LiteReactor(null)
        var i = 0
        while (i < N) {
          actor = new LiteTestActor(reactor, actor)
          i = i + 1
        }
        actors.update(j, actor)
        j = j + 1
      }
      val futures = new Array[LiteFuture](M)
      j = 0
      while (j < M) {
        val future = new LiteFuture
        futures.update(j, future)
        j = j + 1
      }
      val t0 = System.currentTimeMillis
      j = 0
      while (j < M) {
        val future = futures(j)
        val actor = actors(j)
        future.send(actor, null)
        j = j + 1
      }
      j = 0
      while (j < M) {
        futures(j).get
        j = j + 1
      }
      val t1 = System.currentTimeMillis
      val t = t1 - t0
      if (t == 0) println("too short a run--time is 0 miliseconds!")
      else {
        val mps = 2 * M * N * 1000L / t
        println("messages per second = " + mps)
      }
    }
  }
}
