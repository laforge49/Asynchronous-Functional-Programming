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

case class AddString(transactionContext: TransactionContext, value: String)

class MashupComponent(actor: Actor) extends Component(actor) {
  val incDesNavMap = actor.asInstanceOf[IncDesNavMap[String, IncDesIncDes]]
  var _title: IncDesString = null

  bind(classOf[Title], title)
  bind(classOf[SetTitle], setTitle)

  def title(msg: AnyRef, rf: Any => Unit) {
    getTitle(null) {
      r1 => {
        if (r1 == null) {
          rf(null)
          return
        }
        r1(Value())(rf)
      }
    }
  }

  def setTitle(msg: AnyRef, rf: Any => Unit) {
    val st = msg.asInstanceOf[SetTitle]
    val transactionContext = st.transactionContext
    val t = st.title
    getTitle(transactionContext) {
      r1 => {
        r1(Set(transactionContext, t))(rf)
      }
    }
  }

  def getTitle(transactionContext: TransactionContext)(rf: IncDesString => Unit) {
    if (_title != null) {
      rf(_title)
      return
    }
    actor(Get("title")) {
      r1 => {
        if (r1 != null) {
          _getTitle(transactionContext, r1.asInstanceOf[IncDesIncDes], rf)
          return
        }
        if (transactionContext == null) {
          rf(null)
          return
        }
        val titleHolder = incDesNavMap.newValue
        actor(Put(transactionContext, "title", titleHolder)) {
          r2 => {
            _getTitle(transactionContext, titleHolder, rf)
            return
          }
        }
      }
    }
  }

  def _getTitle(transactionContext: TransactionContext,
                titleHolder: IncDesIncDes,
                rf: IncDesString => Unit) {
    titleHolder(Value()) {
      r1 => {
        if (r1 != null) {
          _title = r1.asInstanceOf[IncDesString]
          rf(_title)
          return
        }
        if (transactionContext == null) {
          rf(null)
          return
        }
        _title = IncDesString(mailbox)
        titleHolder(Set(transactionContext, _title)){
          r2 => {
            rf(_title)
          }
        }
      }
    }
  }
}

object MashupComponentFactory extends ComponentFactory {
  override def instantiate(actor: Actor) = new MashupComponent(actor)
}

class MashupFactory extends SubordinateStringIncDesMapFactory(FactoryId("mashup")) {
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
      Future(mashup1, SetTitle(SimpleTransactionContext, "123"))
      Future(mashup1, Title()) must be equalTo("123")
      val bs1 = Future(mashup1, Bytes()).asInstanceOf[Array[Byte]]

      val mashup2 = mashupFactory.newActor(new Mailbox).asInstanceOf[IncDes]
      mashup2.setSystemServices(systemServices)
      mashup2.load(bs1)
      Future(mashup2, Title()) must be equalTo("123")
      Future(mashup2, SetTitle(SimpleTransactionContext, "42"))
      Future(mashup2, Title()) must be equalTo("42")
      val bs2 = Future(mashup2, Bytes()).asInstanceOf[Array[Byte]]

      val mashup3 = mashupFactory.newActor(new Mailbox).asInstanceOf[IncDes]
      mashup3.setSystemServices(systemServices)
      mashup3.load(bs2)
      Future(mashup3, Title()) must be equalTo("42")
    }
  }
}
