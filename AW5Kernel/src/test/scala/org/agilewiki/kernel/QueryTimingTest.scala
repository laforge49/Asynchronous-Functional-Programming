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

import java.util.Properties;
import java.io.File

import org.agilewiki.kernel.element.RolonRootElement
import org.agilewiki.kernel.queries.ExistsQuery
import org.agilewiki.kernel.operation.{Eval, Config}

class QueryTimingTest extends SpecificationWithJUnit {
  new File("10.aw5db").delete

  val properties = new Properties()
  DefaultSystemConfiguration(properties, "10.aw5db", "Master")

  ConfigKernel(properties)

  val config: Config = new Config(properties)

  config.role("qttJE")
  config rootElementType ROLON_ROOT_ELEMENT_ROLE_NAME
  config.include(KernelNames.CHANGE_TYPE)
  config.op(new QttEval)

  "QueryTimingTest" should {
    "run 100000 queries " in {
      var systemContext = new _Kernel(properties)
      Kernel(systemContext).processTransaction(new QttFactory)
      var i = 0
      while (i < 100000) {
        i += 1
        ExistsQuery(systemContext, "123")
      }
      Kernel(systemContext).close
    }
  }
}

class QttFactory extends JournalEntryFactory {
  override def journalEntryType = "qttJE"
}

class QttEval extends Eval {
  override def process(
          roleName: String,
          rolonContext: RolonRootElement,
          targetRolon: RolonRootElement) {
  }
}
