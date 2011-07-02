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

abstract class SeqActor[T, V](reactor: LiteReactor)
  extends LiteActor(reactor, null)
  with SeqComparator[T] {

  def first(responseProcess: PartialFunction[Any, Unit])
           (implicit sourceActor: ActiveActor) {
    send(SeqFirstReq())(responseProcess)(sourceActor)
  }

  def current(key: T)
             (responseProcess: PartialFunction[Any, Unit])
             (implicit sourceActor: ActiveActor) {
    send(SeqCurrentReq(key))(responseProcess)(sourceActor)
  }

  def next(key: T)
          (responseProcess: PartialFunction[Any, Unit])
          (implicit sourceActor: ActiveActor) {
    send(SeqNextReq(key))(responseProcess)(sourceActor)
  }

  def hasKey(key: T)
            (responseProcess: PartialFunction[Any, Unit])
            (implicit sourceActor: ActiveActor) {
    current(key)({
      case rsp: SeqEndRsp => responseProcess(false)
      case rsp: SeqResultRsp[T, V] => responseProcess(rsp.key == key)
    })(sourceActor)
  }

  def get(key: T)
         (responseProcess: PartialFunction[Any, Unit])
         (implicit sourceActor: ActiveActor) {
    current(key)({
      case rsp: SeqEndRsp => reply(null.asInstanceOf[T])
      case rsp: SeqResultRsp[T, V] => {
        if (rsp.key == key) responseProcess(rsp.value)
        else responseProcess(null.asInstanceOf[T])
      }
    })(sourceActor)
  }

  def exact(key: T)
           (responseProcess: PartialFunction[Any, Unit])
           (implicit sourceActor: ActiveActor) {
    current(key)({
      case rsp: SeqEndRsp => throw new IllegalArgumentException("not present: " + key)
      case rsp: SeqResultRsp[T, V] => {
        if (rsp.key == key) responseProcess(rsp.value)
        else throw new IllegalArgumentException("not present: " + key)
      }
    })(sourceActor)
  }

  addRequestHandler{
    case req: FoldReq[V] => _fold(req)(back)
    case req: ExistsReq[V] => _exists(req)(back)
    case req: FindReq[V] => _find(req.find)
  }

  def fold(seed: V, f: (V, V) => V)
          (responseProcess: PartialFunction[Any, Unit])
          (implicit sourceActor: ActiveActor) {
    send(FoldReq(seed, f))(responseProcess)(sourceActor)
  }

  private def _fold(req: FoldReq[V])
                   (responseProcess: PartialFunction[Any, Unit]) {
    first{
      case rsp: SeqEndRsp => responseProcess(FoldRsp(req.seed))
      case rsp: SeqResultRsp[T, V] =>
        _foldNext(rsp.key, req.fold(req.seed, rsp.value), req.fold)(responseProcess)
    }
  }

  private def _ifoldNext(key: T, seed: V, fold: (V, V) => V)
                        (responseProcess: PartialFunction[Any, Unit]) {
    _foldNext(key, seed, fold)(responseProcess)
  }

  @tailrec private def _foldNext(key: T, seed: V, fold: (V, V) => V)
                                (responseProcess: PartialFunction[Any, Unit]) {
    var async = false
    var sync = false
    var nextKey = null.asInstanceOf[T]
    var nextValue = null.asInstanceOf[V]
    next(key) {
      case rsp: SeqEndRsp => responseProcess(FoldRsp(seed))
      case rsp: SeqResultRsp[T, V] => {
        if (async) _ifoldNext(rsp.key, fold(seed, rsp.value), fold)(responseProcess)
        else {
          sync = true
          nextKey = rsp.key
          nextValue = rsp.value
        }
      }
    }
    if (!sync) {
      async = true
      return
    }
    _foldNext(nextKey, fold(seed, nextValue), fold)(responseProcess)
  }

  def exists(e: V => Boolean)
            (responseProcess: PartialFunction[Any, Unit])
            (implicit sourceActor: ActiveActor) {
    send(ExistsReq(e))(responseProcess)(sourceActor)
  }

  private def _exists(req: ExistsReq[V])
                     (responseProcess: PartialFunction[Any, Unit]) {
    val exists = req.exists
    first{
      case rsp: SeqEndRsp => responseProcess(ExistsRsp(false))
      case rsp: SeqResultRsp[T, V] => {
        if (exists(rsp.value)) responseProcess(ExistsRsp(true))
        else _existsNext(rsp.key, exists)(responseProcess)
      }
    }
  }

  private def _iexistsNext(key: T, exists: V => Boolean)
                     (responseProcess: PartialFunction[Any, Unit]) {
    _existsNext(key, exists)(responseProcess)
  }

  @tailrec private def _existsNext(key: T, exists: V => Boolean)
                     (responseProcess: PartialFunction[Any, Unit]) {
    var async = false
    var sync = false
    var nextKey = null.asInstanceOf[T]
    next(key) {
      case rsp: SeqEndRsp => responseProcess(ExistsRsp(false))
      case rsp: SeqResultRsp[T, V] => {
        if (exists(rsp.value)) responseProcess(ExistsRsp(true))
        else if (async) _iexistsNext(rsp.key, exists)(responseProcess)
        else {
          sync = true
          nextKey = rsp.key
        }
      }
    }
    if (!sync) {
      async = true
      return
    }
    _existsNext(nextKey, exists)(responseProcess)
  }

  def find(f: (V) => Boolean)
          (responseProcess: PartialFunction[Any, Unit])
          (implicit sourceActor: ActiveActor) {
    send(FindReq(f))(responseProcess)(sourceActor)
  }

  private def _find(find: V => Boolean) {
    first{
      case rsp: SeqEndRsp => reply(NotFoundRsp())
      case rsp: SeqResultRsp[T, V] => {
        if (find(rsp.value)) reply(FoundRsp(rsp.value))
        else _findNext(rsp.key, find)
      }
    }
  }

  private def _ifindNext(key: T, find: V => Boolean) {
    _findNext(key, find)
  }

  @tailrec private def _findNext(key: T, find: V => Boolean) {
    var async = false
    var sync = false
    var nextKey = null.asInstanceOf[T]
    next(key) {
      case rsp: SeqEndRsp => reply(NotFoundRsp())
      case rsp: SeqResultRsp[T, V] => {
        if (find(rsp.value)) reply(FoundRsp(rsp.value))
        else if (async) _ifindNext(rsp.key, find)
        else {
          sync = true
          nextKey = rsp.key
        }
      }
    }
    if (!sync) {
      async = true
      return
    }
    _findNext(nextKey, find)
  }

  def map[V2](_map: V => V2): SeqActor[T, V2] =
    new LiteMapFunc(this, _map)

  def map[V2](_map: SeqActor[V, V2]): SeqActor[T, V2] =
    new LiteMapSeq(this, _map)

  def filter(_filter: V => Boolean): SeqActor[T, V] =
    new LiteFilterFunc(this, _filter)

  def filter[V1](_filter: SeqActor[V, V1]): SeqActor[T, V] =
    new LiteFilterSeq(this, _filter)

  def flatMap[V2](_map: V => V2): SeqActor[T, V2] = {
    val ms = map(_map)
    ms.filter((x: V2) => x != null.asInstanceOf[V])
  }

  def flatMap[V2](seq: SeqActor[V, V2]): SeqActor[T, V2] = {
    val ms = map(seq)
    ms.filter((x: V2) => x != null.asInstanceOf[V])
  }

  def tail(start: T): SeqActor[T, V] =
    new LiteTailSeq(this, start)

  def head(limit: T): SeqActor[T, V] =
    new LiteHeadSeq(this, limit)
}


class SeqExtensionActor[T, V](reactor: LiteReactor, seq: SeqExtension[T, V])
  extends SeqActor[T, V](reactor) {

  addExtension(seq)

  def seqExtension = seq

  override def comparator = seq.comparator

  override def first(responseProcess: PartialFunction[Any, Unit])
                    (implicit src: ActiveActor) {
    if (isSafe(src)) responseProcess(seq._first)
    else send(SeqFirstReq())(responseProcess)(src)
  }

  override def current(key: T)
                      (responseProcess: PartialFunction[Any, Unit])
                      (implicit src: ActiveActor) {
    if (isSafe(src)) responseProcess(seq._current(key))
    else send(SeqCurrentReq(key))(responseProcess)(src)
  }

  override def next(key: T)
                   (responseProcess: PartialFunction[Any, Unit])
                   (implicit src: ActiveActor) {
    if (isSafe(src)) responseProcess(seq._next(key))
    else send(SeqNextReq(key))(responseProcess)(src)
  }

  override def tail(start: T) =
    new LiteExtensionTailSeq(this, start)

  override def head(limit: T) =
    new LiteExtensionHeadSeq(this, limit)
}
