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

class NamedJitListTest extends SpecificationWithJUnit {
  "JitNamedJitList" should {
    "Serialize/deserialize" in {
      val properties = _Jits.defaultConfiguration("test")
      val context = new _Jits(properties)

      var i1 = JitString.createJit(context)
      i1.setString("abc")

      val m1 = JitNamedJitList.createJit(context)
      m1.size must be equalTo(0)
      var bs = m1.jitToBytes

      val m2 = JitNamedJitList.createJit(context)
      m2.loadJit(bs)
      m2.size must be equalTo(0)
      m2.put("i1", i1)
      m2.size must be equalTo(1)
      i1 = m2.get("i1").asInstanceOf[JitString]
      i1.getString must be equalTo("abc")
      bs = m2.jitToBytes

      val m3 = JitNamedJitList.createJit(context)
      m3.loadJit(bs)
      m3.size must be equalTo(1)
      i1 = m3.get("i1").asInstanceOf[JitString]
      i1.getString must be equalTo("abc")
      i1.setString("coo")
      i1 = m3.get("i1").asInstanceOf[JitString]
      i1.getString must be equalTo("coo")
      bs = m3.jitToBytes

      val m4 = JitNamedJitList.createJit(context)
      m4.loadJit(bs)
      m4.size must be equalTo(1)
      i1 = m4.get("i1").asInstanceOf[JitString]
      i1.getString must be equalTo("coo")
      m4.remove("i1")
      m4.size must be equalTo(0)
      bs = m4.jitToBytes

      val m5 = JitNamedJitList.createJit(context)
      m5.loadJit(bs)
      m5.size must be equalTo(0)
      i1 = JitString.createJit(context)
      i1.setString("aa")
      m5.put("a", i1)
      i1 = JitString.createJit(context)
      i1.setString("bb")
      m5.put("b", i1)
      i1 = JitString.createJit(context)
      i1.setString("cc")
      m5.put("c", i1)
      m5.size must be equalTo (3)
      m5.indexOf("a") must be equalTo(0)
      m5.indexOf("b") must be equalTo(1)
      m5.indexOf("c") must be equalTo(2)
      bs = m5.jitToBytes

      val m6 = JitNamedJitList.createJit(context)
      m6.loadJit(bs)
      m6.size must be equalTo(3)
      m6.move("a", "b", true)
      m6.size must be equalTo(3)
      m6.indexOf("b") must be equalTo(0)
      m6.indexOf("a") must be equalTo(1)
      m6.indexOf("c") must be equalTo(2)
      bs = m6.jitToBytes


      val m7 = JitNamedJitList.createJit(context)
      m7.loadJit(bs)
      m7.size must be equalTo(3)
      m7.indexOf("b") must be equalTo(0)
      m7.indexOf("a") must be equalTo(1)
      m7.indexOf("c") must be equalTo(2)
    }
  }
}