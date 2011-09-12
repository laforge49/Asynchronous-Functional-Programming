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

class TransactionProcessorComponentFactory extends ComponentFactory {
  override def instantiate(actor: Actor) = new TransactionProcessorComponent(actor)
}

class TransactionProcessorComponent(actor: Actor)
  extends Component(actor) {

  bind(classOf[TransactionRequest], transactionRequest)
  bindSafe(classOf[QueryTransaction], new Query(process))
  bindSafe(classOf[UpdateTransaction], new Update({
    (msg, rf) => exceptionHandler(msg, rf, process) {
      ex => systemServices(Abort(ex))(rf)
    }
  }))

  override def open {
    actor.requiredService(classOf[Commit])
    actor.requiredService(classOf[Abort])
    actor.requiredService(classOf[DirtyBlock])
    actor.requiredService(classOf[DbRoot])
  }

  private def transactionRequest(msg: AnyRef, rf: Any => Unit) {
    val block = msg.asInstanceOf[TransactionRequest].block
    block(Bytes()) {
      rsp0 => {
        val bytes = rsp0.asInstanceOf[Array[Byte]]
        val key = block.key
        block(IsQuery()) {
          rsp1 => {
            if (rsp1.asInstanceOf[Boolean]) actor(new QueryTransaction(key, bytes))(rf)
            else actor(new UpdateTransaction(key, bytes))(rf)
          }
        }
      }
    }
  }

  private def process(msg: AnyRef, rf: Any => Unit) {
    val transaction = msg.asInstanceOf[Transaction]
    var journalEntry: Block = Block(mailbox)
    journalEntry.partness(null, transaction.key, null)
    journalEntry.setSystemServices(actor)
    journalEntry.load(transaction.bytes)
    journalEntry(Process) {
      rsp2 => {
        systemServices(Commit()) {
          rsp3 => {
            rf(rsp2)
          }
        }
      }
    }
  }
}
