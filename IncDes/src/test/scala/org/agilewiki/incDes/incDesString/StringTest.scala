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
package incDesString

import org.specs.SpecificationWithJUnit
import blip._
import bind._
import blip.services._

class StringTest extends SpecificationWithJUnit {
  "StringTest" should {
    "Serialize/deserialize" in {
      val j1 = IncDesString(null)
      Future(j1, Set(null, null))
      Future(j1, Length()) must be equalTo (4)
      Future(j1, Value()) must beNull
      var bs = Future(j1, Bytes()).asInstanceOf[Array[Byte]]

      val j2 = IncDesString(null)
      j2.load(bs)
      Future(j2, Value()) must beNull

      val j3 = IncDesString(null)
      Future(j3, Set(null, ""))
      bs = Future(j3, Bytes()).asInstanceOf[Array[Byte]]

      val j4 = IncDesString(null)
      j4.load(bs)
      bs = Future(j4, Bytes()).asInstanceOf[Array[Byte]]

      val j5 = IncDesString(null)
      j5.load(bs)
      Future(j5, Value()) must be equalTo ("")

      val systemServices = SystemServices(new IncDesComponentFactory)
      try {
        val driver = new Driver
        driver.setExchangeMessenger(systemServices.newSyncMailbox)
        Future(driver, DoIt()) must be equalTo ("Hello world!")
      } finally {
        systemServices.close
      }
    }
  }
}

case class DoIt()

class Driver extends Actor {
  bind(classOf[DoIt], doit)

  def doit(msg: AnyRef, rf: Any => Unit) {
    systemServices(Instantiate(INC_DES_STRING_FACTORY_ID, null)) {
      rsp => {
        val j6 = rsp.asInstanceOf[Actor]
        j6(Set(null, "Hello world!")) {
          rsp1 => {
            j6(Copy(null)) {
              rsp2 => {
                val j7 = rsp2.asInstanceOf[Actor]
                j7(Value())(rf)
              }
            }
          }
        }
      }
    }
  }
}
