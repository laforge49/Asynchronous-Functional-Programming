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
package incDes

import blip._

class IncDes extends Actor {
  protected var data: ImmutableData = _
  private var _container: IncDes = _
  protected var _key: Any = null
  bind(classOf[Length], _length)
  bind(classOf[Bytes], _bytes)
  bind(classOf[Save], _save)
  bind(classOf[Copy], _copy)
  bind(classOf[Resolve], resolve)

  def load(_data: MutableData) {
    if (isOpen) throw new IllegalStateException
    data = _data.immutable
  }

  def load(bytes: Array[Byte]) {
    val _data = new MutableData(bytes, 0)
    load(_data)
  }

  def key = _key

  def partness(container: IncDes, key: Any, visibleContainer: IncDes) {
    if (container == this) throw new IllegalArgumentException
    _container = container
    _key = key
  }

  def resolve(msg: AnyRef, rf: Any => Unit) {
    val pathname = msg.asInstanceOf[Resolve].pathname
    if (pathname.length > 0) throw new IllegalArgumentException("Invalid pathname")
    rf((this, ""))
  }

  def _length(msg: Any, rf: Any => Unit) {
    rf(length)
  }

  def _bytes(msg: Any, rf: Any => Unit) {
    rf(bytes)
  }

  def _save(msg: Any, rf: Any => Unit) {
    val data = msg.asInstanceOf[Save].data
    save(data)
    rf(null)
  }

  def _copy(msg: Any, rf: Any => Unit) {
    val c = factory.newActor(msg.asInstanceOf[Copy].mailbox).asInstanceOf[IncDes]
    c.setSystemServices(systemServices)
    c.load(bytes)
    rf(c)
  }

  def container = _container

  def clearContainer {
    this._container = null
  }

  def writable(transactionContext: TransactionContext)(rf: Any => Unit) {
    if (container == null) {
      if (transactionContext != null && transactionContext.isInstanceOf[QueryContext])
        throw new IllegalStateException("QueryContext does not support writable")
      rf(null)
    }
    else container.writable(transactionContext)(rf)
  }

  def change(transactionContext: TransactionContext, lenDiff: Int, what: IncDes, rf: Any => Unit) {
    changed(transactionContext, lenDiff, what, rf)
  }

  def changed(transactionContext: TransactionContext, lenDiff: Int, what: IncDes, rf: Any => Unit) {
    data = null
    if (container == null) rf(null)
    else container.change(transactionContext, lenDiff, what, rf)
  }

  def bytes = {
    val bytes = new Array[Byte](length)
    val jmc = new MutableData(bytes, 0)
    save(jmc)
    bytes
  }

  protected def isSerialized = data != null

  def length = 0

  def isDeserialized = true

  protected def serialize(_data: MutableData) {}

  def save(_data: MutableData) {
    if (isSerialized) {
      val ic = _data.immutable
      _data.write(data, length)
      data = ic
    } else {
      data = _data.immutable
      serialize(_data)
    }
    if (data.offset + length != _data.offset) {
      System.err.println(getClass.getName)
      System.err.println("" + data.offset + " + " + length + " != " + _data.offset)
      throw new IllegalStateException
    }
  }
}
