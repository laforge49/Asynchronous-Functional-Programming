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

class TAActor(reactor: LiteReactor, text: String) extends LiteActor(reactor) {
  def print {
    println(text)
  }
}

class taActorFactory(name: FactoryName, text: String) extends ActorFactory(name) {
  def instantiate(reactor: LiteReactor): LiteActor = {
    new TAActor(reactor, text)
  }
}

class TAFactory
  extends SystemComponentFactory {
  val actorFactories = new java.util.HashMap[String, ActorFactory]

  addDependency(classOf[LiteFactory])

  override def configure(systemContext: SystemContext) {
    val liteFactory = LiteFactory(systemContext)
    liteFactory.addFactory(new taActorFactory(FactoryName("a"), "Apple"))
    liteFactory.addFactory(new taActorFactory(FactoryName("b"), "Boy"))
  }

  override def instantiate(systemContext: SystemContext) = new TA(systemContext, this)
}

class TA(systemContext: SystemContext, tcFactory: TAFactory)
  extends SystemComponent(systemContext) {
}

class ActorFactoryTest extends SpecificationWithJUnit {
  "ActorFactory" should {
    "configure actors" in {
      val taFactory = new TAFactory
      val systemContext = new SystemContext(taFactory)
      val lite = Lite(systemContext)
      val reactor = systemContext.newReactor
      val actor1 = lite.newActor(FactoryName("a"), reactor).asInstanceOf[TAActor]
      println(actor1.factoryName.value)
      actor1.print
      val actor2 = lite.newActor(FactoryName("b"), reactor).asInstanceOf[TAActor]
      println(actor2.factoryName.value)
      actor2.print
    }
  }
}
