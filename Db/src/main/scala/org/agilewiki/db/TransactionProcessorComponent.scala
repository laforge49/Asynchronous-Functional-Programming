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

import blip._
import bind._
import services._
import incDes._
import blocks._


class TransactionProcessorComponentFactory extends ComponentFactory {
  addDependency(classOf[TimestampComponentFactory])
  addDependency(classOf[FactoryRegistryComponentFactory])
  addDependency(classOf[BlocksComponentFactory])

  override def instantiate(actor: Actor) = new TransactionProcessorComponent(actor)
}

class TransactionProcessorComponent(actor: Actor)
  extends Component(actor) {

  bind(classOf[TransactionRequest], transactionRequest)
  bindMessageLogic(classOf[QueryTransaction], new Query(process))
  bindMessageLogic(classOf[UpdateTransaction], new Update({
    (msg, rf) => exceptionHandler(msg, rf, process) {
      (ex, mailbox) => {
        systemServices(Abort(ex))(rf)
      }
    }
  }))

  override def open {
    actor.requiredService(classOf[Commit])
    actor.requiredService(classOf[Abort])
    actor.requiredService(classOf[DirtyBlock])
    actor.requiredService(classOf[DbRoot])
    actor.requiredService(classOf[LogTransaction])
  }

  private def transactionRequest(msg: AnyRef, rf: Any => Unit) {
    val request = msg.asInstanceOf[TransactionRequest].request
    var block = Block(null)
    block.setSystemServices(actor.systemServices)
    var timestamp = 0L
    var bytes: Array[Byte] = null
    val results = new Results
    val chain = new Chain(results)
    chain.op(block, Set(null, request))
    chain.op(systemServices, GetTimestamp(), "timestamp")
    chain.op(block, Bytes(), "bytes")
    chain.op(Unit => {
      timestamp = results("timestamp").asInstanceOf[Long]
      bytes = results("bytes").asInstanceOf[Array[Byte]]
      block = Block(exchangeMessenger)
      block.partness(null, timestamp, null)
      block.setSystemServices(actor.systemServices)
      block.load(bytes)
      block
    }, IsQuery(), "isQuery")
    actor(chain) {
      rsp => {
        val isQuery = results("isQuery").asInstanceOf[Boolean]
        if (isQuery) actor(new QueryTransaction(block))(rf)
        else {
          actor(new UpdateTransaction(timestamp, block)) {
            rsp1 => {
              systemServices(LogTransaction(timestamp, bytes)) {
                rsp2 => {
                  systemServices(Commit(timestamp, block)) {
                    rsp3 => {
                      rf(timestamp)
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  private def process(msg: AnyRef, rf: Any => Unit) {
    val req = msg.asInstanceOf[Transaction]
    val block = req.block
    val tc = exchangeMessenger.curReq.asInstanceOf[MailboxReq].transactionContext
    if (req.isInstanceOf[UpdateTransaction]) {
      val ts = req.asInstanceOf[UpdateTransaction].timestamp
      val utc = tc.asInstanceOf[UpdateContext]
      utc.timestamp = ts
    }
    block(Process(tc))(rf)
  }
}
