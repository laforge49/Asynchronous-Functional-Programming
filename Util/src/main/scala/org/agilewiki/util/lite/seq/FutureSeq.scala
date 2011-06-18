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

case class FutureSeq[T, V](actor: SeqActor[T, V])
  extends LiteFuture {

  def isEmpty: Boolean = {
    send(actor, SeqFirstReq(), new LiteReactor)
    val rsp = get
    if (rsp.isInstanceOf[SeqEndRsp]) return true
    if (rsp.isInstanceOf[SeqResultRsp[T, V]]) return false
    throw new UnsupportedOperationException(rsp.toString)
  }

  def firstKey: T = {
    send(actor, SeqFirstReq(), new LiteReactor)
    val rsp = get
    if (rsp.isInstanceOf[SeqEndRsp]) return null.asInstanceOf[T]
    if (rsp.isInstanceOf[SeqResultRsp[T, V]]) return rsp.asInstanceOf[SeqResultRsp[T, V]].key
    throw new UnsupportedOperationException(rsp.toString)
  }

  def first: V = {
    send(actor, SeqFirstReq(), new LiteReactor)
    val rsp = get
    if (rsp.isInstanceOf[SeqEndRsp]) return null.asInstanceOf[V]
    if (rsp.isInstanceOf[SeqResultRsp[T, V]]) return rsp.asInstanceOf[SeqResultRsp[T, V]].value
    throw new UnsupportedOperationException(rsp.toString)
  }

  def firstMatch(expectedKey: T, expectedValue: V): Boolean = {
    send(actor, SeqFirstReq(), new LiteReactor)
    val rsp = get
    if (rsp.isInstanceOf[SeqEndRsp]) return false
    if (rsp.isInstanceOf[SeqResultRsp[T, V]]) {
      val result = rsp.asInstanceOf[SeqResultRsp[T, V]]
      return result.key == expectedKey && result.value == expectedValue
    }
    throw new UnsupportedOperationException(rsp.toString)
  }

  def currentKey(k: T): T = {
    send(actor, SeqCurrentReq[T](k), new LiteReactor)
    val rsp = get
    if (rsp.isInstanceOf[SeqEndRsp]) return null.asInstanceOf[T]
    if (rsp.isInstanceOf[SeqResultRsp[T, V]]) return rsp.asInstanceOf[SeqResultRsp[T, V]].key
    throw new UnsupportedOperationException(rsp.toString)
  }

  def current(k: T): V = {
    send(actor, SeqCurrentReq[T](k), new LiteReactor)
    val rsp = get
    if (rsp.isInstanceOf[SeqEndRsp]) return null.asInstanceOf[V]
    if (rsp.isInstanceOf[SeqResultRsp[T, V]]) return rsp.asInstanceOf[SeqResultRsp[T, V]].value
    throw new UnsupportedOperationException(rsp.toString)
  }

  def currentMatch(k: T, expectedKey: T, expectedValue: V): Boolean = {
    send(actor, SeqCurrentReq[T](k), new LiteReactor)
    val rsp = get
    if (rsp.isInstanceOf[SeqEndRsp]) return false
    if (rsp.isInstanceOf[SeqResultRsp[T, V]]) {
      val result = rsp.asInstanceOf[SeqResultRsp[T, V]]
      return result.key == expectedKey && result.value == expectedValue
    }
    throw new UnsupportedOperationException(rsp.toString)
  }

  def currentPair(k: T): (T, V) = {
    send(actor, SeqCurrentReq[T](k), new LiteReactor)
    val rsp = get
    if (rsp.isInstanceOf[SeqEndRsp]) return (k, null.asInstanceOf[V])
    if (rsp.isInstanceOf[SeqResultRsp[T, V]]) {
      val result = rsp.asInstanceOf[SeqResultRsp[T, V]]
      return (result.key, result.value)
    }
    throw new UnsupportedOperationException(rsp.toString)
  }

  def isCurrentEnd(k: T): Boolean = {
    send(actor, SeqCurrentReq[T](k), new LiteReactor)
    val rsp = get
    if (rsp.isInstanceOf[SeqEndRsp]) return true
    if (rsp.isInstanceOf[SeqResultRsp[T, V]]) return false
    throw new UnsupportedOperationException(rsp.toString)
  }

  def nextKey(k: T): T = {
    send(actor, SeqNextReq[T](k), new LiteReactor)
    val rsp = get
    if (rsp.isInstanceOf[SeqEndRsp]) return null.asInstanceOf[T]
    if (rsp.isInstanceOf[SeqResultRsp[T, V]]) return rsp.asInstanceOf[SeqResultRsp[T, V]].key
    throw new UnsupportedOperationException(rsp.toString)
  }

  def next(k: T): V = {
    send(actor, SeqNextReq[T](k), new LiteReactor)
    val rsp = get
    if (rsp.isInstanceOf[SeqEndRsp]) return null.asInstanceOf[V]
    if (rsp.isInstanceOf[SeqResultRsp[T, V]]) return rsp.asInstanceOf[SeqResultRsp[T, V]].value
    throw new UnsupportedOperationException(rsp.toString)
  }

  def nextMatch(k: T, expectedKey: T, expectedValue: V): Boolean = {
    send(actor, SeqNextReq[T](k), new LiteReactor)
    val rsp = get
    if (rsp.isInstanceOf[SeqEndRsp]) return false
    if (rsp.isInstanceOf[SeqResultRsp[T, V]]) {
      val result = rsp.asInstanceOf[SeqResultRsp[T, V]]
      return result.key == expectedKey && result.value == expectedValue
    }
    throw new UnsupportedOperationException(rsp.toString)
  }

  def nextPair(k: T): (T, V) = {
    send(actor, SeqNextReq[T](k), new LiteReactor)
    val rsp = get
    if (rsp.isInstanceOf[SeqEndRsp]) return (k, null.asInstanceOf[V])
    if (rsp.isInstanceOf[SeqResultRsp[T, V]]) {
      val result = rsp.asInstanceOf[SeqResultRsp[T, V]]
      return (result.key, result.value)
    }
    throw new UnsupportedOperationException(rsp.toString)
  }

  def isNextEnd(k: T): Boolean = {
    send(actor, SeqNextReq[T](k), new LiteReactor)
    val rsp = get
    if (rsp.isInstanceOf[SeqEndRsp]) return true
    if (rsp.isInstanceOf[SeqResultRsp[T, V]]) return false
    throw new UnsupportedOperationException(rsp.toString)
  }

  def fold(seed: V, f: (V, V) => V): V = {
    send(actor, FoldReq(seed, f), new LiteReactor)
    val rsp = get
    if (rsp.isInstanceOf[FoldRsp[V]]) return rsp.asInstanceOf[FoldRsp[V]].result
    throw new UnsupportedOperationException(rsp.toString)
  }

  def foldMatch(seed: V, f: (V, V) => V, expectedValue: V): Boolean = {
    return fold(seed, f) == expectedValue
  }

  def exists(f: V => Boolean): Boolean = {
    send(actor, ExistsReq(f), new LiteReactor)
    val rsp = get
    if (rsp.isInstanceOf[ExistsRsp]) return rsp.asInstanceOf[ExistsRsp].result
    throw new UnsupportedOperationException(rsp.toString)
  }

  def find(f: V => Boolean): V = {
    send(actor, FindReq(f), new LiteReactor)
    val rsp = get
    if (rsp.isInstanceOf[FindRsp[V]]) return rsp.asInstanceOf[FindRsp[V]].result
    throw new UnsupportedOperationException(rsp.toString)
  }

  def findMatch(f: V => Boolean, expectedValue: V) {
    return find(f) == expectedValue
  }
}
