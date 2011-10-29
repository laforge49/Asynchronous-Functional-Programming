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
package incDesNavMap

import org.specs.SpecificationWithJUnit
import blip._
import seq._

class NavMapTest extends SpecificationWithJUnit {
  "NavMapTest" should {
    "Serialize/deserialize" in {
      val systemServices = SystemServices(new IncDesComponentFactory)

      val booll0 = IncDesIntBooleanMap(new ReactorMailbox, systemServices)
      val bool0 = booll0.newValue
      Future(booll0, Put[Int, IncDesBoolean, Boolean](null, 2, bool0))
      Future(booll0, Length()) must be equalTo (9)
      val boolb0 = Future(booll0, Bytes()).asInstanceOf[Array[Byte]]

      val booll1 = IncDesIntBooleanMap(new ReactorMailbox, systemServices)
      booll1.load(boolb0)
      Future(booll1, Length()) must be equalTo (9)
      val bool1 = Future(booll1, Get(2))
      bool1.isInstanceOf[IncDesBoolean] must beTrue

      val bytesl0 = IncDesLongBytesMap(new ReactorMailbox, systemServices)
      val bytes0 = IncDesBytes(null)
      Future(bytesl0, Put[Long, IncDesBytes, Array[Byte]](null, 42L, bytes0))
      Future(bytesl0, Length()) must be equalTo (16)
      val bytesb0 = Future(bytesl0, Bytes()).asInstanceOf[Array[Byte]]

      val bytesl1 = IncDesLongBytesMap(new ReactorMailbox, systemServices)
      bytesl1.load(bytesb0)
      Future(bytesl1, Length()) must be equalTo (16)
      val bytes1 = Future(bytesl1, Get(42L))
      bytes1.isInstanceOf[IncDesBytes] must beTrue

      val intl0 = IncDesStringIntMap(new ReactorMailbox, systemServices)
      val int0 = intl0.newValue
      Future(intl0, Put[String, IncDesInt, Int](null, "j", int0))
      Future(intl0, Length()) must be equalTo (14)
      val intb0 = Future(intl0, Bytes()).asInstanceOf[Array[Byte]]

      val intl1 = IncDesStringIntMap(new ReactorMailbox, systemServices)
      intl1.load(intb0)
      Future(intl1, Length()) must be equalTo (14)
      val int1 = Future(intl1, Get("j"))
      int1.isInstanceOf[IncDesInt] must beTrue

      val longl0 = IncDesIntLongMap(new ReactorMailbox, systemServices)
      val long0 = IncDesLong(null)
      Future(longl0, Put[Int, IncDesLong, Long](null, 9, long0))
      Future(longl0, Length()) must be equalTo (16)
      val longb0 = Future(longl0, Bytes()).asInstanceOf[Array[Byte]]

      val longl1 = IncDesIntLongMap(new ReactorMailbox, systemServices)
      longl1.load(longb0)
      Future(longl1, Length()) must be equalTo (16)
      val long1 = Future(longl1, Get(9))
      long1.isInstanceOf[IncDesLong] must beTrue

      val strl0 = IncDesLongStringMap(new ReactorMailbox, systemServices)
      val str0 = strl0.newValue
      Future(strl0, Put[Long, IncDesString, String](null, -2L, str0))
      Future(strl0, Length()) must be equalTo (16)
      val strb0 = Future(strl0, Bytes()).asInstanceOf[Array[Byte]]

      val strl1 = IncDesLongStringMap(new ReactorMailbox, systemServices)
      strl1.load(strb0)
      Future(strl1, Length()) must be equalTo (16)
      val str1 = Future(strl1, Get(-2L ))
      str1.isInstanceOf[IncDesString] must beTrue

      val incdesl0 = IncDesStringIncDesMap(new ReactorMailbox, systemServices)
      val incdes0 = IncDesIncDes(null)
      Future(incdesl0, ContainsKey("x")) must be equalTo(false)
      Future(incdesl0, Size()) must be equalTo (0)
      Future(incdesl0, Put[String, IncDesIncDes, IncDes](null, "x", incdes0))
      Future(incdesl0, Length()) must be equalTo (14)
      Future(incdesl0, Size()) must be equalTo (1)
      val incdesb0 = Future(incdesl0, Bytes()).asInstanceOf[Array[Byte]]

      val incdesl1 = IncDesStringIncDesMap(new ReactorMailbox, systemServices)
      incdesl1.load(incdesb0)
      Future(incdesl1, ContainsKey("x")) must be equalTo(true)
      Future(incdesl1, Length()) must be equalTo (14)
      Future(incdesl1, Size()) must be equalTo (1)

      val incdes1 = Future(incdesl1, Get("x"))
      incdes1.isInstanceOf[IncDesIncDes] must beTrue
      val incdes2 = incdesl1.newValue
      Future(incdesl1, Put[String, IncDesIncDes, IncDes](null, "y", incdes2))
      Future(incdesl1, Length()) must be equalTo (24)
      Future(incdesl1, Size()) must be equalTo (2)
      val incdes3 = Future(incdesl1, Remove(null, "x"))
      incdes3.isInstanceOf[IncDesIncDes] must beTrue
      Future(incdesl1, Length()) must be equalTo (14)
      Future(incdesl1, Size()) must be equalTo (1)

      val seq = Future(incdesl1, Seq()).asInstanceOf[Sequence[String, IncDes]]
      Future(seq, First()).isInstanceOf[KVPair[String, IncDesIncDes]] must beTrue
    }
  }
}
