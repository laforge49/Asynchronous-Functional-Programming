/*
 * Copyright 2009 Barrie McGuire
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

import element.operation.{ElementConfig, _Elements}
import org.specs.SpecificationWithJUnit

import org.agilewiki.kernel.element.EmbeddedTransientElement
import util.jit.{Jit, Jits}

/**
 * Specification for the AttributesComponent functionality
 */
class AttributesComponentTest extends SpecificationWithJUnit {
  val properties = _Elements.defaultConfiguration("Master")
  new ElementConfig(properties) {
    role("EmbeddedTransientElement")
    kernelElementType(classOf[EmbeddedTransientElement] getName)
  }
  val systemContext = new _Elements(properties)

  "AttributesComponent" should {

    "Add a name/value pair" in {
      val ac = Jits(systemContext).createJit("EmbeddedTransientElement").asInstanceOf[EmbeddedTransientElement]
      ac.partness(null, "ac", null)
      ac.attributes.put("anAttributeName", "12345")
      ac.persistence.getWriteLock must be equalTo (true)
      ac.persistence.getDirty must be equalTo (true)
    }

    "Return a name/valur pair" in {
      val ac = Jits(systemContext).createJit("EmbeddedTransientElement").asInstanceOf[EmbeddedTransientElement]
      ac.partness(null, "ac", null)
      ac.attributes.put("anAttributeName", "abd930d")
      ac.attributes.get("anAttributeName") must be equalTo ("abd930d")
    }

    "Add an integer value" in {
      val ac = Jits(systemContext).createJit("EmbeddedTransientElement").asInstanceOf[EmbeddedTransientElement]
      ac.partness(null, "ac", null)
      ac.attributes.putInt("anIntValue", 567, 0)
      ac.attributes.getInt("anIntValue", 0) must be equalTo (567)
      ac.attributes.getInt("anotherIntValue", 50) must be equalTo (50)
    }

    "Add a long value" in {
      val ac = Jits(systemContext).createJit("EmbeddedTransientElement").asInstanceOf[EmbeddedTransientElement]
      ac.partness(null, "ac", null)
      ac.attributes.putLong("aLongValue", 876543212L, 0)
      ac.attributes.getLong("aLongValue", 0) must be equalTo (876543212L)
    }

    "Remove an integer value" in {
      val ac = Jits(systemContext).createJit("EmbeddedTransientElement").asInstanceOf[EmbeddedTransientElement]
      ac.partness(null, "ac", null)
      ac.attributes.putInt("anIntValue", 100, 0)
      ac.attributes.putInt("anIntValue", 100, 100)
      ac.attributes.getInt("anIntValue", 0) must be equalTo (0)
    }

    "Remove a long value" in {
      val ac = Jits(systemContext).createJit("EmbeddedTransientElement").asInstanceOf[EmbeddedTransientElement]
      ac.partness(null, "ac", null)
      ac.attributes.putLong("aLongValue", 39473527484L, 0)
      ac.attributes.putLong("aLongValue", 0, 0)
      ac.attributes.getLong("aLongValue", 9) must be equalTo (9)
    }

    "Return the current size" in {
      val ac = Jits(systemContext).createJit("EmbeddedTransientElement").asInstanceOf[EmbeddedTransientElement]
      ac.partness(null, "ac", null)
      ac.attributes.size must be equalTo (0)
      ac.attributes.put("first", "value")
      ac.attributes.put("first", "value")
      ac.attributes.put("first", "value")
      ac.attributes.put("second", "value")
      ac.attributes.size must be equalTo (2)
      ac.attributes.put("first", null)
      ac.attributes.size must be equalTo (1)
    }
  }

}
