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
import org.agilewiki.kernel.element.RolonRootElement
import org.agilewiki.kernel.operation.{Config, Eval}
import util.UtilNames

class PopulateDatabaseJE extends Eval {
  override def process(role: String, context: RolonRootElement, target: RolonRootElement) {
    for (i <- 1 to 100) {
      val rolon = target.transactionContexts.createRolonRootElement("Test" + target.uuid + "Rolon-" + i + "_" + UtilNames.PAGE_TYPE)
      rolon.attributes.put("serial", String.valueOf(i))
      rolon.attributes.put("comment", "This rolon is created for test purpose")

    }
  }
}

object PopulateDatabaseJE {
  def apply(properties: Properties) {
    val config = new Config(properties)
    config role "PopulateDatabaseJE"
    config include KernelNames.CHANGE_TYPE
    config rootElementType ROLON_ROOT_ELEMENT_ROLE_NAME
    config op (new PopulateDatabaseJE)
  }
}
