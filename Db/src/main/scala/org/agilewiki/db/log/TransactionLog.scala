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
import bind._
import services._
import java.nio.channels._

class TransactionLogComponentFactory extends ComponentFactory {
  override def instantiate(actor: Actor) = new TransactionLogComponent(actor)
}

class TransactionLogComponent(actor: Actor)
  extends Component(actor) {
  private var transactionLog: TransactionLog = null

  transactionLog = new TransactionLog
  bindMessageLogic(classOf[LogTransaction], new Forward(transactionLog))
  bindMessageLogic(classOf[LogInfo], new Forward(transactionLog))

  override def open {
    super.open
    transactionLog.setSystemServices(systemServices)
    transactionLog.setExchangeMessenger(newAsyncMailbox)
    transactionLog.logDirPathname = GetProperty.required("logDirPathname")
    transactionLog.flushLog = GetProperty.boolean("flushLog", true)
    transactionLog.blockSize = GetProperty.int("logBlockSize", 8 * 1024)
  }

  override def close {
    transactionLog.close
    super.close
  }
}

class TransactionLog extends Actor {
  var logDirPathname: String = null
  var flushLog = true
  var blockSize = 8 * 1024
  var logFile: java.io.File = null
  private val logTS = (new org.joda.time.DateTime(org.joda.time.DateTimeZone.UTC)).
    toString("yyyy-MM-dd_HH-mm-ss_SSS")
  private var writer: java.io.DataOutputStream = null
  private var fileChannel: FileChannel = null

  bind(classOf[LogTransaction], logTransaction)
  bind(classOf[LogInfo], logInfo)

  private def logInfo(msg: AnyRef, rf: Any => Unit) {
    val position = if (logFile == null) 0L else fileChannel.size
    rf((logTS, position))
  }

  private def logTransaction(msg: AnyRef, rf: Any => Unit) {
    initializeLog
    val logTransaction = msg.asInstanceOf[LogTransaction]
    val timestamp = logTransaction.timestamp
    val bytes = logTransaction.bytes
    writer.writeLong(timestamp)
    writer.writeInt(bytes.length)
    writer.write(bytes)
    if (flushLog) {
      writer.flush
      fileChannel.force(false)
    }
    rf(null)
  }

  def initializeLog {
    if (writer != null) return
    val dir = new java.io.File(logDirPathname)
    if (!dir.exists) dir.mkdirs
    val fileName = dir.getCanonicalPath + java.io.File.separator + logTS + ".jnl"
    logFile = new java.io.File(fileName)
    val fileOutputStream = new java.io.FileOutputStream(logFile)
    fileChannel = fileOutputStream.getChannel
    val bos = new java.io.BufferedOutputStream(fileOutputStream, blockSize)
    writer = new java.io.DataOutputStream(bos)
  }

  override def close {
    try {
      writer.close
    } catch {
      case unknown => {}
    }
    super.close
  }
}
