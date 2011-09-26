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
package db
package transactions

import blip._
import seq._
import incDes._
import blocks._

class DbStringSeq[V](db: Actor, pathname: String)
  extends Sequence[String, V] {

  override def first(msg: AnyRef, rf: Any => Unit) {
    val je = (new FirstRootRequestFactory).newActor(null)
    db(TransactionRequest(je.asInstanceOf[IncDes]))(rf)
  }

  override def current(msg: AnyRef, rf: Any => Unit) {
    val key = msg.asInstanceOf[Current[String]].key
    val je = (new CurrentRootRequestFactory).newActor(null)
    je(Set(null, key)) {
      rsp => db(TransactionRequest(je.asInstanceOf[IncDes]))(rf)
    }
  }

  override def next(msg: AnyRef, rf: Any => Unit) {
    val key = msg.asInstanceOf[Next[String]].key
    val je = (new NextRootRequestFactory).newActor(null)
    je(Set(null, key)) {
      rsp => db(TransactionRequest(je.asInstanceOf[IncDes]))(rf)
    }
  }
}

class FirstRootRequestFactory
  extends Factory(DBT_SEQ_FIRST) {
  override protected def instantiate = {
    val req = new IncDes
    addComponent(new QueryRequestComponent(req))
    addComponent(new FirstRootRequestComponent(req))
    req
  }
}

class FirstRootRequestComponent(actor: Actor)
  extends Component(actor) {
  bindSafe(classOf[Process], new ChainFactory(process))

  private def process(msg: AnyRef, chain: Chain) {
    chain.op(systemServices, DbRoot(), "dbRoot")
    chain.op(Unit => chain("dbRoot"), Value(), "items")
    chain.op(Unit => chain("items"), ValuesSeq(), "seq")
    chain.op(Unit => chain("seq"), First(), "item")
    chain.op(Unit => chain("item"), Copy(null))
 }
}

class CurrentRootRequestFactory
  extends Factory(DBT_SEQ_CURRENT) {
  override protected def instantiate = {
    val req = new IncDesString
    addComponent(new QueryRequestComponent(req))
    addComponent(new CurrentRootRequestComponent(req))
    req
  }
}

class CurrentRootRequestComponent(actor: Actor)
  extends Component(actor) {
  bindSafe(classOf[Process], new ChainFactory(process))

  private def process(msg: AnyRef, chain: Chain) {
    chain.op(actor, Value(), "key")
    chain.op(systemServices, DbRoot(), "dbRoot")
    chain.op(Unit => chain("dbRoot"), Value(), "items")
    chain.op(Unit => chain("items"), ValuesSeq(), "seq")
    chain.op(Unit => chain("seq"), Unit => Current(chain("key")), "item")
    chain.op(Unit => chain("item"), Copy(null))
 }
}

class NextRootRequestFactory
  extends Factory(DBT_SEQ_NEXT) {
  override protected def instantiate = {
    val req = new IncDesString
    addComponent(new QueryRequestComponent(req))
    addComponent(new NextRootRequestComponent(req))
    req
  }
}

class NextRootRequestComponent(actor: Actor)
  extends Component(actor) {
  bindSafe(classOf[Process], new ChainFactory(process))

  private def process(msg: AnyRef, chain: Chain) {
    chain.op(actor, Value(), "key")
    chain.op(systemServices, DbRoot(), "dbRoot")
    chain.op(Unit => chain("dbRoot"), Value(), "items")
    chain.op(Unit => chain("items"), ValuesSeq(), "seq")
    chain.op(Unit => chain("seq"), Unit => Next(chain("key")), "item")
    chain.op(Unit => chain("item"), Copy(null))
 }
}
