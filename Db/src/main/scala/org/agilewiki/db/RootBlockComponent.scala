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
import java.util.zip.Adler32

class RootBlockComponentFactory extends ComponentFactory {

  addDependency(classOf[PropertiesComponentFactory])

  override def instantiate(actor: Actor) = new RootBlockComponent(actor)
}

class RootBlockComponent(actor: Actor)
  extends Component(actor) {
  val HEADER_LENGTH = 4 + 8
  private var currentRootOffset = 0
  private var maxBlockSize = 0

  bind(classOf[ReadRootBlock], readRootBlock)
  bind(classOf[WriteRootBlock], writeRootBlock)

  override def open {
    super.open
    maxBlockSize = GetProperty.int("maxRootBlockSize", 1024)
  }

  private def readRootBlock(msg: AnyRef, rf: Any => Unit) {

  }

  private def writeRootBlock(msg: AnyRef, rf: Any => Unit) {
    currentRootOffset = maxBlockSize - currentRootOffset
    val rootBlock = msg.asInstanceOf[WriteRootBlock].rootBlock
    var bytes: Array[Byte] = null
    var length = 0
    val blockLength = new IncDesInt
    val checksum = new IncDesLong
    var data: MutableData = null
    val results = new Results
    val chain = new Chain(results)
    chain.op(rootBlock, Length(), "length")
    chain.op(blockLength, Unit => {
      length = results("length").asInstanceOf[Int]
      Set(null, length)
    })
    chain.op(blockLength, Unit => {
      bytes = new Array[Byte](HEADER_LENGTH + length)
      data = new MutableData(bytes, 8)
      Save(data)
    })
    chain.op(rootBlock, Save(data))
    chain.op(checksum, Unit => {
      val adler32 = new Adler32
      adler32.update(bytes, 8, length + 4)
      val cs = adler32.getValue
      Set(null, cs)
    })
    chain.op(checksum, Unit => {
      data.rewind
      Save(data)
    })
    chain.op(actor, WriteBytes(currentRootOffset, bytes))
  }
}