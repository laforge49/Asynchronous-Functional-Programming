/*
 * Copyright 2010 Bill La Forge
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
package com

import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.InvalidObjectException

class DataInputStack(bais: ByteArrayInputStream) extends DataInputStream(bais) {
  def readRolonName: RolonName = {
    check("ROLON")
    val rolonUuid = readUTF
    RolonName(rolonUuid)
  }

  def readStringList: List[String] = {
    readList[String]("STRING_LIST", (dis: DataInputStack) => dis.readUTF)
  }

  def readStringMap: Map[String, String] = {
    readMap[String, String](
      "STRING_MAP",
      (dis: DataInputStack) => dis.readUTF,
      (dis: DataInputStack) => dis.readUTF)
  }

  def readMapMap: Map[String, Map[String, String]] = {
    readMap[String, Map[String, String]](
      "MAP_MAP",
      (dis: DataInputStack) => dis.readUTF,
      (dis: DataInputStack) => dis.readStringMap)
  }

  def readMapMapMap: Map[String, Map[String, Map[String, String]]] = {
    readMap[String, Map[String, Map[String, String]]](
      "MAP_MAP_MAP",
      (dis: DataInputStack) => dis.readUTF,
      (dis: DataInputStack) => dis.readMapMap)
  }

  def readRolonList: List[RolonName] = {
    readList[RolonName]("ROLON_LIST", (dis: DataInputStack) => {
      val uuid = readUTF
      RolonName(uuid)
    })
  }

  def readList[A](checkName: String, readA: (DataInputStack) => A): List[A] = {
    check(checkName)
    val size = readInt
    var list = List[A]()
    for (i <- 1 to size) {
      list +:= readA(this)
    }
    list
  }

  def readMap[A, B](checkName: String, readAFrom: (DataInputStack) => A, readBFrom: (DataInputStack) => B): Map[A, B] = {
    check(checkName)
    val size = readInt
    var map = Map[A, B]()
    for (i <- 1 to size) {
      val a = readAFrom(this)
      val b = readBFrom(this)
      map += (a -> b)
    }
    map
  }

  private def check(typeName: String) {
    val value = readUTF
    if (value != typeName)
      throw new InvalidObjectException("No object of type '" + typeName + "' is found")
  }
}

object DataInputStack {
  def apply(bytes: Array[Byte]): DataInputStack = {
    val bais = new ByteArrayInputStream(bytes)
    new DataInputStack(bais)
  }

  def apply(dos: DataOutputStack): DataInputStack = apply(dos.getBytes)
}
