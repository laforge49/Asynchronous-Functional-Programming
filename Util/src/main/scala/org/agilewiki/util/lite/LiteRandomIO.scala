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
  addRequestHandler {
    case req: ReadBlockReq => {
      randomAccessFile.seek(req.blockOffset)
      randomAccessFile.readFully(req.bytes)
      reply(ReadBlockRsp)
    }
    case req: ReadVariableBlockReq => {
      randomAccessFile.seek(req.blockOffset)
      val len = randomAccessFile.readInt
      val bytes = new Array[Byte](len)
      randomAccessFile.readFully(bytes)
      reply(ReadVariableBlockRsp(bytes))
    }
    case req: WriteBlockReq => {
      randomAccessFile.seek(req.blockOffset)
      randomAccessFile.write(req.bytes)
      reply(WriteBlockRsp)
    }
    case req: WriteVariableBlockReq => {
      randomAccessFile.seek(req.blockOffset)
      randomAccessFile.writeInt(req.bytes.length)
      randomAccessFile.write(req.bytes)
      reply(WriteVariableBlockRsp)
    }
    case req: CloseReq => {
      try {
        randomAccessFile.close
      } catch {
        case unknown => {}
      }
      reply(CloseRsp)
    }
  }
}

object LiteRandomIO {
  def apply(reactor: LiteReactor, pathname: String, accessMode: String) = {
    new LiteRandomIO(reactor, pathname, accessMode, null)
  }
}

case class ReadBlockReq(blockOffset: Long, bytes: Array[Byte])

case class ReadBlockRsp

case class ReadVariableBlockReq(blockOffset: Long)

case class ReadVariableBlockRsp(bytes: Array[Byte])

case class WriteBlockReq(blockOffset: Long, bytes: Array[Byte])

case class WriteBlockRsp

case class WriteVariableBlockReq(blockOffset: Long, bytes: Array[Byte])

case class WriteVariableBlockRsp

case class CloseReq

case class CloseRsp
