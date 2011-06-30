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
package seq

import annotation.tailrec

class LiteFilterFunc[T, V](liteSeq: SeqActor[T, V], filter: V => Boolean)
  extends SeqActor[T, V](liteSeq.liteReactor) {
  override def comparator = liteSeq.comparator

  addRequestHandler{
    case req: SeqReq => _filter(req)
  }

  @tailrec private def _filter(req: SeqReq) {
    var async = false
    var sync = false
    var nextKey = null.asInstanceOf[T]
    liteSeq.send(req) {
      case rsp: SeqEndRsp => {
        reply(SeqEndRsp())
      }
      case rsp: SeqResultRsp[T, V] => {
        if (filter(rsp.value)) reply(rsp)
        else {
          if (async) send(SeqNextReq[T](rsp.key)){
            case r => reply(r)
          }
          else {
            sync = true
            nextKey = rsp.key
          }
        }
      }
    }
    if (!sync) {
      async = true
      return
    }
    _filter(SeqNextReq[T](nextKey))
  }
}

class LiteFilterSeq[T, V, V1](liteSeq: SeqActor[T, V], filter: SeqActor[V, V1])
  extends SeqActor[T, V](liteSeq.liteReactor) {
  override def comparator = liteSeq.comparator

  addRequestHandler{
    case req: SeqReq => _filter(req)
  }

  private def _filter(req: SeqReq) {
    liteSeq.send(req) {
      case rsp: SeqEndRsp => reply(SeqEndRsp())
      case rsp: SeqResultRsp[T, V] => {
        filter.hasKey(rsp.value) {
          case r: Boolean => {
            if (r) reply(rsp)
            else _filter(SeqNextReq[T](rsp.key))
          }
        }
      }
    }
  }
}

class LiteExtensionFilterFunc[T, V](seqExtensionActor: SeqExtensionActor[T, V], filter: V => Boolean)
  extends SeqExtensionActor[T, V](seqExtensionActor.liteReactor, new FilterSeqExtension[T, V](seqExtensionActor.seqExtension, filter))

class FilterSeqExtension[T, V](extension: SeqExtension[T, V], filter: V => Boolean)
  extends SeqExtension[T, V] {

  override def comparator = extension.comparator

  override def first: SeqRsp = {
    f(extension.first)
  }

  override def current(k: T): SeqRsp = {
    f(extension.current(k))
  }

  override def next(k: T): SeqRsp = {
    f(extension.next(k))
  }

  @tailrec private def f(rsp: SeqRsp): SeqRsp = {
    if (!rsp.isInstanceOf[SeqResultRsp[T, V]]) return rsp
    val r = rsp.asInstanceOf[SeqResultRsp[T, V]]
    if (filter(r.value)) {
      return rsp
    }
    f(extension.next(r.key))
  }
}
