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

class LiteMapSeq[T, V1, V2](liteSeq: SeqActor[T, V1], map: V1 => V2)
  extends SeqActor[T, V2](null) {
  override def comparator = liteSeq.comparator

  addRequestHandler{
    case req: SeqReq => send(liteSeq.asInstanceOf[LiteActor], req) {
      case rsp: SeqEndRsp => reply(SeqEndRsp())
      case rsp: SeqResultRsp[T, V1] => reply(SeqResultRsp(rsp.key, map(rsp.value)))
    }
  }

  val m: PartialFunction[Any, Unit] = {
    case r: SeqResultRsp[T, V1] => reply(SeqResultRsp(r.key, map(r.value)))
    case r => reply(r)
  }

  override def first(sourceActor: LiteActor)
                    (responseProcess: PartialFunction[Any, Unit]) {
    liteSeq.first(this)(m)
  }

  override def current(sourceActor: LiteActor, key: T)
                      (responseProcess: PartialFunction[Any, Unit]) {
    liteSeq.current(this, key)(m)
  }

  override def next(sourceActor: LiteActor, key: T)
                   (responseProcess: PartialFunction[Any, Unit]) {
    liteSeq.next(this, key)(m)
  }
}
