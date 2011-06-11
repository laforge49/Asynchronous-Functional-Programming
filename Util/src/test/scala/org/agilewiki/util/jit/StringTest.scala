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
package org.agilewiki.util.jit

import org.specs.SpecificationWithJUnit

class StringTest extends SpecificationWithJUnit {
  "JitString" should {
    "Serialize/deserialize" in {
      val properties = _Jits.defaultConfiguration("test")
      val context = new _Jits(properties)
      val v1 = "a"

      val j1 = JitString.createJit(context)
      j1.setString(v1)
      j1.jitByteLength must be equalTo (j1.intByteLength + 2 * v1.length)
      val bs = j1.jitToBytes

      val j2 = JitString.createJit(context)
      j2.loadJit(bs)
      j2.getString must be equalTo ("a")

      val v2 = "abc"
      val j3 = JitString.createJit(context)
      j3.setString(v2)
      j3.jitByteLength must be equalTo (j3.intByteLength + 2 * v2.length)
      val bs1 = j3.jitToBytes

      val j4 = JitString.createJit(context)
      j4.loadJit(bs1)
      val bs2 = j4.jitToBytes

      val j5 = JitString.createJit(context)
      j5.loadJit(bs2)
      j5.getString must be equalTo (v2)
    }
  }
}