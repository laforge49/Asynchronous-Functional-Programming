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
import java.util.Properties

class NamedVariableJitTreeMapTest extends SpecificationWithJUnit {
  "JitNamedVariableJitTreeMap" should {
    "Serialize/deserialize" in {
      val properties = _Jits.defaultConfiguration("test")
      val context = new _Jits(properties)

      var i1 = JitInt.createJit(context)
      i1.setInt(-5)
      var b1 = JitBytes.createJit(context)
      b1.setBytes("hi!".getBytes)

      val m1 = JitNamedVariableJitTreeMap.createJit(context)
      m1.size must be equalTo (0)
      var bs = m1.jitToBytes

      val m2 = JitNamedVariableJitTreeMap.createJit(context)
      m2.loadJit(bs)
      m2.size must be equalTo (0)
      m2.put("i1", i1)
      m2.size must be equalTo (1)
      i1 = m2.get("i1").asInstanceOf[JitInt]
      i1.getInt must be equalTo (-5)
      bs = m2.jitToBytes

      val m3 = JitNamedVariableJitTreeMap.createJit(context)
      m3.loadJit(bs)
      m3.size must be equalTo (1)
      i1 = m3.get("i1").asInstanceOf[JitInt]
      i1.getInt must be equalTo (-5)
      i1.setInt(42)
      bs = m3.jitToBytes

      val m4 = JitNamedVariableJitTreeMap.createJit(context)
      m4.loadJit(bs)
      m4.size must be equalTo (1)
      i1 = m4.get("i1").asInstanceOf[JitInt]
      i1.getInt must be equalTo (42)
      m4.remove("i1")
      m4.size must be equalTo (0)
      bs = m4.jitToBytes

      val m5 = JitNamedVariableJitTreeMap.createJit(context)
      m5.loadJit(bs)
      m5.size must be equalTo (0)
      m5.put("b1", b1)
      bs = m5.jitToBytes

      val m6 = JitNamedVariableJitTreeMap.createJit(context)
      m6.loadJit(bs)
      b1 = m6.get("b1").asInstanceOf[JitBytes]
      val b1b = b1.getBytes
      val b1s = new String(b1b)
      b1s must be equalTo("hi!")
    }
  }
}