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
import records._
import blocks._

case class TransactionRequest(request: IncDes)

case class Transaction(block: Block)

class QueryTransaction(block: Block)
  extends Transaction(block)

class UpdateTransaction(_timestamp: Long, block: Block)
  extends Transaction(block) {
  def timestamp = _timestamp
}

case class Commit(update: Block)

case class Abort(exception: Exception)

case class DbRoot()

case class GetTimestamp()

case class LogTransaction(timestamp: Long, bytes: Array[Byte])

case class LogInfo()

case class ReadBytes(offset: Long, length: Int)

case class ReadBytesOrNull(offset: Long, length: Int)

case class WriteBytes(offset: Long, bytes: Array[Byte])

case class ReadRootBlock()

case class WriteRootBlock(rootBlock: Block)

case class InitDb(rootBlock: Block)

case class Recover()

case class ProcessFile(jnlPathname: String)

case class FilesSeq(dirPathname: String)

case class Pathname()

case class Records()

case class RecordsPathname()

case class GetRecord(recordKey: String)

case class AssignRecord(transactionContext: UpdateContext, recordKey: String, record: Record)

case class BatchItem(batchItem: IncDes)

case class ValidateTimestamp(recordKey: String, timestamp: Long)

