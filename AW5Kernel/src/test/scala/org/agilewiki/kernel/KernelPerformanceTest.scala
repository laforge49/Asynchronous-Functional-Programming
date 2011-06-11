/*
 * Copyright 2010 M. NAJI
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

import journal.logging.BuildLoggingProperties
import org.specs.SpecificationWithJUnit

import java.util.Properties
import java.io.File
import util.UtilNames
;
import org.agilewiki.kernel.element.RolonRootElement
import org.agilewiki.kernel.operation.{Eval, Config}

class NewRolonJournalEntry extends Eval {
  override def process(
          roleName: String,
          rolonContext: RolonRootElement,
          targetRolon: RolonRootElement) {
    var rolonUuid = targetRolon.attributes.get("rolonUuid")
    if (rolonUuid == null) {
      rolonUuid = "testRolon" + Count.ndx
      Count.ndx += 1
      TransactionContext().createRolonRootElement(rolonUuid + "_" + UtilNames.PAGE_TYPE)
      targetRolon.attributes.put("rolonUuid", rolonUuid)
    }
    else {
      TransactionContext().createRolonRootElement(rolonUuid + "_" + UtilNames.PAGE_TYPE)
    }
  }
}

object Count {
  var ndx = 0
}

class KernelPerformanceNewRolonFactory extends JournalEntryFactory {
  override def journalEntryType = "newRolonJE"
}

class KernelPerformanceTest extends SpecificationWithJUnit {
  val nbr = 100
  "kernel" should {
    "process "+nbr+" transactions" in {
      new File("KernelPerformanceTest0.aw5db").delete
      new File("KernelPerformanceTest0.jnl").delete

      val properties = new Properties()
      DefaultSystemConfiguration(properties, "KernelPerformanceTest0.aw5db", "Master")
      BuildLoggingProperties(properties, "KernelPerformanceTest0.jnl")
      ConfigKernel(properties)

      val config: Config = new Config(properties)

      config role "newRolonJE"
      config rootElementType ROLON_ROOT_ELEMENT_ROLE_NAME
      config include KernelNames.CHANGE_TYPE
      config op new NewRolonJournalEntry

      var systemContext = new _Kernel(properties)
      val t0 = System.currentTimeMillis
      for (i <- 0 to nbr) {
        Kernel(systemContext) processTransaction new KernelPerformanceNewRolonFactory
      }
      val t1 = System.currentTimeMillis
      systemContext.close
      println("tps: "+ ((nbr * 1000)/(t1 - t0)))
    }

    "recover "+nbr+" transactions" in {
      new File("KernelPerformanceTest1.aw5db").delete

      val properties = new Properties()
      DefaultSystemConfiguration(properties, "KernelPerformanceTest1.aw5db", "Master")
      ConfigKernel(properties)

      val config: Config = new Config(properties)

      config role "newRolonJE"
      config rootElementType ROLON_ROOT_ELEMENT_ROLE_NAME
      config include KernelNames.CHANGE_TYPE
      config op new NewRolonJournalEntry

      var systemContext = new _KernelRecover(properties)
      val t0 = System.currentTimeMillis
      Kernel(systemContext).recover("KernelPerformanceTest0.jnl")
      val t1 = System.currentTimeMillis
      systemContext.close
      println("tps (recovery): "+ ((nbr * 1000)/(t1 - t0)))
    }
  }
}
