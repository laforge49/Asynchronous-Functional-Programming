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

import java.lang.ref.ReferenceQueue
import cache.NamedWeakReference
import java.util.{TimerTask, UUID, HashMap}

case class MapPutReq(actor: InternalAddressActor)

case class MapGetReq(uuid: Uuid)

case class MapGetRsp(actor: InternalAddressActor)

case class RememberReq(actor: InternalAddressActor, period: Int)

case class ForgetReq(actor: InternalAddressActor)

case class CreateReq(reactor: LiteReactor, className: ClassName)

case class CreateRsp(actor: InternalAddressActor)

case class CreateUuidReq(reactor: LiteReactor, className: ClassName, uuid: Uuid)

case class CreateUuidRsp(actor: InternalAddressActor)

case class CreateForwardReq(reactor: LiteReactor, className: ClassName, msg: Any)

case class ForwardReq(uuid: Uuid, msg: Any)

class LiteManager(systemContext: SystemComposite) extends LiteActor(new ContextReactor(systemContext)) {
  private val referenceQueue = new ReferenceQueue[InternalAddressActor]
  private val hashMap = new HashMap[String, NamedWeakReference[InternalAddressActor]]
  private var longLivingActors = new java.util.HashMap[String, (LiteActor, TimerTask)]
  private var factory = new LiteFactory
  private lazy val pinger = Lite(systemContext).pinger

  requestHandler = {
    case req: CreateReq => {
      val uuid = Uuid(UUID.randomUUID.toString)
      send(factory, CreateUuidReq(req.reactor, req.className, uuid)) {
        case rsp: CreateRsp => {
          mapPut(rsp.actor, uuid.value)
          reply(rsp)
        }
      }
    }
    case req: CreateForwardReq => {
      val uuid = Uuid(UUID.randomUUID.toString)
      send(factory, CreateUuidReq(req.reactor, req.className, uuid)) {
        case rsp: CreateRsp => {
          val actor = rsp.actor
          mapPut(actor, uuid.value)
          send(actor, req.msg) {
            case rsp => reply(rsp)
          }
        }
      }
    }
    case req: CreateUuidReq => {
      send(factory, req) {
        case rsp: CreateRsp => {
          val actor = rsp.actor
          mapPut(actor, req.uuid.value)
          reply(CreateUuidRsp(actor))
        }
      }
    }
    case req: RememberReq => {
      val actor = req.actor
      val uuid = actor.getUuid.value
      val retryReq = RetryReq(ForgetReq(actor), req.period)
      send(pinger, retryReq) {
        case pr: RetryRsp => {
          val tt = pr.tt
          if (longLivingActors.containsKey(uuid)) {
            val (actor2, tt2) = longLivingActors.remove(uuid)
            tt2.cancel
          }
          longLivingActors.put(uuid,(actor, tt))
        }
      }
    }
    case req: ForgetReq => {
      val (actor, tt) = longLivingActors.remove(req.actor.getUuid.value)
      tt.cancel
    }
    case req: MapPutReq => {
      val actor = req.actor
      mapPut(actor, actor.getUuid.value)
    }
    case req: MapGetReq => {
      val nwr = hashMap.get(req.uuid.value)
      var rv: InternalAddressActor = null
      if (nwr != null) {
        rv = nwr.get
      }
      reply(MapGetRsp(rv))
    }
    case req: ForwardReq => {
      val nwr = hashMap.get(req.uuid.value)
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

  private def mapPut(item: InternalAddressActor, name: String) {
    val nwr = new NamedWeakReference[InternalAddressActor](item, referenceQueue, name)
    hashMap.put(name, nwr)
    var more = true
    while (more) {
      val x = referenceQueue.poll.asInstanceOf[NamedWeakReference[InternalAddressActor]]
      if (x == null) {
        more = false
      } else {
        hashMap.remove(x.name)
      }
    }
  }
}
