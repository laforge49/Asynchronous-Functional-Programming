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
import services._
import incDes._
import blocks._
import java.util.zip.Adler32

class RootBlockComponentFactory extends ComponentFactory {

  addDependency(classOf[RandomIOComponentFactory])

  override def instantiate(actor: Actor) = new RootBlockComponent(actor)
}

class RootBlockComponent(actor: Actor)
  extends Component(actor) {
  val HEADER_LENGTH = longLength + longLength + intLength + intLength
  private var maxBlockSize = 0
  private var currentRootOffset = 0

  bind(classOf[ReadRootBlock], readRootBlock)
  bindMessageLogic(classOf[WriteRootBlock], new ChainFactory(writeRootBlock))

  override def open {
    super.open
    maxBlockSize = GetProperty.int("maxRootBlockSize", 1024)
    actor.requiredService(classOf[InitDb])
  }

  private def readRootBlock(msg: AnyRef, rf: Any => Unit) {
    val pathname = GetProperty.required("dbPathname")
    val file = new java.io.File(pathname)
    val fileLength = file.length
    if (fileLength == 0L) {
      currentRootOffset = maxBlockSize
      val block = Block(mailbox)
      block.setSystemServices(systemServices)
      actor(InitDb(block))(rf)
    } else {
      _readRootBlock(0L) {
        rsp1 => {
          val b0 = rsp1.asInstanceOf[Block]
          _readRootBlock(maxBlockSize) {
            rsp2 => {
              val b1 = rsp2.asInstanceOf[Block]
              if (b0 == null && b1 == null)
                throw new IllegalStateException("Db corrupted")
              if (b1 == null) {
                currentRootOffset = 0
                rf(b0)
              } else if (b0 == null) {
                currentRootOffset = maxBlockSize
                rf(b1)
              } else if (b0.key.asInstanceOf[Long] > b1.key.asInstanceOf[Long]) {
                currentRootOffset = 0
                rf(b0)
              } else {
                currentRootOffset = maxBlockSize
                rf(b1)
              }
            }
          }
        }
      }
    }
  }

  private def _readRootBlock(offset: Long)(rf: Any => Unit) {
    systemServices(ReadBytesOrNull(offset, HEADER_LENGTH)) {
      rsp1 => {
        if (rsp1 == null) {
          rf(null)
        }
        else {
          val headerBytes = rsp1.asInstanceOf[Array[Byte]]
          val data = new MutableData(headerBytes, 0)
          val checksum = new IncDesLong
          checksum.load(data)
          val timestamp = new IncDesLong
          timestamp.load(data)
          val maxSize = new IncDesInt
          maxSize.load(data)
          val blockLength = new IncDesInt
          blockLength.load(data)
          blockLength(Value()) {
            rsp2 => {
              val length = rsp2.asInstanceOf[Int]
              if (length < 1 || length + HEADER_LENGTH > maxBlockSize) rf(null)
              else systemServices(ReadBytesOrNull(offset + HEADER_LENGTH, length)) {
                rsp3 => {
                  if (rsp3 == null) rf(null)
                  else {
                    val blockBytes = rsp3.asInstanceOf[Array[Byte]]
                    val adler32 = new Adler32
                    adler32.update(headerBytes, longLength, HEADER_LENGTH - longLength)
                    adler32.update(blockBytes)
                    val newcs = adler32.getValue
                    checksum(Value()) {
                      rsp4 => {
                        val cs = rsp4.asInstanceOf[Long]
                        if (cs != newcs) rf(null)
                        else {
                          val rootBlock = Block(mailbox)
                          rootBlock.setSystemServices(systemServices)
                          rootBlock.load(blockBytes)
                          timestamp(Value()) {
                            rsp5 => {
                              rootBlock.partness(null, rsp5, null)
                              maxSize(Value()) {
                                rsp6 => {
                                  val ms = rsp6.asInstanceOf[Int]
                                  if (ms != maxBlockSize) throw new IllegalArgumentException(
                                    "maxRootBlockSize property must be " + ms)
                                  rf(rootBlock)
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
            }
          }
        }
      }
    }
  }

  private def writeRootBlock(msg: AnyRef, chain: Chain) {
    currentRootOffset = maxBlockSize - currentRootOffset
    val rootBlock = msg.asInstanceOf[WriteRootBlock].rootBlock
    var bytes: Array[Byte] = null
    var length = 0
    val checksum = new IncDesLong
    val timestamp = new IncDesLong
    val maxSize = new IncDesInt
    val blockLength = new IncDesInt
    var data: MutableData = null
    chain.op(systemServices, GetTimestamp(), "timestamp")
    chain.op(maxSize, Set(null, maxBlockSize))
    chain.op(timestamp, Unit => Set(null, chain("timestamp")))
    chain.op(rootBlock, Length(), "length")
    chain.op(blockLength, Unit => {
      length = chain("length").asInstanceOf[Int]
      if (length + HEADER_LENGTH > maxBlockSize)
        throw new IllegalArgumentException("Root block size exceeds maxRootBlockSize property: " +
          length + HEADER_LENGTH + " > " + maxBlockSize)
      Set(null, length)
    })
    chain.op(timestamp, Unit => {
      bytes = new Array[Byte](HEADER_LENGTH + length)
      data = new MutableData(bytes, longLength)
      Save(data)
    })
    chain.op(maxSize, Unit => Save(data))
    chain.op(blockLength, Unit => Save(data))
    chain.op(rootBlock, Unit => Save(data))
    chain.op(checksum, Unit => {
      val adler32 = new Adler32
      adler32.update(bytes, longLength, length + HEADER_LENGTH - longLength)
      val cs = adler32.getValue
      Set(null, cs)
    })
    chain.op(checksum, Unit => {
      data.rewind
      Save(data)
    })
    chain.op(actor, Unit => WriteBytes(currentRootOffset, bytes))
  }
}
