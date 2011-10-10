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
package mashup

import org.specs.SpecificationWithJUnit
import blip._
import seq._

case class Title()

case class SetTitle(transactionContext: TransactionContext, title: String)

case class Strings()

case class AddString(transactionContext: TransactionContext, string: String)

case class GetString(index: Int)

class MashupComponent(actor: Actor) extends Component(actor) {

  bind(classOf[Title], title)
  bind(classOf[SetTitle], setTitle)
  bind(classOf[Strings], strings)
  bind(classOf[AddString], addString)
  bind(classOf[GetString], getString)

  def title(msg: Any, rf: Any => Unit) {
    actor(GetValue2("title"))(rf)
  }

  def setTitle(msg: AnyRef, rf: Any => Unit) {
    val st = msg.asInstanceOf[SetTitle]
    val transactionContext = st.transactionContext
    val t = st.title
    actor(PutString(transactionContext, "title", t))(rf)
  }

  def strings(msg: AnyRef, rf: Any => Unit) {
    actor(GetValue("strings"))(rf)
  }

  def addString(msg: AnyRef, rf: Any => Unit) {
    val st = msg.asInstanceOf[AddString]
    val transactionContext = st.transactionContext
    val t = st.string
    actor(MakePutMakeSet(transactionContext, "strings", INC_DES_STRING_LIST_FACTORY_ID)) {
      r1 => {
        val strings = r1.asInstanceOf[IncDesList[IncDesString, String]]
        val ids = IncDesString(mailbox)
        strings(Add[IncDesString, String](transactionContext, ids)) {
          r2 => {
            ids(Set(transactionContext, t))(rf)
          }
        }
      }
    }
  }

  def getString(msg: AnyRef, rf: Any => Unit) {
    val st = msg.asInstanceOf[GetString]
    val index = st.index
    actor(GetValue("strings")) {
      r1 => {
        if (r1 == null) {
          rf(null)
          return
        }
        val strings = r1.asInstanceOf[IncDesList[IncDesString, String]]
        strings(GetValue(index))(rf)
      }
    }
  }
}

object MashupComponentFactory extends ComponentFactory {
  override def instantiate(actor: Actor) = new MashupComponent(actor)
}

class MashupFactory extends IncDesStringIncDesMapFactory(FactoryId("mashup")) {
  include(MashupComponentFactory)
}

class MashupTest extends SpecificationWithJUnit {
  "MashupTest" should {
    "exercise" in {
      val systemServices = SystemServices(new IncDesComponentFactory)
      val mashupFactory = new MashupFactory
      mashupFactory.configure(systemServices)

      val mashup1 = mashupFactory.newActor(new Mailbox)
      mashup1.setSystemServices(systemServices)
      Future(mashup1, Title()) must beNull
      Future(mashup1, SetTitle(null, "123"))
      Future(mashup1, Title()) must be equalTo ("123")
      Future(mashup1, MakePut(null, "nada"))
      Future(mashup1, GetString(0)) must beNull
      Future(mashup1, AddString(null, "Laundry"))
      Future(mashup1, GetString(0)) must be equalTo("Laundry")
      val bs1 = Future(mashup1, Bytes()).asInstanceOf[Array[Byte]]

      val mashup2 = mashupFactory.newActor(new Mailbox).asInstanceOf[IncDes]
      mashup2.setSystemServices(systemServices)
      mashup2.load(bs1)
      Future(mashup2, Title()) must be equalTo ("123")
      Future(mashup2, SetTitle(null, "42"))
      Future(mashup2, Title()) must be equalTo ("42")
      Future(mashup2, AddString(null, null))
      Future(mashup2, AddString(null, "Dishes"))
      Future(mashup2, GetString(0)) must be equalTo("Laundry")
      Future(mashup2, GetString(1)) must beNull
      Future(mashup2, GetString(2)) must be equalTo("Dishes")
      val bs2 = Future(mashup2, Bytes()).asInstanceOf[Array[Byte]]

      val mashup3 = mashupFactory.newActor(new Mailbox).asInstanceOf[IncDes]
      mashup3.setSystemServices(systemServices)
      mashup3.load(bs2)
      Future(mashup3, Title()) must be equalTo ("42")
      Future(mashup3, GetString(2)) must be equalTo("Dishes")
      Future(mashup3, GetString(1)) must beNull
      Future(mashup3, GetString(0)) must be equalTo("Laundry")
      println("")
      val mashupSeq = Future(mashup3, Seq()).asInstanceOf[Sequence[String, IncDesIncDes]]
      println("mashupSeq:")
      Future(mashupSeq, Loop((key: String, item: IncDesIncDes) => println(key+" "+item)))
      println("")
      val mashupValuesSeq = Future(mashup3, ValuesSeq()).asInstanceOf[Sequence[String, IncDes]]
      println("mashupValuesSeq:")
      Future(mashupValuesSeq, Loop((key: String, value: IncDes) => println(key+" "+value)))
      println("")
      val flatMashupValuesSeq = Future(mashup3, FlatValuesSeq()).asInstanceOf[Sequence[String, IncDes]]
      println("flatMashupValuesSeq:")
      Future(flatMashupValuesSeq, Loop((key: String, value: IncDes) => println(key+" "+value)))
      println("")
      val mashupStrings = Future(mashup3, Strings()).asInstanceOf[IncDesList[IncDesString, String]]
      val stringsSeq = Future(mashupStrings, Seq()).asInstanceOf[Sequence[Int, IncDesString]]
      println("stringsSeq:")
      Future(stringsSeq, Loop((key: Int, item: IncDesString) => println(key+" "+item)))
      println("")
      val stringValuesSeq = Future(mashupStrings, ValuesSeq()).asInstanceOf[Sequence[Int, String]]
      println("stringValuesSeq:")
      Future(stringValuesSeq, Loop((key: Int, value: String) => println(key+" "+value)))
      println("")
      val flatStringValuesSeq = Future(mashupStrings, FlatValuesSeq()).asInstanceOf[Sequence[Int, String]]
      println("flatStringValuesSeq:")
      Future(flatStringValuesSeq, Loop((key: Int, value: String) => println(key+" "+value)))
      println("")
    }
  }
}
