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
package org.agilewiki.util
package lite
package com

import java.io.DataOutput

import scala.collection.immutable.Stack

//See http://java.sun.com/j2se/1.5.0/docs/api/java/io/DataInput.html
//and http://java.sun.com/j2se/1.5.0/docs/api/java/io/DataOutput.html

case class DataOutputStack()
  extends DataOutput
  with DataStack {
  private var stack = new Stack[Array[Byte]]()

  def inputPayload = DataInputStack(this)

  def getStack = stack

  def size = {
    var s = 0
    var iter = stack.iterator
    while (iter.hasNext) {
      s += iter.next.size
    }
    s
  }

  def getBytes = {
    val s = size
    val b = new Array[Byte](s)
    var o = 0
    val iter = stack.iterator
    while (iter.hasNext) {
      val pn = iter.next
      val pns = pn.size
      System.arraycopy(pn, 0, b, o, pns)
      o += pns
    }
    b
  }

  override def write(b: Int) {
    val l = new Array[Byte](1)
    val v = (255 & b).asInstanceOf[Byte]
    l.update(0, v)
    write(l)
  }

  override def write(b: Array[Byte]) {
    stack = stack.push(b)
  }

  override def write(b: Array[Byte], off: Int, len: Int) {
    write(b.slice(off, off + len))
  }

  override def writeShort(v: Int) {
    val s = new Array[Byte](2)
    var b = (255 & (v >> 8)).asInstanceOf[Byte]
    s.update(0, b)
    b = (255 & v).asInstanceOf[Byte]
    s.update(1, b)
    write(s)
  }

  override def writeUTF(str: String) {
    if (str == null) throw new NullPointerException
    else {
      val b = str.getBytes("UTF-8")
      write(b)
      writeShort(b.size)
    }
  }

  override def writeInt(v: Int) {
    val s = new Array[Byte](4)
    val b0 = (255 & v).asInstanceOf[Byte]
    var w = v >> 8
    val b1 = (255 & w).asInstanceOf[Byte]
    w = w >> 8
    val b2 = (255 & w).asInstanceOf[Byte]
    w = w >> 8
    val b3 = (255 & w).asInstanceOf[Byte]
    s.update(0, b3)
    s.update(1, b2)
    s.update(2, b1)
    s.update(3, b0)
    write(s)
  }

  override def writeLong(v: Long) {
    val s = new Array[Byte](8)
    val b0 = (255 & v).asInstanceOf[Byte]
    var w = v >> 8
    val b1 = (255 & w).asInstanceOf[Byte]
    w = w >> 8
    val b2 = (255 & w).asInstanceOf[Byte]
    w = w >> 8
    val b3 = (255 & w).asInstanceOf[Byte]
    w = w >> 8
    val b4 = (255 & w).asInstanceOf[Byte]
    w = w >> 8
    val b5 = (255 & w).asInstanceOf[Byte]
    w = w >> 8
    val b6 = (255 & w).asInstanceOf[Byte]
    val b7 = (w >> 8).asInstanceOf[Byte]
    s.update(0, b7)
    s.update(1, b6)
    s.update(2, b5)
    s.update(3, b4)
    s.update(4, b3)
    s.update(5, b2)
    s.update(6, b1)
    s.update(7, b0)
    write(s)
  }

  override def writeChar(v: Int) {
    val s = new Array[Byte](2)
    var b = (255 & (v >> 8)).asInstanceOf[Byte]
    s.update(0, b)
    b = (255 & v).asInstanceOf[Byte]
    s.update(1, b)
    write(s)
  }

  override def writeChars(v: String) {
    if (v == null) throw new NullPointerException
    else if (v.length == 0) return
    else {
      val chars = v.toCharArray()
      val s = new Array[Byte](chars.length * 2)
      var p = 0
      for (i <- 0 to chars.length - 1) {
        val c = chars(i)
        var b = (255 & (c >> 8)).asInstanceOf[Byte]
        s.update(p, b)
        p += 1
        b = (255 & c).asInstanceOf[Byte]
        s.update(p, b)
        p += 1
      }
      write(s)
    }

  }

  override def writeFloat(v: Float) {
    val i = java.lang.Float.floatToIntBits(v)
    writeInt(i)
  }

  override def writeDouble(v: Double) {
    val l = java.lang.Double.doubleToLongBits(v)
    writeLong(l)
  }

  override def writeBytes(v: String) {
    if (v == null) throw new NullPointerException
    else if (v.length == 0) return
    else {
      val chars = v.toCharArray()
      val s = new Array[Byte](chars.length)
      for (i <- 0 to chars.length - 1) {
        val c = chars(i)
        val b = (255 & c).asInstanceOf[Byte]
        s.update(i, b)
      }
      write(s)
    }
  }

  override def writeByte(v: Int) {
    val s = new Array[Byte](1)
    val b = (255 & v).asInstanceOf[Byte]
    s.update(0, b)
    write(s)
  }

  override def writeBoolean(v: Boolean) {
    if (v)
      write(1)
    else
      write(0)
  }

  override def clone: DataOutputStack = {
    val x = DataOutputStack()
    x.stack = this.stack
    x
  }

  def writeRolonName(rolonName: RolonName) {
    writeUTF(rolonName.rolonUuid)
    writeUTF("ROLON")
  }

  def writeStringList(list: List[String]) {
    writeList[String](list, "STRING_LIST", (a: String, dos: DataOutputStack) => dos.writeUTF(a))
  }

  def writeStringMap(map: Map[String, String]) {
    writeMap[String, String](
      map,
      "STRING_MAP",
      (a: String, dos: DataOutputStack) => dos.writeUTF(a),
      (b: String, dos: DataOutputStack) => dos.writeUTF(b))
  }

  def writeMapMap(map: Map[String, Map[String, String]]) {
    writeMap[String, Map[String, String]](
      map,
      "MAP_MAP",
      (a: String, dos: DataOutputStack) => dos.writeUTF(a),
      (b: Map[String, String], dos: DataOutputStack) => dos.writeStringMap(b))
  }

  def writeMapMapMap(map: Map[String, Map[String, Map[String, String]]]) {
    writeMap[String, Map[String, Map[String, String]]](
      map,
      "MAP_MAP_MAP",
      (a: String, dos: DataOutputStack) => dos.writeUTF(a),
      (b: Map[String, Map[String, String]], dos: DataOutputStack) => dos.writeMapMap(b))
  }

  def writeRolonList(list: List[RolonName]) {
    writeList[RolonName](list, "ROLON_LIST", (rolon: RolonName, dos: DataOutputStack) => {
      dos.writeUTF(rolon.rolonUuid)
    })
  }

  def writeList[A](list: List[A], checkName: String, writeA: (A, DataOutputStack) => Unit) {
    for (a <- list) {
      writeA(a, this)
    }
    writeInt(list.size)
    writeUTF(checkName)
  }

  def writeMap[A, B](map: Map[A, B], checkName: String, writeA: (A, DataOutputStack) => Unit, writeB: (B, DataOutputStack) => Unit) {
    for ((a, b) <- map) {
      writeB(b, this)
      writeA(a, this)
    }
    writeInt(map.size)
    writeUTF(checkName)
  }
}