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
package com

import org.specs.SpecificationWithJUnit

case class PingerTestReq()
case class PingerTestRetryReq(count: Int)
case class PingerTestRsp()

class PingerTestActor(pinger: LiteActor)
  extends LiteActor(new LiteReactor(null), null) {
  addRequestHandler {
    case req: PingerTestReq => _test(req)(back)
    case req: PingerTestRetryReq => _retry(req)(back)
  }

    private def _test(req: PingerTestReq)
                     (responseProcess: PartialFunction[Any, Unit]) {
      pinger.send(RetryReq(PingerTestRetryReq(5), 1)) {
        case rsp: RetryRsp =>
      }
    }

    private def _retry(req: PingerTestRetryReq)
                     (responseProcess: PartialFunction[Any, Unit]) {
      System.err.println(req.count)
      if (req.count < 2) responseProcess(PingerTestRsp())
      else pinger.send(RetryReq(PingerTestRetryReq(req.count - 1), 1)) {
        case rsp: RetryRsp =>
      }
    }
}

class PingerTest extends SpecificationWithJUnit {
  "pinger test" should {
    "count down from 5" in {
      LiteFuture(new PingerTestActor(new Pinger(new LiteReactor(null))), PingerTestReq())
    }
  }
}
