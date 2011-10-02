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
import services._

class TransactionLogComponentFactory extends ComponentFactory {
  override def instantiate(actor: Actor) = new TransactionLogComponent(actor)
}

class TransactionLogComponent(actor: Actor)
  extends Component(actor) {
  private var transactionLog = new TransactionLog
  bindSafe(classOf[LogTransaction], new SafeForward(transactionLog))
  bindSafe(classOf[LogAbort], new SafeForward(transactionLog))

  override def open {
    super.open
    transactionLog.logDirPathname = GetProperty.required("logDirPathname")
    transactionLog.flush = "true" == GetProperty("flushLog")
  }

  override def close {
    transactionLog.close
    super.close
  }
}

class TransactionLog
  extends Actor {
  var logDirPathname: String = null
  var flush = false
  private val logTS = (new org.joda.time.DateTime(org.joda.time.DateTimeZone.UTC)).
    toString("yyyy-MM-dd_HH-mm-ss_SSS")
  private var writer: java.io.DataOutputStream = null

  setMailbox(new Mailbox)
  bind(classOf[LogTransaction], logTransaction)
  bind(classOf[LogAbort], logAbort)

  private def logTransaction(msg: AnyRef, rf: Any => Unit) {
    initialize
    val logTransaction = msg.asInstanceOf[LogTransaction]
    val timestamp = logTransaction.timestamp
    val bytes = logTransaction.bytes
    writer.writeLong(timestamp)
    writer.writeInt(bytes.length)
    writer.write(bytes)
    if (flush) writer.flush
    rf(null)
  }

  private def logAbort(msg: AnyRef, rf: Any => Unit) {
    writer.writeLong(0L)
    if (flush) writer.flush
    rf(null)
  }

  def initialize {
    if (writer != null) return
    val dir = new java.io.File(logDirPathname)
    if (!dir.exists) dir.mkdirs
    val fileName = dir.getCanonicalPath + java.io.File.separator + logTS + ".jnl"
    writer = new java.io.DataOutputStream(new java.io.FileOutputStream(fileName))
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
