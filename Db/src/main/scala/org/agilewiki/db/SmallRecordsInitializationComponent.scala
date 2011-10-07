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
import seq._
import incDes._
import blocks._
import records._

class SmallRecordsInitializationComponentFactory extends ComponentFactory {
  override def instantiate(actor: Actor) = new SmallRecordsInitializationComponent(actor)
}

class SmallRecordsInitializationComponent(actor: Actor)
  extends Component(actor) {

  bind(classOf[InitDb], initDb)
  bind(classOf[GetRecord], getRecord)
  bind(classOf[AssignRecord], assignRecord)

  override def open {
    actor.requiredService(classOf[DbRoot])
  }

  private def initDb(msg: AnyRef, rf: Any => Unit) {
    val rootBlock = msg.asInstanceOf[InitDb].rootBlock
    val records = IncDesStringRecordMap(null, systemServices)
    rootBlock(Set(null, records)) {
      rsp => rf(rootBlock)
    }
  }

  private def getRecord(msg: AnyRef, rf: Any => Unit) {
    val req = msg.asInstanceOf[GetRecord]
    val recordKey = req.recordKey
    val chain = new Chain
    chain.op(actor, DbRoot(), "root")
    chain.op(Unit => chain("root"), Value(), "records")
    chain.op(Unit => chain("records"), Get(recordKey))
    actor(chain)(rf)
  }

  private def assignRecord(msg: AnyRef, rf: Any => Unit) {
    val req = msg.asInstanceOf[AssignRecord]
    val tc = req.transactionContext
    val recordKey = req.recordKey
    val value = req.value
    val chain = new Chain
    chain.op(actor, DbRoot(), "root")
    chain.op(Unit => chain("root"), Value(), "records")
    chain.op(Unit => chain("records"), Assign(tc, recordKey, value))
    actor(chain)(rf)
  }
}
