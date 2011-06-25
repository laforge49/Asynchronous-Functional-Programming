/*
 * Copyright 2010 Alex K.
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

import java.lang.ref.ReferenceQueue
import cache.NamedWeakReference
import java.util.{TimerTask, HashMap}

case class MapPutReq(actor: LiteActor)

case class MapGetReq(id: ActorId)

case class MapGetRsp(actor: LiteActor)

case class RememberReq(actor: LiteActor, period: Int)

case class ForgetReq(actor: LiteActor)

case class ForwardReq(id: ActorId, msg: Any)

class LiteManager(reactor: LiteReactor)
  extends LiteActor(reactor, null) {
  private val referenceQueue = new ReferenceQueue[LiteActor]
  private val hashMap = new HashMap[String, NamedWeakReference[LiteActor]]
  private var longLivingActors = new java.util.HashMap[String, (LiteActor, TimerTask)]
  private lazy val pinger = Udp(systemContext).pinger

  addRequestHandler {
    case req: RememberReq => {
      val actor = req.actor
      val id = actor.id.value
      val retryReq = RetryReq(ForgetReq(actor), req.period)
      send(pinger, retryReq) {
        case pr: RetryRsp => {
          val tt = pr.tt
          if (longLivingActors.containsKey(id)) {
            val (actor2, tt2) = longLivingActors.remove(id)
            tt2.cancel
          }
          longLivingActors.put(id,(actor, tt))
        }
      }
    }
    case req: ForgetReq => {
      val (actor, tt) = longLivingActors.remove(req.actor.id.value)
      tt.cancel
    }
    case req: MapPutReq => {
      val actor = req.actor
      mapPut(actor, actor.id.value)
    }
    case req: MapGetReq => {
      val nwr = hashMap.get(req.id.value)
      var rv: LiteActor = null
      if (nwr != null) {
        rv = nwr.get
      }
      reply(MapGetRsp(rv))
    }
    case req: ForwardReq => {
      val nwr = hashMap.get(req.id.value)
      if (nwr != null) {
        val actor = nwr.get
        if (actor != null) {
          send(actor, req.msg) {
            case rsp => reply(rsp)
          }
        }
      }
    }
  }

  private def mapPut(item: LiteActor, name: String) {
    val nwr = new NamedWeakReference[LiteActor](item, referenceQueue, name)
    hashMap.put(name, nwr)
    var more = true
    while (more) {
      val x = referenceQueue.poll.asInstanceOf[NamedWeakReference[LiteActor]]
      if (x == null) {
        more = false
      } else {
        hashMap.remove(x.name)
      }
    }
  }
}
