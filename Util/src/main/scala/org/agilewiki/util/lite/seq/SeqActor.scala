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

abstract class SeqActor[T, V](reactor: LiteReactor)
  extends LiteActor(reactor)
  with SeqComparator[T] {

  addRequestHandler{
    case req: FoldReq[V] => _fold(null.asInstanceOf[T], req.seed, req.fold)
    case req: ExistsReq[V] => _exists(null.asInstanceOf[T], req.exists)
    case req: FindReq[V] => _find(null.asInstanceOf[T], req.find)
  }

  def first(sourceActor: LiteActor)
           (responseProcess: PartialFunction[Any, Unit]) {
    current(sourceActor, null.asInstanceOf[T])(responseProcess)
  }

  def current(sourceActor: LiteActor, key: T)
             (responseProcess: PartialFunction[Any, Unit]) {
    sourceActor.send(this, SeqCurrentReq(key))(responseProcess)
  }

  def next(sourceActor: LiteActor, key: T)
          (responseProcess: PartialFunction[Any, Unit]) {
    sourceActor.send(this, SeqNextReq(key))(responseProcess)
  }

  def hasKey(sourceActor: LiteActor, key: T)
         (responseProcess: PartialFunction[Any, Unit]) {
    current(sourceActor, key) {
      case rsp: SeqEndRsp => responseProcess(false)
      case rsp: SeqResultRsp[T, V] => responseProcess(rsp.key == key)
    }
  }

  def get(sourceActor: LiteActor, key: T)
         (responseProcess: PartialFunction[Any, Unit]) {
    current(sourceActor, key) {
      case rsp: SeqEndRsp => reply(null.asInstanceOf[T])
      case rsp: SeqResultRsp[T, V] => {
        if (rsp.key == key) responseProcess(rsp.value)
        else responseProcess(null.asInstanceOf[T])
      }
    }
  }

  def exact(sourceActor: LiteActor, key: T)
           (responseProcess: PartialFunction[Any, Unit]) {
    current(sourceActor, key) {
      case rsp: SeqEndRsp => throw new IllegalArgumentException("not present: " + key)
      case rsp: SeqResultRsp[T, V] => {
        if (rsp.key == key) responseProcess(rsp.value)
        else throw new IllegalArgumentException("not present: " + key)
      }
    }
  }

  def fold(sourceActor: LiteActor, seed: V, f: (V, V) => V)
          (responseProcess: PartialFunction[Any, Unit]) {
    sourceActor.send(this, FoldReq(seed, f))(responseProcess)
  }

  protected def _fold(key: T, seed: V, fold: (V, V) => V) {
    if (key == null.asInstanceOf[V])
      current(this, key) {
        case rsp: SeqEndRsp => reply(FoldRsp(seed))
        case rsp: SeqResultRsp[T, V] => _fold(rsp.key, fold(seed, rsp.value), fold)
      }
    else next(this, key) {
      case rsp: SeqEndRsp => reply(FoldRsp(seed))
      case rsp: SeqResultRsp[T, V] => _fold(rsp.key, fold(seed, rsp.value), fold)
    }
  }

  def exists(sourceActor: LiteActor, e: V => Boolean)
            (responseProcess: PartialFunction[Any, Unit]) {
    sourceActor.send(this, ExistsReq(e))(responseProcess)
  }

  protected def _exists(key: T, exists: V => Boolean) {
    if (key == null.asInstanceOf[V])
      current(this, key) {
        case rsp: SeqEndRsp => reply(ExistsRsp(false))
        case rsp: SeqResultRsp[T, V] => {
          if (exists(rsp.value)) reply(ExistsRsp(true))
          else _exists(rsp.key, exists)
        }
      }
    else next(this, key) {
      case rsp: SeqEndRsp => reply(ExistsRsp(false))
      case rsp: SeqResultRsp[T, V] => {
        if (exists(rsp.value)) reply(ExistsRsp(true))
        else _exists(rsp.key, exists)
      }
    }
  }

  def find(sourceActor: LiteActor, f: (V) => Boolean)
          (responseProcess: PartialFunction[Any, Unit]) {
    sourceActor.send(this, FindReq(f))(responseProcess)
  }

  protected def _find(key: T, find: V => Boolean) {
    if (key == null.asInstanceOf[V])
      current(this, key) {
        case rsp: SeqEndRsp => reply(NotFoundRsp())
        case rsp: SeqResultRsp[T, V] => {
          if (find(rsp.value)) reply(FoundRsp(rsp.value))
          else _find(rsp.key, find)
        }
      }
    else next(this, key) {
      case rsp: SeqEndRsp => reply(NotFoundRsp())
      case rsp: SeqResultRsp[T, V] => {
        if (find(rsp.value)) reply(FoundRsp(rsp.value))
        else _find(rsp.key, find)
      }
    }
  }

  def mapActor[V2](map: V => V2): SeqActor[T, V2] =
    new LiteMapFunc(this, map)

  def mapActor[V2](map: SeqActor[V, V2]): SeqActor[T, V2] =
    new LiteMapSeq(reactor, this, map)

  def filterActor(filter: V => Boolean): SeqActor[T, V] =
    new LiteFilterFunc(reactor, this, filter)

  def filterActor[V1](filter: SeqActor[V, V1]): SeqActor[T, V] =
    new LiteFilterSeq(reactor, this, filter)

  def flatMapActor[V2](map: V => V2): SeqActor[T, V2] = {
    val ms = mapActor(map)
    ms.filterActor((x: V2) => x != null.asInstanceOf[V])
  }

  def flatMapActor[V2](seq: SeqActor[V, V2]): SeqActor[T, V2] = {
    val ms = mapActor(seq)
    ms.filterActor((x: V2) => x != null.asInstanceOf[V])
  }
}


