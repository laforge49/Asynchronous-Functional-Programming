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
package incDes
package examples
package hit

import org.specs.SpecificationWithJUnit
import blip._
import bind._

case class Hit()

class HitComponent(actor: Actor) extends Component(actor) {
  bind(classOf[Hit], hit)

  def hit(msg: AnyRef, rf: Any => Unit) {
    actor(Value()) {
      rsp1 => {
        val i = rsp1.asInstanceOf[Int] + 1
        println("# of hits: " + i)
        actor(Set(null, i))(rf)
      }
    }
  }
}

object HitComponentFactory extends ComponentFactory {
  override def instantiate(actor: Actor) = new HitComponent(actor)
}

class HitFactory extends IncDesIntFactory(FactoryId("hit")) {
  include(HitComponentFactory)
}

class HitTest extends SpecificationWithJUnit {
  "HitTest" should {
    "count hits" in {
      val hitFactory = new HitFactory

      val hit1 = hitFactory.newActor(null)
      Future(hit1, Hit())
      val bs = Future(hit1, Bytes()).asInstanceOf[Array[Byte]]

      val hit2 = hitFactory.newActor(null).asInstanceOf[IncDes]
      hit2.load(bs)
      Future(hit2, Hit())
    }
  }
}
