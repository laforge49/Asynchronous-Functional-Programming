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
package org.agilewiki
package kernel

import org.specs.SpecificationWithJUnit

import java.util.Properties
import util.UtilNames
;
import java.io.File

import org.agilewiki.kernel.element.RolonRootElement
import org.agilewiki.kernel.operation.{Eval, Config}

/**
 * Unit tests for the System functionalities
 *
 * @author Alex K.
 */

class DummyFactory extends JournalEntryFactory {
  override def journalEntryType = "dummyJE"
}

class DummySTEval extends Eval {
  override def process(
          roleName: String,
          rolonContext: RolonRootElement,
          targetRolon: RolonRootElement) {
  }
}

class Dummy1Factory extends JournalEntryFactory {
  override def journalEntryType = "dummyJE1"
}

class DummySTEval1 extends Eval {
  override def process(
          roleName: String,
          rolonContext: RolonRootElement,
          targetRolon: RolonRootElement) {
    val transactionContexts = targetRolon.transactionContexts
    transactionContexts.createRolonRootElement("dummyRolon1_" + UtilNames.PAGE_TYPE)
  }
}

class Dummy2Factory extends JournalEntryFactory {
  override def journalEntryType = "dummyJE2"
}

class DummySTEval2 extends Eval {
  override def process(
          roleName: String,
          rolonContext: RolonRootElement,
          targetRolon: RolonRootElement) {
    val transactionContexts = targetRolon.transactionContexts
    transactionContexts.createRolonRootElement("dummyRolon1_" + UtilNames.PAGE_TYPE)
    throw new Exception("test exception")
  }
}

class Dummy3Factory extends JournalEntryFactory {
  override def journalEntryType = "dummyJE3"
}

class DummySTEval3 extends Eval {
  override def process(
          roleName: String,
          rolonContext: RolonRootElement,
          targetRolon: RolonRootElement) {
    val transactionContexts = targetRolon.transactionContexts
    val testRolon = transactionContexts.rolonRootElement("dummyRolon1_" + UtilNames.PAGE_TYPE)
    if (testRolon == null)
      throw new NullPointerException("rolon is null")
  }
}

class Dummy4Factory extends JournalEntryFactory {
  override def journalEntryType = "dummyJE4"
}

class DummySTEval4 extends Eval {
  override def process(
          roleName: String,
          rolonContext: RolonRootElement,
          targetRolon: RolonRootElement) {
    val transactionContexts = targetRolon.transactionContexts
    transactionContexts.delete("dummyRolon1_" + UtilNames.PAGE_TYPE)
  }
}

