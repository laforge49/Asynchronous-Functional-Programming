/*
 * Copyright 2010 Ruchi B.
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

import org.specs.SpecificationWithJUnit

class DataInputOutputStackTest extends SpecificationWithJUnit {
  "Data Input Output Stack " should {


    "test boolean " in {
      val dos = DataOutputStack()
      dos.writeBoolean(true)
      val dis = DataInputStack(dos)
      dis.readBoolean must be equalTo (true)
    }

    "test int " in {
      val dos = DataOutputStack()
      dos.writeInt(5)
      val dis = DataInputStack(dos)
      dis.readInt must be equalTo (5)
    }

    "test string utf " in {
      val dos = DataOutputStack()
      dos.writeUTF("test1")
      dos.writeUTF("test2")
      dos.writeUTF("test3")
      dos.writeUTF("test4")
      dos.writeUTF("test5")
      dos.writeUTF("test6")
      val dis = DataInputStack(dos)
      val test1 = dis.readUTF
      val test2 = dis.readUTF
      test1 must be equalTo ("test6")
      test2 must be equalTo ("test5")
      dis.readUTF must be equalTo ("test4")
      dis.readUTF must be equalTo ("test3")
      dis.readUTF must be equalTo ("test2")
      dis.readUTF must be equalTo ("test1")
    }

    "test float " in {
      val dos = DataOutputStack()
      dos.writeFloat(5.5f)
      val dis = DataInputStack(dos)
      dis.readFloat must be equalTo (5.5f)
    }

    "test long " in {
      val dos = DataOutputStack()
      dos.writeLong(55555555555555555L)
      val dis = DataInputStack(dos)
      dis.readLong must be equalTo (55555555555555555L)
    }

    "test double " in {
      val dos = DataOutputStack()
      dos.writeDouble(5.5555555555555555)
      val dis = DataInputStack(dos)
      dis.readDouble must be equalTo (5.5555555555555555)
    }

    "test list" in {
      val dos = DataOutputStack()
      var list=List[String]()
      list +:= "A"
      list +:= "B"
      list +:= "C"
      list +:= "1"
      list +:= "2"
      list +:= "3"
      list +:= "X"
      list +:= "Y"
      list +:= "ZEE"
      dos.writeStringList(list)
      val dis = DataInputStack(dos)
      dis.readStringList must be equalTo list
    }

    "test map" in {
      val dos = DataOutputStack()
      var map=Map[String,String]()
      map += ("1"->"A")
      map += ("2"->"B")
      map += ("3"->"C")
      map += ("a"->"1")
      map += ("b"->"2")
      map += ("c"->"3")
      map += ("z"->"X")
      map += ("why"->"Y")
      map += ("x"->"ZEE")
      dos.writeStringMap(map)
      val dis = DataInputStack(dos)
      dis.readStringMap must be equalTo map
    }

  }
}
