/*
 * Copyright 2010 M.Naji
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
package journal
package logging

import java.util.Properties
import org.specs.SpecificationWithJUnit
import java.io._

class JournalLogTest extends SpecificationWithJUnit {
  "Journal Log System should" should {
    "Log Journal Entries" in {
      new File("JournalLogTest.aw5db").delete
      new File("JournalLogTest.aw5db").exists must be equalTo false
      MultiFileLogger.emptyDirectory("JournalLogTest")

      val properties = new Properties()
      BuildBasicProperties(properties, "JournalLogTest.aw5db")
      MultiFileLogger(properties, "JournalLogTest")
      ConfigKernel(properties)
      PopulateDatabaseJE(properties)

      var systemContext = new _Kernel(properties)
      for (i <- 1 to 3)
        Kernel(systemContext).processTransaction(new SimpleJournalEntryFactory("PopulateDatabaseJE", "user2"))
      systemContext.close

      systemContext = new _Kernel(properties)
      Kernel(systemContext).processTransaction(new CustomJEBuilderJournalEntryFactory("PopulateDatabaseJE", "user1"))
      systemContext.close

      systemContext = new _Kernel(properties)
      for (i <- 1 to 10) Kernel(systemContext).processTransaction(new SimpleJournalEntryFactory("PopulateDatabaseJE", "user2"))
      systemContext.close

      new File("JournalLogTest.aw5db").exists must be equalTo true
    }

    "Recover Journal Entries" in {
      new File("JournalLogTest1.aw5db").delete
      new File("JournalLogTest.aw5db").exists must be equalTo true
      new File("JournalLogTest1.aw5db").exists must be equalTo false

      val properties = new Properties
      BuildBasicProperties(properties, "JournalLogTest1.aw5db")
      ConfigKernel(properties)
      PopulateDatabaseJE(properties)

      val systemContext = new _KernelRecover(properties)
      Kernel(systemContext).recoverJnlFiles("JournalLogTest")
      systemContext.close
      new File("JournalLogTest1.aw5db").exists must be equalTo true
    }
  }
}