class SystemTest extends SpecificationWithJUnit {
  "SystemTest" should {
    "return property count " in {
      new File("10-pc.aw5db").delete

      val properties = new Properties()
      //properties.put(DATABASE_PATHNAME,"10.aw5db")
      DefaultSystemConfiguration(properties, "10-pc.aw5db", "Master")

      ConfigKernel(properties)

      val config: Config = new Config(properties)

      config.role("dummyJE")
      config rootElementType ROLON_ROOT_ELEMENT_ROLE_NAME
      config.include(KernelNames.CHANGE_TYPE)
      config.op(new DummySTEval)

      config.role("dummyJE1")
      config rootElementType ROLON_ROOT_ELEMENT_ROLE_NAME
      config.include(KernelNames.CHANGE_TYPE)
      config.op(new DummySTEval1)

      config.role("dummyJE2")
      config rootElementType ROLON_ROOT_ELEMENT_ROLE_NAME
      config.include(KernelNames.CHANGE_TYPE)
      config.op(new DummySTEval2)

      config.role("dummyJE3")
      config rootElementType ROLON_ROOT_ELEMENT_ROLE_NAME
      config.include(KernelNames.CHANGE_TYPE)
      config.op(new DummySTEval3)

      config.role("dummyJE4")
      config rootElementType ROLON_ROOT_ELEMENT_ROLE_NAME
      config.include(KernelNames.CHANGE_TYPE)
      config.op(new DummySTEval4)

      var systemContext = new _Kernel(properties)
      Kernel(systemContext).totalAddressMapSize must be equalTo (1)
      Kernel(systemContext).close
    }

    "return property count after process a transaction" in {
      new File("10-pca.aw5db").delete

      val properties = new Properties()
      //properties.put(DATABASE_PATHNAME,"10.aw5db")
      DefaultSystemConfiguration(properties, "10-pca.aw5db", "Master")

      ConfigKernel(properties)

      val config: Config = new Config(properties)

      config.role("dummyJE")
      config rootElementType ROLON_ROOT_ELEMENT_ROLE_NAME
      config.include(KernelNames.CHANGE_TYPE)
      config.op(new DummySTEval)

      config.role("dummyJE1")
      config rootElementType ROLON_ROOT_ELEMENT_ROLE_NAME
      config.include(KernelNames.CHANGE_TYPE)
      config.op(new DummySTEval1)

      config.role("dummyJE2")
      config rootElementType ROLON_ROOT_ELEMENT_ROLE_NAME
      config.include(KernelNames.CHANGE_TYPE)
      config.op(new DummySTEval2)

      config.role("dummyJE3")
      config rootElementType ROLON_ROOT_ELEMENT_ROLE_NAME
      config.include(KernelNames.CHANGE_TYPE)
      config.op(new DummySTEval3)

      config.role("dummyJE4")
      config rootElementType ROLON_ROOT_ELEMENT_ROLE_NAME
      config.include(KernelNames.CHANGE_TYPE)
      config.op(new DummySTEval4)

      var systemContext = new _Kernel(properties)
      Kernel(systemContext).totalAddressMapSize must be equalTo (1)
      Kernel(systemContext).processTransaction(new DummyFactory)
      Kernel(systemContext).close
    }

    "return property count after process a transaction which creates block element" in {
      new File("10-pcab.aw5db").delete

      val properties = new Properties()
      //properties.put(DATABASE_PATHNAME,"10.aw5db")
      DefaultSystemConfiguration(properties, "10-pcab.aw5db", "Master")

      ConfigKernel(properties)

      val config: Config = new Config(properties)

      config.role("dummyJE")
      config rootElementType ROLON_ROOT_ELEMENT_ROLE_NAME
      config.include(KernelNames.CHANGE_TYPE)
      config.op(new DummySTEval)

      config.role("dummyJE1")
      config rootElementType ROLON_ROOT_ELEMENT_ROLE_NAME
      config.include(KernelNames.CHANGE_TYPE)
      config.op(new DummySTEval1)

      config.role("dummyJE2")
      config rootElementType ROLON_ROOT_ELEMENT_ROLE_NAME
      config.include(KernelNames.CHANGE_TYPE)
      config.op(new DummySTEval2)

      config.role("dummyJE3")
      config rootElementType ROLON_ROOT_ELEMENT_ROLE_NAME
      config.include(KernelNames.CHANGE_TYPE)
      config.op(new DummySTEval3)

      config.role("dummyJE4")
      config rootElementType ROLON_ROOT_ELEMENT_ROLE_NAME
      config.include(KernelNames.CHANGE_TYPE)
      config.op(new DummySTEval4)

      var systemContext = new _Kernel(properties)
      Kernel(systemContext).totalAddressMapSize must be equalTo (1)
      Kernel(systemContext).processTransaction(new Dummy1Factory)
      Kernel(systemContext).close
    }

    "return property count after process a transaction which creates block element and throws exception" in {
      new File("10-pcabe.aw5db").delete

      val properties = new Properties()
      //properties.put(DATABASE_PATHNAME,"10.aw5db")
      DefaultSystemConfiguration(properties, "10-pcabe.aw5db", "Master")

      ConfigKernel(properties)

      val config: Config = new Config(properties)

      config.role("dummyJE")
      config rootElementType ROLON_ROOT_ELEMENT_ROLE_NAME
      config.include(KernelNames.CHANGE_TYPE)
      config.op(new DummySTEval)

      config.role("dummyJE1")
      config rootElementType ROLON_ROOT_ELEMENT_ROLE_NAME
      config.include(KernelNames.CHANGE_TYPE)
      config.op(new DummySTEval1)

      config.role("dummyJE2")
      config rootElementType ROLON_ROOT_ELEMENT_ROLE_NAME
      config.include(KernelNames.CHANGE_TYPE)
      config.op(new DummySTEval2)

      config.role("dummyJE3")
      config rootElementType ROLON_ROOT_ELEMENT_ROLE_NAME
      config.include(KernelNames.CHANGE_TYPE)
      config.op(new DummySTEval3)

      config.role("dummyJE4")
      config rootElementType ROLON_ROOT_ELEMENT_ROLE_NAME
      config.include(KernelNames.CHANGE_TYPE)
      config.op(new DummySTEval4)

      var systemContext = new _Kernel(properties)
      Kernel(systemContext).totalAddressMapSize must be equalTo (1)
      Kernel(systemContext).close
    }
    "delete test " in {
      new File("10-deleteTest.aw5db").delete

      val properties = new Properties()
      //properties.put(DATABASE_PATHNAME,"10.aw5db")
      DefaultSystemConfiguration(properties, "10-deleteTest.aw5db", "Master")

      ConfigKernel(properties)

      val config: Config = new Config(properties)

      config.role("dummyJE")
      config rootElementType ROLON_ROOT_ELEMENT_ROLE_NAME
      config.include(KernelNames.CHANGE_TYPE)
      config.op(new DummySTEval)

      config.role("dummyJE1")
      config rootElementType ROLON_ROOT_ELEMENT_ROLE_NAME
      config.include(KernelNames.CHANGE_TYPE)
      config.op(new DummySTEval1)

      config.role("dummyJE2")
      config rootElementType ROLON_ROOT_ELEMENT_ROLE_NAME
      config.include(KernelNames.CHANGE_TYPE)
      config.op(new DummySTEval2)

      config.role("dummyJE3")
      config rootElementType ROLON_ROOT_ELEMENT_ROLE_NAME
      config.include(KernelNames.CHANGE_TYPE)
      config.op(new DummySTEval3)

      config.role("dummyJE4")
      config rootElementType ROLON_ROOT_ELEMENT_ROLE_NAME
      config.include(KernelNames.CHANGE_TYPE)
      config.op(new DummySTEval4)

      // create rolon test
      var systemContext = new _Kernel(properties)
      Kernel(systemContext).totalAddressMapSize must be equalTo (1)
      Kernel(systemContext).processTransaction(new Dummy1Factory)
      Kernel(systemContext).close
      // fetch rolon test
      systemContext = new _Kernel(properties)
      Kernel(systemContext).processTransaction(new Dummy3Factory)
      Kernel(systemContext).close
      // delete rolon test
      systemContext = new _Kernel(properties)
      Kernel(systemContext).processTransaction(new Dummy4Factory)
      Kernel(systemContext).close
      // fetch rolon test
      systemContext = new _Kernel(properties)
      Kernel(systemContext).close
    }
  }
}