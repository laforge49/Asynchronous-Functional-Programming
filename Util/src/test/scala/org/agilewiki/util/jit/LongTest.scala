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

class LongTest extends SpecificationWithJUnit {
  "JitLong" should {
    "Serialize/deserialize" in {
      val properties = _Jits.defaultConfiguration("test")
      val context = new _Jits(properties)

      val j1 = JitLong.createJit(context)
      j1.setLong(32)
      j1.jitByteLength must be equalTo (j1.longByteLength)
      var bs = j1.jitToBytes

      val j2 = JitLong.createJit(context)
      j2.loadJit(bs)
      j2.getLong must be equalTo (32)

      val j3 = JitLong.createJit(context)
      j3.setLong(-4)
      bs = j3.jitToBytes

      val j4 = JitLong.createJit(context)
      j4.loadJit(bs)
      bs = j4.jitToBytes

      val j5 = JitLong.createJit(context)
      j5.loadJit(bs)
      j5.getLong must be equalTo (-4)
    }
  }
}