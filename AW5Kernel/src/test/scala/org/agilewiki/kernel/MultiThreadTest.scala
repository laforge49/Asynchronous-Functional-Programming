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
import org.agilewiki.kernel.queries.ExistsQuery
import org.agilewiki.kernel.operation.{Eval, Config}

class MultiThreadingTest extends SpecificationWithJUnit {
  new File("MultiThreadingTest.aw5db").delete

  val properties = new Properties()
  //properties.put(DATABASE_PATHNAME,"10.aw5db")
  DefaultSystemConfiguration(properties, "MultiThreadingTest.aw5db", "Master")

  ConfigKernel(properties)

  val config: Config = new Config(properties)

  config.role("mttJE")
  config rootElementType ROLON_ROOT_ELEMENT_ROLE_NAME
  config.include(KernelNames.CHANGE_TYPE)
  config.op(new MttEval)

  "MultiThreadingTest" should {
    "run 10 threads with queries & updates " in {

      val k = new _Kernel(properties)

      class QUThread(n: String) extends Thread {
        override def run {
          var i = 0
          while (i < 10) {
            i += 1
            val nm = n + i
            Kernel(k).processTransaction(new MttFactory(nm))
            ExistsQuery(k, "123_" + UtilNames.PAGE_TYPE) must be equalTo (false)
            ExistsQuery(k, nm + "_" + UtilNames.PAGE_TYPE) must be equalTo (true)
          }
        }
      }

      val t1 = new QUThread("A")
      val t2 = new QUThread("B")
      val t3 = new QUThread("C")
      val t4 = new QUThread("D")
      val t5 = new QUThread("E")
      val t6 = new QUThread("F")
      val t7 = new QUThread("G")
      val t8 = new QUThread("H")
      val t9 = new QUThread("I")
      val t10 = new QUThread("J")
      t1.start
      t2.start
      t3.start
      t4.start
      t5.start
      t6.start
      t7.start
      t8.start
      t9.start
      t10.start
      t1.join
      t2.join
      t3.join
      t4.join
      t5.join
      t6.join
      t7.join
      t8.join
      t9.join
      t10.join
      Kernel(k).close
    }
  }
}

class MttFactory(nm: String) extends JournalEntryFactory {
  override def journalEntryType = "mttJE"

  override def initializeJournalEntry(
          journalEntry: RolonRootElement) {
    journalEntry.attributes.put("nm", nm)
  }
}

class MttEval extends Eval {
  override def process(
          roleName: String,
          rolonContext: RolonRootElement,
          targetRolon: RolonRootElement) {
    val nm = targetRolon.attributes.get("nm")
    targetRolon.transactionContexts.createRolonRootElement(nm + "_" + UtilNames.PAGE_TYPE)
  }
}
