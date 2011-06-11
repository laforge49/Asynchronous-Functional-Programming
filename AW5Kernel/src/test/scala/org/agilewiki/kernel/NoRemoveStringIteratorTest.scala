/*
 * Copyright 2009 Alex K.
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
package kernel

import org.specs.SpecificationWithJUnit

import java.util.List
import java.util.ArrayList
import java.util.Iterator

/**
 * Specification for the NoRemoveStringIterator functionality
 */

class NoRemoveStringIteratorTest extends SpecificationWithJUnit {
  val strings: List[String] = new ArrayList()
  strings.add("first")
  strings.add("second")
  strings.add("third")
  strings.add("fourth")

  val iterator: Iterator[String] = strings.iterator()
  val nrsi = new NoRemoveStringIterator(iterator)

  "NoRemoveStringIterator" should {
    "look for existance of next element" in {
      nrsi.hasNext() must be equalTo (true)
    }

    "return next element" in {
      nrsi.next() must be equalTo ("first")
      nrsi.next() must be equalTo ("second")
    }

    "remove throws exception" in {
      nrsi.remove() must throwA(new UnsupportedOperationException())
    }
  }
}
