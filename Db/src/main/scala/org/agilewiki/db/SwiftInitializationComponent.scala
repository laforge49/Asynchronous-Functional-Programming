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
import records._

class SwiftInitializationComponentFactory extends ComponentFactory {
  override def instantiate(actor: Actor) = new SwiftInitializationComponent(actor)
}

class SwiftInitializationComponent(actor: Actor)
  extends SmallRecordsInitializationComponent(actor) {

  override protected def recordsPathname = "$/records/"

  override protected def initDb(msg: AnyRef, rf: Any => Unit) {
    val rootBlock = msg.asInstanceOf[InitDb].rootBlock
    val rootMap = IncDesStringIncDesMap(null, systemServices)
    val recordsIncDes = IncDesIncDes(null)
    val records = IncDesStringRecordMap(null, systemServices)
    val chain = new Chain
    chain.op(rootBlock, Set(null, rootMap))
    chain.op(rootMap, PutString(null, "logFileTimestamp", ""))
    chain.op(rootMap, PutLong(null, "logFilePosition", -1L))
    chain.op(rootMap, Put[String, IncDesIncDes, IncDes](null, "records", recordsIncDes))
    chain.op(recordsIncDes, Set(null, records))
    actor(chain){
      rsp => rf(rootBlock)
    }
  }

  override protected def records(msg: AnyRef, rf: Any => Unit) {
    val chain = new Chain
    chain.op(actor, DbRoot(), "root")
    chain.op(Unit => chain("root"), GetValue("records"))
    actor(chain)(rf)
  }
}
