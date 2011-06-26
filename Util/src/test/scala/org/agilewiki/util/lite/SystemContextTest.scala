/*
 * Copyright 2011 Bill La Forge
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
package util
package lite

import org.specs.SpecificationWithJUnit

class SystemContextTest extends SpecificationWithJUnit {
  "SystemContext" should {
    "instantiate Lite" in {
      val tcFactory = new TCFactory
      val systemContext = new SystemContext(tcFactory)
      println(Lite(systemContext))
      systemContext.start
      systemContext.close
    }
  }
}

class TCFactory
  extends SystemComponentFactory {
  val actorFactories = new java.util.HashMap[String, ActorFactory]
  val startMsg = "Hello world!"
  val closeMsg = "Bye bye."

  addDependency(classOf[LiteFactory])

  override def configure(systemContext: SystemContext) {
    val liteFactory = LiteFactory(systemContext)
    println(liteFactory)
  }

  override def instantiate(systemContext: SystemContext) = new TC(systemContext, this)
}

class TC(systemContext: SystemContext, tcFactory: TCFactory)
  extends SystemComponent(systemContext) {

  override def start {
    println(tcFactory.startMsg)
  }

  override def close {
    println(tcFactory.closeMsg)
  }
}
