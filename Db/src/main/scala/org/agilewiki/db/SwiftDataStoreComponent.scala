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
import incDes._
import blocks._

class SwiftDataStoreComponentFactory extends ComponentFactory {
  addDependency(classOf[TransactionProcessorComponentFactory])
  addDependency(classOf[RootBlockComponentFactory])

  override def instantiate(actor: Actor) = new SwiftDataStoreComponent(actor)
}

class SwiftDataStoreComponent(actor: Actor)
  extends Component(actor) {
  var dirty = false
  var rootBlock: Block = null

  bind(classOf[Commit], commit)
  bind(classOf[Abort], abort)
  bind(classOf[DirtyBlock], dirtyBlock)
  bind(classOf[DbRoot], dbRoot)

  private def commit(msg: AnyRef, rf: Any => Unit) {
    if (!dirty) {
      rf(null)
      return
    }
    val chain = new Chain
    chain.op(systemServices, LogInfo(), "tuple")
    chain.op(rootBlock, Value(), "rootMap")
    chain.op(Unit => chain("rootMap"), Unit => {
      val (logFileTimestamp, logFilePosition) = chain("tuple").asInstanceOf[(String, Long)]
      PutLong(null, "logFilePosition", logFilePosition)
    })
    chain.op(Unit => chain("rootMap"), Unit => {
      val (logFileTimestamp, logFilePosition) = chain("tuple").asInstanceOf[(String, Long)]
      PutString(null, "logFileTimestamp", logFileTimestamp)
    })
    chain.op(systemServices, WriteRootBlock(rootBlock))
    chain.op(
      Unit => {
        dirty = false
        rootBlock
      }, Clean())
    actor(chain)(rf)
  }

  private def abort(msg: AnyRef, rf: Any => Unit) {
    if (dirty) {
      rootBlock = null
      dirty = false
    }
    throw msg.asInstanceOf[Abort].exception
  }

  private def dirtyBlock(msg: AnyRef, rf: Any => Unit) {
    dirty = true
    rf(null)
  }

  private def dbRoot(msg: AnyRef, rf: Any => Unit) {
    if (rootBlock != null) rf(rootBlock)
    else systemServices(ReadRootBlock()) {
      rsp => {
        rootBlock = rsp.asInstanceOf[Block]
        rf(rootBlock)
      }
    }
  }
}
