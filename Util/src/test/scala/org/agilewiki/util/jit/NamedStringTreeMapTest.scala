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

class NamedStringTreeMapTest extends SpecificationWithJUnit {
  "JitNamedStringTreeMap" should {
    "Serialize/deserialize" in {
      val properties = _Jits.defaultConfiguration("test")
      val context = new _Jits(properties)

      val v1 = "a"
      val v2 = "bc"
      val v3 = "def"

      val j1 = JitNamedStringTreeMap.createJit(context)
      var bs = j1.jitToBytes

      val j2 = JitNamedStringTreeMap.createJit(context)
      j2.loadJit(bs)
      j2.size must be equalTo (0)
      j2.putString(v1, v1)
      j2.putString(v2, v2)
      j2.removeString(v1)
      j2.size must be equalTo (1)
      bs = j2.jitToBytes

      val j3 = JitNamedStringTreeMap.createJit(context)
      j3.loadJit(bs)
      j3.size must be equalTo (1)
      j3.getString(v1) == null must be equalTo (true)
      j3.getString(v2) must be equalTo (v2)
      j3.getString(v3) == null must be equalTo (true)
    }
  }
}