class SeqExtensionActor[T, V](reactor: LiteReactor, seq: SeqExtension[T, V])
  extends SeqActor[T, V](reactor) {

  addExtension(seq)

  def seqExtension = seq

  override def comparator = seq.comparator

  override def current(sourceActor: LiteActor, key: T)
                      (responseProcess: PartialFunction[Any, Unit]) {
    if (isSafe(sourceActor, this)) {
      currentReactor(sourceActor.currentReactor)
      responseProcess(seq.current(key))
    }
    else sourceActor.send(this, SeqCurrentReq(key))(responseProcess)
  }

  override def next(sourceActor: LiteActor, key: T)
                   (responseProcess: PartialFunction[Any, Unit]) {
    if (isSafe(sourceActor, this)) {
      currentReactor(sourceActor.currentReactor)
      responseProcess(seq.next(key))
    }
    else sourceActor.send(this, SeqNextReq(key))(responseProcess)
  }

  override def fold(sourceActor: LiteActor, seed: V, f: (V, V) => V)
                   (responseProcess: PartialFunction[Any, Unit]) {
    if (isSafe(sourceActor, this)) {
      currentReactor(sourceActor.currentReactor)
      responseProcess(seq._fold(null.asInstanceOf[T], seed, f))
    }
    else sourceActor.send(this, FoldReq(seed, f))(responseProcess)
  }

  override protected def _fold(key: T, seed: V, fold: (V, V) => V) {
    reply(seq._fold(null.asInstanceOf[T], seed, fold))
  }

  override def exists(sourceActor: LiteActor, e: V => Boolean)
                     (responseProcess: PartialFunction[Any, Unit]) {
    if (isSafe(sourceActor, this)) {
      currentReactor(sourceActor.currentReactor)
      responseProcess(seq._exists(null.asInstanceOf[T], e))
    }
    else sourceActor.send(this, ExistsReq(e))(responseProcess)
  }

  override protected def _exists(key: T, exists: V => Boolean) {
    reply(seq._exists(null.asInstanceOf[T], exists))
  }

  override def find(sourceActor: LiteActor, f: V => Boolean)
                   (responseProcess: PartialFunction[Any, Unit]) {
    if (isSafe(sourceActor, this)) {
      currentReactor(sourceActor.currentReactor)
      responseProcess(seq._find(null.asInstanceOf[T], f))
    }
    else sourceActor.send(this, FindReq(f))(responseProcess)
  }

  override protected def _find(key: T, find: V => Boolean) {
    reply(seq._find(null.asInstanceOf[T], find))
  }

  override def filterActor(filter: V => Boolean) =
    new LiteExtensionFilterSeq(currentReactor, this, filter)
}
