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
package incDesInt

import org.specs.SpecificationWithJUnit
import blip._
import blip.services._

class IntTest extends SpecificationWithJUnit {
  "IntTest" should {
    "Serialize/deserialize" in {
      val j1 = IncDesInt(null)
      Future(j1, Set(null, 32))
      Future(j1, Length()) must be equalTo (4)
      Future(j1, Value()) must be equalTo (32)
      var bs = Future(j1, Bytes()).asInstanceOf[Array[Byte]]

      val j2 = IncDesInt(null)
      j2.load(bs)
      Future(j2, Value()) must be equalTo (32)

      val j3 = IncDesInt(null)
      Future(j3, Set(null, -4))
      bs = Future(j3, Bytes()).asInstanceOf[Array[Byte]]

      val j4 = IncDesInt(null)
      j4.load(bs)
      bs = Future(j4, Bytes()).asInstanceOf[Array[Byte]]

      val j5 = IncDesInt(null)
      j5.load(bs)
      Future(j5, Value()) must be equalTo (-4)

      val systemServices = SystemServices(new IncDesComponentFactory)
      val driver = new Driver
      driver.setSystemServices(systemServices)
      Future(driver, DoIt()) must be equalTo (42)
    }
  }
}

case class DoIt()

class Driver extends Actor {
  bind(classOf[DoIt], doit)
  setMailbox(new Mailbox)

  def doit(msg: AnyRef, rf: Any => Unit) {
    val results = new Results
    val chain = new Chain(results)
    chain.add(systemServices, Instantiate(INC_DES_INT_FACTORY_ID, null), "incDesInt")
    chain.addFuncs(Unit => results("incDesInt"), Unit => Set(null, 42))
    chain.addFuncs(Unit => results("incDesInt"), Unit => Copy(null), "clone")
    chain.addFuncs(Unit => results("clone"), Unit => Value())
    this(chain)(rf)
  }
}
