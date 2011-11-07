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
package incDesList

import org.specs.SpecificationWithJUnit
import blip._
import seq._

class ListTest extends SpecificationWithJUnit {
  "ListTest" should {
    "Serialize/deserialize" in {
      val systemServices = SystemServices(new IncDesComponentFactory)
      try {
        val booll0 = IncDesBooleanList(systemServices.newSyncMailbox, systemServices)
        val bool0 = booll0.newValue
        Future(booll0, Add[IncDesBoolean, Boolean](null, bool0))
        Future(booll0, Length()) must be equalTo (5)
        val boolb0 = Future(booll0, Bytes()).asInstanceOf[Array[Byte]]

        val booll1 = IncDesBooleanList(systemServices.newSyncMailbox, systemServices)
        booll1.load(boolb0)
        Future(booll1, Length()) must be equalTo (5)
        val bool1 = Future(booll1, Get(0))
        bool1.isInstanceOf[IncDesBoolean] must beTrue

        val bytesl0 = IncDesBytesList(systemServices.newSyncMailbox, systemServices)
        val bytes0 = IncDesBytes(null)
        Future(bytesl0, Add[IncDesBytes, Array[Byte]](null, bytes0))
        Future(bytesl0, Length()) must be equalTo (8)
        val bytesb0 = Future(bytesl0, Bytes()).asInstanceOf[Array[Byte]]

        val bytesl1 = IncDesBytesList(systemServices.newSyncMailbox, systemServices)
        bytesl1.load(bytesb0)
        Future(bytesl1, Length()) must be equalTo (8)
        val bytes1 = Future(bytesl1, Get(0))
        bytes1.isInstanceOf[IncDesBytes] must beTrue

        val intl0 = IncDesIntList(systemServices.newSyncMailbox, systemServices)
        val int0 = intl0.newValue
        Future(intl0, Add[IncDesInt, Int](null, int0))
        Future(intl0, Length()) must be equalTo (8)
        val intb0 = Future(intl0, Bytes()).asInstanceOf[Array[Byte]]

        val intl1 = IncDesIntList(systemServices.newSyncMailbox, systemServices)
        intl1.load(intb0)
        Future(intl1, Length()) must be equalTo (8)
        val int1 = Future(intl1, Get(0))
        int1.isInstanceOf[IncDesInt] must beTrue

        val longl0 = IncDesLongList(systemServices.newSyncMailbox, systemServices)
        val long0 = IncDesLong(null)
        Future(longl0, Add[IncDesLong, Long](null, long0))
        Future(longl0, Length()) must be equalTo (12)
        val longb0 = Future(longl0, Bytes()).asInstanceOf[Array[Byte]]

        val longl1 = IncDesLongList(systemServices.newSyncMailbox, systemServices)
        longl1.load(longb0)
        Future(longl1, Length()) must be equalTo (12)
        val long1 = Future(longl1, Get(0))
        long1.isInstanceOf[IncDesLong] must beTrue

        val strl0 = IncDesStringList(systemServices.newSyncMailbox, systemServices)
        val str0 = strl0.newValue
        Future(strl0, Add[IncDesString, String](null, str0))
        Future(strl0, Length()) must be equalTo (8)
        val strb0 = Future(strl0, Bytes()).asInstanceOf[Array[Byte]]

        val strl1 = IncDesStringList(systemServices.newSyncMailbox, systemServices)
        strl1.load(strb0)
        Future(strl1, Length()) must be equalTo (8)
        val str1 = Future(strl1, Get(0))
        str1.isInstanceOf[IncDesString] must beTrue

        val incdesl0 = IncDesIncDesList(systemServices.newSyncMailbox, systemServices)
        val incdes0 = IncDesIncDes(null)
        Future(incdesl0, ContainsKey(0)) must be equalTo (false)
        Future(incdesl0, Size()) must be equalTo (0)
        Future(incdesl0, Add[IncDesIncDes, IncDes](null, incdes0))
        Future(incdesl0, Length()) must be equalTo (8)
        Future(incdesl0, Size()) must be equalTo (1)
        val incdesb0 = Future(incdesl0, Bytes()).asInstanceOf[Array[Byte]]

        val incdesl1 = IncDesIncDesList(systemServices.newSyncMailbox, systemServices)
        incdesl1.load(incdesb0)
        Future(incdesl1, ContainsKey(0)) must be equalTo (true)
        Future(incdesl1, Length()) must be equalTo (8)
        Future(incdesl1, Size()) must be equalTo (1)

        val incdes1 = Future(incdesl1, Get(0))
        incdes1.isInstanceOf[IncDesIncDes] must beTrue
        val incdes2 = incdesl1.newValue.asInstanceOf[IncDesIncDes]
        Future(incdesl1, Insert[IncDesIncDes, IncDes](null, 0, incdes2))
        Future(incdesl1, Length()) must be equalTo (12)
        Future(incdesl1, Size()) must be equalTo (2)
        val incdes3 = Future(incdesl1, Remove(null, 1))
        incdes3.isInstanceOf[IncDesIncDes] must beTrue
        Future(incdesl1, Length()) must be equalTo (8)
        Future(incdesl1, Size()) must be equalTo (1)

        val seq = Future(incdesl1, Seq()).asInstanceOf[Sequence[Int, IncDes]]
        Future(seq, First()).isInstanceOf[KVPair[Int, IncDesIncDes]] must beTrue
      } finally {
        systemServices.close
      }
    }
  }
}
