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
package incDes

case class MutableData(bytes: Array[Byte], var offset: Int) {
  def immutable = ImmutableData(bytes, offset)

    def rewind {offset = 0}

    def skip(length: Int) {offset += length}

    def write(immutableData: ImmutableData, length: Int) {
      System.arraycopy(immutableData.bytes, immutableData.offset, bytes, offset, length)
      skip(length)
    }

    def writeByte(b: Int) {
      bytes.update(offset, b.asInstanceOf[Byte])
      skip(1)
    }

    def readByte = {
      val b = bytes(offset)
      skip(1)
      b.asInstanceOf[Int] & 255
    }

    def writeBytes(bs: Array[Byte]) {
      System.arraycopy(bs, 0, bytes, offset, bs.length)
      skip(bs.length)
    }

    def writeBytes(bs: Array[Byte], off: Int, len: Int) {
      System.arraycopy(bs, off, bytes, offset, len)
      skip(len)
    }

    def readBytes(len: Int): Array[Byte] = {
      val bs = new Array[Byte](len)
      System.arraycopy(bytes, offset, bs, 0, len)
      skip(len)
      bs
    }

    def readBytes(bs: Array[Byte], off: Int, len: Int) {
      System.arraycopy(bytes, offset, bs, off, len)
      skip(len)
      bs
    }

    def writeInt(integer: Int) {
      bytes.update(offset + 3, (255 & integer).asInstanceOf[Byte])
      var w = integer >> 8
      bytes.update(offset + 2, (255 & w).asInstanceOf[Byte])
      w = w >> 8
      bytes.update(offset + 1, (255 & w).asInstanceOf[Byte])
      w = w >> 8
      bytes.update(offset, (255 & w).asInstanceOf[Byte])
      skip(4)
    }

    def readInt = {
      val ch1 = readByte
      val ch2 = readByte
      val ch3 = readByte
      val ch4 = readByte
      (ch1 << 24) | (ch2 << 16) | (ch3 << 8) | ch4
    }

    def writeLong(long: Long) {
      bytes.update(offset + 7, (255 & long).asInstanceOf[Byte])
      var w = long >> 8
      bytes.update(offset + 6, (255 & w).asInstanceOf[Byte])
      w = w >> 8
      bytes.update(offset + 5, (255 & w).asInstanceOf[Byte])
      w = w >> 8
      bytes.update(offset + 4, (255 & w).asInstanceOf[Byte])
      w = w >> 8
      bytes.update(offset + 3, (255 & w).asInstanceOf[Byte])
      w = w >> 8
      bytes.update(offset + 2, (255 & w).asInstanceOf[Byte])
      w = w >> 8
      bytes.update(offset + 1, (255 & w).asInstanceOf[Byte])
      w = w >> 8
      bytes.update(offset, (255 & w).asInstanceOf[Byte])
      skip(8)
    }

    def readLong = {
      val ch1 = readByte.asInstanceOf[Long]
      val ch2 = readByte.asInstanceOf[Long]
      val ch3 = readByte.asInstanceOf[Long]
      val ch4 = readByte.asInstanceOf[Long]
      val ch5 = readByte.asInstanceOf[Long]
      val ch6 = readByte.asInstanceOf[Long]
      val ch7 = readByte.asInstanceOf[Long]
      val ch8 = readByte.asInstanceOf[Long]
      (ch1 << 56) | (ch2 << 48) | (ch3 << 40) | (ch4 << 32) | (ch5 << 24) | (ch6 << 16) | (ch7 << 8) | ch8
    }

    def writeString(string: String) {
      writeInt(string.length)
      val chars = string.toCharArray()
      for (i <- 0 to (chars.length - 1)) {
        val c = chars(i)
        var b = (255 & (c >> 8)).asInstanceOf[Byte]
        writeByte(b)
        b = (255 & c).asInstanceOf[Byte]
        writeByte(b)
      }
    }

    def readString: String = {
      val length = readInt
      readString(length)
    }

    def readString(length: Int): String = {
      val chars = new Array[Char](length)
      for (i <- 0 to chars.length - 1) {
        val b1 = readByte
        val b2 = readByte
        val c = ((b1 << 8) | b2).asInstanceOf[Char]
        chars.update(i, c)
      }
      new String(chars)
    }
}
