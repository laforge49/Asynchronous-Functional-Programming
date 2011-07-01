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
package util
package lite

import java.io.{RandomAccessFile, File}

class LiteRandomIO(reactor: LiteReactor, pathname: String, accessMode: String, factory: ActorFactory)
  extends LiteActor(reactor, factory) {
  val file = new File(pathname)
  val randomAccessFile = new RandomAccessFile(file, accessMode)
  addRequestHandler{
    case req: ReadBlockReq => readBlock(req)(back)
    case req: ReadVariableBlockReq => readVariableBlock(req)(back)
    case req: WriteBlockReq => writeBlock(req)(back)
    case req: WriteVariableBlockReq => writeVariableBlock(req)(back)
    case req: CloseReq => close(req)(back)
  }

  private def readBlock(req: ReadBlockReq)
                       (responseProcess: PartialFunction[Any, Unit])
                       (implicit src: ActiveActor) {
    randomAccessFile.seek(req.blockOffset)
    randomAccessFile.readFully(req.bytes)
    responseProcess(ReadBlockRsp())
  }

  private def readVariableBlock(req: ReadVariableBlockReq)
                               (responseProcess: PartialFunction[Any, Unit])
                               (implicit src: ActiveActor) {
    randomAccessFile.seek(req.blockOffset)
    val len = randomAccessFile.readInt
    val bytes = new Array[Byte](len)
    randomAccessFile.readFully(bytes)
    responseProcess(ReadVariableBlockRsp(bytes))
  }

  private def writeBlock(req: WriteBlockReq)
                        (responseProcess: PartialFunction[Any, Unit])
                        (implicit src: ActiveActor) {
    randomAccessFile.seek(req.blockOffset)
    randomAccessFile.write(req.bytes)
    responseProcess(WriteBlockRsp())
  }

  private def writeVariableBlock(req: WriteVariableBlockReq)
                                (responseProcess: PartialFunction[Any, Unit])
                                (implicit src: ActiveActor) {
    randomAccessFile.seek(req.blockOffset)
    randomAccessFile.writeInt(req.bytes.length)
    randomAccessFile.write(req.bytes)
    responseProcess(WriteVariableBlockRsp())
  }

  private def close(req: CloseReq)
                   (responseProcess: PartialFunction[Any, Unit])
                   (implicit src: ActiveActor) {
    try {
      randomAccessFile.close
    } catch {
      case unknown => {}
    }
    responseProcess(CloseRsp())
  }
}

case class ReadBlockReq(blockOffset: Long, bytes: Array[Byte])

case class ReadBlockRsp()

case class ReadVariableBlockReq(blockOffset: Long)

case class ReadVariableBlockRsp(bytes: Array[Byte])

case class WriteBlockReq(blockOffset: Long, bytes: Array[Byte])

case class WriteBlockRsp()

case class WriteVariableBlockReq(blockOffset: Long, bytes: Array[Byte])

case class WriteVariableBlockRsp()

case class CloseReq()

case class CloseRsp()
