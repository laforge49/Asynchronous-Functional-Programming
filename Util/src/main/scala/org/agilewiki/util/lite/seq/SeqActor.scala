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

  def first(sourceActor: LiteActor)
           (responseProcess: PartialFunction[Any, Unit]) {
    sourceActor.send(this, SeqFirstReq())(responseProcess)
  }

  def current(sourceActor: LiteActor, key: T)
             (responseProcess: PartialFunction[Any, Unit]) {
    sourceActor.send(this, SeqCurrentReq(key))(responseProcess)
  }

  def next(sourceActor: LiteActor, key: T)
          (responseProcess: PartialFunction[Any, Unit]) {
    sourceActor.send(this, SeqNextReq(key))(responseProcess)
  }

  def mapActor[V2](map: V => V2): SeqActor[T, V2] =
    new LiteMapSeq(reactor, this, map)

  def filterActor(filter: V => Boolean): SeqActor[T, V] =
    new LiteFilterSeq(reactor, this, filter)
}


class SeqExtensionActor[T, V](reactor: LiteReactor, seq: SeqExtension[T])
  extends SeqActor[T, V](reactor) {

  requestHandlerExtension(seq)

  override def comparator = seq.comparator

  override def mapActor[V2](map: V => V2): SeqActor[T, V2] =
    seq.mapActor(map)

  def mapExtension[V2](map: V => V2): SeqExtension[T] =
    seq.mapExtension(map)

  override def filterActor(filter: V => Boolean): SeqActor[T, V] =
    seq.filterActor(filter)

  def filterExtension(filter: V => Boolean): SeqExtension[T] =
    seq.filterExtension(filter)
}
