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

class LiteMapSeq[T, V1, V2](liteSeq: SeqActor[T, V1], mapSeq: SeqActor[V1, V2])
  extends SeqActor[T, V2](liteSeq.liteReactor) {
  override def comparator = liteSeq.comparator

  bind(classOf[SeqFirstReq], _map)
  bind(classOf[SeqCurrentReq[T]], _map)
  bind(classOf[SeqNextReq[T]], _map)

  protected def _map(msg: AnyRef, responseProcess: PartialFunction[Any, Unit]) {
    val req = msg.asInstanceOf[SeqReq]
    liteSeq.send(req)(m(responseProcess))
  }

  def m(responseProcess: PartialFunction[Any, Unit]): PartialFunction[Any, Unit] = {
    case r: SeqResultRsp[T, V1] => {
      mapSeq.get(r.value) {
        case mr => {
          responseProcess(SeqResultRsp(r.key, mr))
        }
      }
    }
    case r => responseProcess(r)
  }
}

class LiteMapFunc[T, V1, V2](liteSeq: SeqActor[T, V1], map: V1 => V2)
  extends SeqActor[T, V2](liteSeq.liteReactor) {
  override def comparator = liteSeq.comparator

  bind(classOf[SeqFirstReq], _map)
  bind(classOf[SeqCurrentReq[T]], _map)
  bind(classOf[SeqNextReq[T]], _map)

  protected def _map(msg: AnyRef, responseProcess: PartialFunction[Any, Unit]) {
    val req = msg.asInstanceOf[SeqReq]
    liteSeq.send(req)(m(responseProcess))
  }

  def m(responseProcess: PartialFunction[Any, Unit]): PartialFunction[Any, Unit] = {
    case r: SeqResultRsp[T, V1] => responseProcess(SeqResultRsp(r.key, map(r.value)))
    case r => responseProcess(r)
  }
}
