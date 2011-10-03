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
package db
package log

import blip._
import seq._
import incDes._
import blocks._

class TransactionsSeq(pathname: String, jeMailbox: Mailbox)
  extends Sequence[Long, Block] {
  private var kvPair: KVPair[Long, Block] = _
  private var reader: java.io.DataInputStream = _

  setMailbox(new Mailbox)

  private def read {
    var timestamp = 0L
    var len = 0
    var bytes: Array[Byte] = null
    try {
      timestamp = reader.readLong
      len = reader.readInt
      bytes = new Array[Byte](len)
      reader.readFully(bytes)
    } catch {
      case ex: java.io.EOFException => {
        kvPair = null
        return
      }
      case ex: Exception => {
        println(ex)
        kvPair = null
        return
      }
    }
    var block = Block(jeMailbox)
    block.setSystemServices(systemServices)
    block.partness(null, timestamp, null)
    block.load(bytes)
    kvPair = new KVPair(timestamp, block)
  }

  def init {
    reader = new java.io.DataInputStream(new java.io.FileInputStream(pathname))
  }

  override def first(msg: AnyRef, rf: Any => Unit) {
    if (kvPair != null) throw new IllegalStateException
    init
    read
    rf(kvPair)
  }

  override def current(msg: AnyRef, rf: Any => Unit) {
    val key = msg.asInstanceOf[Current[Long]].key
    if (kvPair == null) {
      if (key != 0L) throw new IllegalStateException
      init
      read
    } else if (kvPair.key != key) throw new IllegalStateException
    rf(kvPair)
  }

  override def next(msg: AnyRef, rf: Any => Unit) {
    val key = msg.asInstanceOf[Next[Long]].key
    if (kvPair == null) {
      if (key != 0L) throw new IllegalStateException
      init
    } else if (kvPair.key != key) throw new IllegalStateException
    read
    rf(kvPair)
  }
}