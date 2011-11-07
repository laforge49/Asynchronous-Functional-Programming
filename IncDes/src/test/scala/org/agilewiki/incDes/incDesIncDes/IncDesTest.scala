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
package incDesIncDes

import org.specs.SpecificationWithJUnit
import blip._
import blip.services._

class IncDesTest extends SpecificationWithJUnit {
  "IncDesTest" should {
    "Serialize/deserialize" in {
      val systemServices = SystemServices(new IncDesComponentFactory)
      try {
        val s1 = IncDesString(null)
        val j1 = IncDesIncDes(null)
        j1.setSystemServices(systemServices)
        Future(j1, Length()) must be equalTo (4)
        Future(j1, Set(null, s1))
        Future(j1, Length()) must be equalTo (18)
        Future(Future(j1, Value()).asInstanceOf[Actor], Value()) must beNull
        Future(s1, Set(null, "!"))
        Future(Future(j1, Value()).asInstanceOf[Actor], Value()) must be equalTo ("!")
        Future(j1, Length()) must be equalTo (20)
        Future(s1, Value()) must be equalTo ("!")
        Future(Future(j1, Value()).asInstanceOf[Actor], Value()) must be equalTo ("!")
        var bs = Future(j1, Bytes()).asInstanceOf[Array[Byte]]

        val j2 = IncDesIncDes(systemServices.newSyncMailbox)
        j2.setSystemServices(systemServices)
        j2.load(bs)
        Future(j2, Length()) must be equalTo (20)
        Future(Future(j2, Value()).asInstanceOf[Actor], Value()) must be equalTo ("!")

        val s3 = IncDesInt(null)
        val j3 = IncDesIncDes(null)
        j3.setSystemServices(systemServices)
        Future(j3, Set(null, s3))
        Future(s3, Set(null, 42))
        bs = Future(j3, Bytes()).asInstanceOf[Array[Byte]]

        val j4 = IncDesIncDes(null)
        j4.setSystemServices(systemServices)
        j4.load(bs)
        bs = Future(j4, Bytes()).asInstanceOf[Array[Byte]]

        val j5 = IncDesIncDes(systemServices.newSyncMailbox)
        j5.setSystemServices(systemServices)
        j5.load(bs)
        Future(Future(j5, Value()).asInstanceOf[Actor], Value()) must be equalTo (42)

        val driver = new Driver
        driver.setSystemServices(systemServices)
        driver.setMailbox(systemServices.newSyncMailbox)
        Future(Future(driver, DoIt()).asInstanceOf[Actor], Value()) must be equalTo (123456789987654321L)
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
    systemServices(Instantiate(INC_DES_INCDES_FACTORY_ID, systemServices.newSyncMailbox)) {
      rsp => {
        val j6 = rsp.asInstanceOf[Actor]
        systemServices(Instantiate(INC_DES_LONG_FACTORY_ID, null)) {
          rsp0 => {
            val s6 = rsp0.asInstanceOf[Actor]
            j6(Set(null, s6)) {
              rsp1 => {
                s6(Set(null, 123456789987654321L)) {
                  rsp2 => {
                    j6(Copy(systemServices.newSyncMailbox)) {
                      rsp3 => {
                        val j7 = rsp3.asInstanceOf[Actor]
                        j7(Value())(rf)
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
