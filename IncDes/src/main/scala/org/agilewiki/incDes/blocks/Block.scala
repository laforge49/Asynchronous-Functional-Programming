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
package blocks

import blip._

class BlockFactory(id: FactoryId)
  extends IncDesFactory(id) {

  override protected def instantiate = new Block
}

object Block {
  def apply(mailbox: Mailbox) = {
    new BlockFactory(BLOCK_FACTORY_ID).newActor(mailbox).asInstanceOf[Block]
  }
}

class Block extends IncDesIncDes {
  private var readOnly = false
  private var dirty = false
  private var _handle: Handle = null

  bind(classOf[Clean], clean)
  bind(classOf[Process], process)
  bind(classOf[IsQuery], isQuery)

  override def length = if (len == -1) 0 else len

  def setHandle(handle: Handle) {
    if (opened) throw new IllegalStateException
    _handle = handle
  }

  def handle = _handle

  def clean(msg: AnyRef, rf: Any => Unit) {
    dirty = false
    rf(null)
  }

  def process(msg: AnyRef, rf: Any => Unit) {
    val transactionContext = msg.asInstanceOf[Process].transactionContext
    this(Value()) {
      rsp => {
        rsp.asInstanceOf[IncDes](Process(transactionContext))(rf)
      }
    }
  }

  def setReadOnly {
    if (opened) throw new IllegalStateException
    readOnly = true
  }

  override def loadLen(_data: MutableData) = _data.remaining

  override def saveLen(_data: MutableData) {}

  override def skipLen(m: MutableData) {}

  override def changed(transactionContext: TransactionContext, lenDiff: Int, what: IncDes, rf: Any => Unit) {
    data = null
    rf(null)
  }

  override def writable(transactionContext: TransactionContext)(rf: Any => Unit) {
    if (transactionContext != null && transactionContext.isInstanceOf[QueryContext])
      throw new IllegalStateException("QueryContext does not support writable")
    if (readOnly) throw new IllegalStateException("Block is read-only")
    if (transactionContext == null) {
      rf(null)
      return
    }
    if (dirty) {
      rf(null)
      return
    }
    dirty = true
    systemServices(DirtyBlock(this))(rf)
  }

  private def isQuery(msg: AnyRef, rf: Any => Unit) {
    this(Value()) {
      rsp => {
        rsp.asInstanceOf[IncDes](msg)(rf)
      }
    }
  }
}