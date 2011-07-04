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

case class MapPutRsp()

case class MapGetReq(id: ActorId)

case class MapGetRsp(actor: LiteActor)

case class RememberReq(actor: LiteActor, period: Int)

case class RememberRsp()

case class ForgetReq(actor: LiteActor)

case class ForgetRsp()

case class ForwardReq(id: ActorId, msg: AnyRef)

class LiteManager(reactor: LiteReactor)
  extends LiteActor(reactor, null) {
  private val referenceQueue = new ReferenceQueue[LiteActor]
  private val hashMap = new HashMap[String, NamedWeakReference[LiteActor]]
  private var longLivingActors = new java.util.HashMap[String, (LiteActor, TimerTask)]
  private lazy val pinger = Udp(systemContext).pinger

  bind(classOf[RememberReq], _remember)
  bind(classOf[ForgetReq], _forget)
  bind(classOf[MapPutReq], _mapPut)
  bind(classOf[MapGetReq], _mapGet)
  bind(classOf[ForwardReq], _forward)

  private def _remember(msg: AnyRef, responseProcess: PartialFunction[Any, Unit]) {
    val req = msg.asInstanceOf[RememberReq]
    val actor = req.actor
    val id = actor.id.value
    val retryReq = RetryReq(ForgetReq(actor), req.period)
    pinger.send(retryReq) {
      case pr: RetryRsp => {
        val tt = pr.tt
        if (longLivingActors.containsKey(id)) {
          val (actor2, tt2) = longLivingActors.remove(id)
          tt2.cancel
        }
        longLivingActors.put(id,(actor, tt))
      }
    }
    responseProcess(RememberRsp())
  }

  private def _forget(msg: AnyRef, responseProcess: PartialFunction[Any, Unit]) {
    val req = msg.asInstanceOf[ForgetReq]
    val (actor, tt) = longLivingActors.remove(req.actor.id.value)
    tt.cancel
    responseProcess(ForgetRsp())
  }

  private def _mapPut(msg: AnyRef, responseProcess: PartialFunction[Any, Unit]) {
    val req = msg.asInstanceOf[MapPutReq]
    val actor = req.actor
    mapPut(actor, actor.id.value)
    responseProcess(MapPutRsp())
  }

  private def _mapGet(msg: AnyRef, responseProcess: PartialFunction[Any, Unit]) {
    val req = msg.asInstanceOf[MapGetReq]
    val nwr = hashMap.get(req.id.value)
    var rv: LiteActor = null
    if (nwr != null) {
      rv = nwr.get
    }
    responseProcess(MapGetRsp(rv))
  }

  private def _forward(msg: AnyRef, responseProcess: PartialFunction[Any, Unit]) {
    val req = msg.asInstanceOf[ForwardReq]
    val nwr = hashMap.get(req.id.value)
    if (nwr != null) {
      val actor = nwr.get
      if (actor != null) {
        actor.send(req.msg) {
          case rsp => responseProcess(rsp)
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
