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
package incDesNavSet

import org.specs.SpecificationWithJUnit
import blip._
import seq._

class NavSetTest extends SpecificationWithJUnit {
  "NavSetTest" should {
    "Serialize/deserialize" in {
      val systemServices = SystemServices(new IncDesComponentFactory)

      val intl0 = IncDesIntSet(new ReactorMailbox, systemServices)
      Future(intl0, AddValue(null, 1))
      Future(intl0, Length()) must be equalTo (8)
      val intb0 = Future(intl0, Bytes()).asInstanceOf[Array[Byte]]

      val intl1 = IncDesIntSet(new ReactorMailbox, systemServices)
      intl1.load(intb0)
      Future(intl1, Length()) must be equalTo (8)
      Future(intl1, ContainsKey(1)) must be equalTo (true)

      val seq = Future(intl1, Seq()).asInstanceOf[Sequence[Int, Int]]
      Future(seq, First()).isInstanceOf[KVPair[Int, Int]] must be equalTo (true)

      val longl0 = IncDesLongSet(new ReactorMailbox, systemServices)
      Future(longl0, AddValue(null, 1L))
      Future(longl0, AddValue(null, 2L))
      Future(longl0, Length()) must be equalTo (20)
      val longb0 = Future(longl0, Bytes()).asInstanceOf[Array[Byte]]

      val longl1 = IncDesLongSet(new ReactorMailbox, systemServices)
      longl1.load(longb0)
      Future(longl1, Length()) must be equalTo (20)
      Future(longl1, ContainsKey(1L)) must be equalTo (true)
      Future(longl1, ContainsKey(2L)) must be equalTo (true)

      val stringl0 = IncDesStringSet(new ReactorMailbox, systemServices)
      Future(stringl0, AddValue(null, "a"))
      Future(stringl0, AddValue(null, "bb"))
      Future(stringl0, Remove(null, "a"))
      Future(stringl0, Length()) must be equalTo (12)
      val stringb0 = Future(stringl0, Bytes()).asInstanceOf[Array[Byte]]

      val stringl1 = IncDesStringSet(new ReactorMailbox, systemServices)
      stringl1.load(stringb0)
      Future(stringl1, Length()) must be equalTo (12)
      Future(stringl1, ContainsKey("a")) must be equalTo (false)
      Future(stringl1, ContainsKey("bb")) must be equalTo (true)
    }
  }
}
