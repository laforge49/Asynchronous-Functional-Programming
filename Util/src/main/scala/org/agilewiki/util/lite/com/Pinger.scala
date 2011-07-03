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

import java.util.{TimerTask, Timer}

case class RetryReq(notification: AnyRef, timeout: Long)

case class RetryRsp(tt: TimerTask)

case class ExtendedRetryReq(retry: LiteReqMsg, notification: AnyRef, timeout: Long)

case class ExtendedRetryRsp(tt: TimerTask)

class Pinger(reactor: LiteReactor)
  extends LiteActor(reactor, null) {
  private val timer = new Timer

  addRequestHandler{
    case req: RetryReq => _retry(req)(back)
    case req: ExtendedRetryReq => _extendedRetry(req)(back)
  }

  private def _retry(req: RetryReq)
                    (responseProcess: PartialFunction[Any, Unit]) {
    responseProcess(RetryRsp(retry(req.timeout, liteReactor.currentRequestMessage, req.notification)))
  }

  private def _extendedRetry(req: ExtendedRetryReq)
                            (responseProcess: PartialFunction[Any, Unit]) {
    responseProcess(ExtendedRetryRsp(retry(req.timeout, req.retry, req.notification)))
  }

  private def retry(timeout: Long, reqMsg: LiteReqMsg, notification: AnyRef) = {
    val oldRequest = reqMsg.oldRequest
    val target = oldRequest.target
    val tt = new TimerTask {
      override def run {
        val newReqMsg = new LiteReqMsg(
          target,
          oldRequest.responseProcess,
          oldRequest.oldRequest,
          notification,
          oldRequest.sender)
        target.actor.liteReactor.request(newReqMsg)
      }
    }
    liteReactor.scheduler.execute{
      timer.schedule(tt, timeout)
    }
    tt
  }
}
