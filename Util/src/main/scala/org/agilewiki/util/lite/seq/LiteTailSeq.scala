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

class LiteTailSeq[T, V](liteSeq: SeqActor[T, V], start: T)
  extends SeqActor[T, V](liteSeq.liteReactor) {
  override def comparator = liteSeq.comparator

  addRequestHandler {
    case req: SeqFirstReq => {
      liteSeq.send(SeqCurrentReq(start)) {
        case rsp => reply(rsp)
      }
    }
    case req: SeqCurrentReq[T] => {
      if (comparator.compare(req.key, start) < 0) liteSeq.current(start) {
        case rsp => reply(rsp)
      } else liteSeq.send(req) {
        case rsp => reply(rsp)
      }
    }
    case req: SeqNextReq[T] => {
      if (comparator.compare(req.key, start) < 0) liteSeq.current(start) {
        case rsp => reply(rsp)
      } else liteSeq.send(req) {
        case rsp => reply(rsp)
      }
    }
  }
}

class LiteExtensionTailSeq[T, V](seqExtensionActor: SeqExtensionActor[T, V], start: T)
  extends SeqExtensionActor[T, V](
    seqExtensionActor.liteReactor,
    new TailSeqExtension[T, V](seqExtensionActor.seqExtension, start))

class TailSeqExtension[T, V](extension: SeqExtension[T, V], start: T)
  extends SeqExtension[T, V] {

  override def comparator = extension.comparator

  override def _first: SeqRsp = extension._current(start)

  override def _current(k: T): SeqRsp = {
    if (comparator.compare(k, start) < 0) extension._current(start)
    else extension._current(k)
  }

  override def _next(k: T): SeqRsp = {
    if (comparator.compare(k, start) < 0) extension._current(start)
    else extension._next(k)
  }
}
