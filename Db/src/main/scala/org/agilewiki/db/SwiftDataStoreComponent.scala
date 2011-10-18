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
import log._
import seq._
import services._

class SwiftDataStoreComponentFactory extends ComponentFactory {
  addDependency(classOf[TransactionProcessorComponentFactory])
  addDependency(classOf[RootBlockComponentFactory])

  override def instantiate(actor: Actor) = new SwiftDataStoreComponent(actor)
}

class SwiftDataStoreComponent(actor: Actor)
  extends Component(actor) {
  var dirty = false
  var init = true
  var rootBlock: Block = null
  var logDirPathname: String = null
  var commitsPerWrite = 1
  var commitCounter = 1
  var updates = new java.util.TreeMap[Long, Block]
  lazy val updatesSeq = {
    val seq = new NavMapSeq(updates)
    seq.setMailbox(mailbox)
    seq.setSystemServices(systemServices)
    seq
  }

  bind(classOf[Commit], commit)
  bind(classOf[Abort], abort)
  bind(classOf[DirtyBlock], dirtyBlock)
  bind(classOf[DbRoot], dbRoot)

  override def open {
    super.open
    logDirPathname = GetProperty.required("logDirPathname")
    commitsPerWrite = GetProperty.int("commitsPerWrite", 100)
    commitCounter = commitsPerWrite
  }

  private def commit(msg: AnyRef, rf: Any => Unit) {
    if (!dirty) {
      rf(null)
      return
    }
    if (commitCounter < commitsPerWrite) {
      val req = msg.asInstanceOf[Commit]
      val timestamp = req.timestamp
      val update = req.update
      updates.put(timestamp, update)
      commitCounter += 1
      dirty = false
      rf(null)
      return
    }
    updates.clear()
    commitCounter = 1
    val chain = new Chain
    chain.op(actor, updateRootBlock)
    chain.op(actor, WriteRootBlock(rootBlock))
    actor(chain) {
      rsp => {
        dirty = false
        rootBlock(Clean())(rf)
      }
    }
  }

  private def updateRootBlock = {
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
    chain
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
    if (rootBlock != null) {
      rf(rootBlock)
      return
    }
    val chain = new Chain
    chain.op(actor, ReadRootBlock(), "rootBlock")
    chain.op(Unit => chain("rootBlock"), Value(), "rootMap")
    chain.op(Unit => chain("rootMap"), GetValue2("logFileTimestamp"), "logFileTimestamp")
    chain.op(Unit => chain("rootMap"), GetValue2("logFilePosition"), "logFilePosition")
    actor(chain) {
      rsp => {
        rootBlock = chain.results("rootBlock").asInstanceOf[Block]
        val rootMap = chain.results("rootMap").asInstanceOf[IncDes]
        val logFileTimestamp = chain.results("logFileTimestamp").asInstanceOf[String]
        val logFilePosition = chain.results("logFilePosition").asInstanceOf[Long]
        if (!init) restore(rootMap, rf)
        else if (logFilePosition == -1) initialize(rootMap, rf)
        else recover(rootMap, logFileTimestamp, logFilePosition, rf)
      }
    }
  }

  private def initialize(rootMap: IncDes, rf: Any => Unit) {
    init = false
    actor(updateRootBlock) {
      rsp => rf(rootBlock)
    }
  }

  private def restore(rootMap: IncDes, rf: Any => Unit) {
    if (updates.isEmpty) rf(rootBlock)
    else updatesSeq(LoopSafe(JnlsSafe)) {
      rsp => initialize(rootMap, rf)
    }
  }

  private def recover(rootMap: IncDes, logFileTimestamp: String, logFilePosition: Long, rf: Any => Unit) {
    val dir = new java.io.File(logDirPathname)
    val fileName = dir.getCanonicalPath + java.io.File.separator + logFileTimestamp + ".jnl"
    val logFile = new java.io.File(fileName)
    if (!logFile.exists) throw new IllegalStateException("no such log file: " + fileName)
    val logFileLength = logFile.length
    if(logFileLength == logFilePosition) {
      initialize(rootMap, rf)
      return
    }
    if (logFileLength < logFilePosition) throw new IllegalStateException("position is beyond eof: "+fileName)
    val inputStream = new java.io.FileInputStream(logFile)
    var rem = logFilePosition
    while (rem > 0) {
      rem -= inputStream.skip(rem)
    }
    val reader = new java.io.DataInputStream(inputStream)
    val seq = new TransactionsSeq(reader, mailbox)
    seq.setSystemServices(systemServices)
    seq(LoopSafe(JnlsSafe)) {
      rsp => initialize(rootMap, rf)
    }
  }
}
