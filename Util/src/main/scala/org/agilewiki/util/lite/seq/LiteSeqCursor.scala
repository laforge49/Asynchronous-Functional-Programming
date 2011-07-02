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

import java.util.Comparator

class LiteSeqCursor[T, V](reactor: LiteReactor, wrappedSeq: SeqActor[T, V])
  extends SeqActor[T, V](reactor)
  with Comparable[LiteSeqCursor[T, V]] {
  var lastResult: SeqResultRsp[T, V] = null
  var _first = false

  override def comparator: Comparator[T] = wrappedSeq.comparator

  private val comp = comparator

  var inited = false

  override def compareTo(o: LiteSeqCursor[T, V]): Int = {
    if (!inited || !o.inited) throw new IllegalStateException
    if (lastResult == null) {
      if (o.lastResult == null) return 0
      return 1
    }
    if (o.lastResult == null) return -1
    return comp.compare(lastResult.key, o.lastResult.key)
  }

  override def equals(o: Any): Boolean = {
    if (!o.isInstanceOf[LiteSeqCursor[T, V]]) throw new IllegalArgumentException(o.toString)
    val other = o.asInstanceOf[LiteSeqCursor[T, V]]
    compareTo(other) == 0
  }

  addRequestHandler{
    case req: SeqFirstReq => _first(req)(back)
    case req: SeqCurrentReq[T] => _current(req)(back)
    case req: SeqNextReq[T] => _next(req)(back)
  }

  protected def _first(req: SeqFirstReq)
                      (responseProcess: PartialFunction[Any, Unit]) {
    if (inited && _first)
      if (lastResult != null) responseProcess(lastResult)
      else responseProcess(SeqEndRsp())
    else {
      inited = true
      wrappedSeq.send(req) {
        case rsp: SeqEndRsp => {
          lastResult = null
          _first = true
          responseProcess(rsp)
        }
        case rsp: SeqResultRsp[T, V] => {
          lastResult = rsp
          _first = true
          responseProcess(rsp)
        }
      }
    }
  }

  protected def _current(req: SeqCurrentReq[T])
                        (responseProcess: PartialFunction[Any, Unit]) {
    val key = req.key
    if (inited && lastResult != null && key == lastResult.key) responseProcess(lastResult)
    else {
      inited = true
      wrappedSeq.send(req) {
        case rsp: SeqEndRsp => {
          lastResult = null
          _first = false
          responseProcess(rsp)
        }
        case rsp: SeqResultRsp[T, V] => {
          lastResult = rsp
          _first = false
          responseProcess(rsp)
        }
      }
    }
  }

  protected def _next(req: SeqNextReq[T])
                     (responseProcess: PartialFunction[Any, Unit]) {
    var key = req.key
    inited = true
    wrappedSeq.send(SeqNextReq(key)) {
      case rsp: SeqEndRsp => {
        lastResult = null
        _first = false
        responseProcess(rsp)
      }
      case rsp: SeqResultRsp[T, V] => {
        lastResult = rsp
        _first = false
        responseProcess(rsp)
      }
    }
  }
}