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
package dbSeq

import blip._
import seq._
import incDes._

class DbStringSeq[V](db: Actor, pathname: String)
  extends Sequence[String, V] {
  private var pn = pathname

  setSystemServices(db)
  if (pn.startsWith("/")) pn = pn.substring(1)
  if (!pn.endsWith("/") && pn.length > 0) pn = pn + "/"

  setMailbox(db.mailbox)

  override def first(msg: AnyRef, rf: Any => Unit) {
    val je = (new FirstRequestFactory).newActor(null)
    je(Set(null, pn)) {
      rsp =>
        db(TransactionRequest(je.asInstanceOf[IncDes]))(rf)
    }
  }

  override def current(msg: AnyRef, rf: Any => Unit) {
    val key = msg.asInstanceOf[Current[String]].key
    val jef = new CurrentStringRequestFactory
    jef.configure(systemServices)
    val je = jef.newActor(null)
    je.setSystemServices(systemServices)
    val chain = new Chain
    chain.op(je, PutString(null, "pathname", pn))
    chain.op(je, PutString(null, "key", key))
    chain.op(db, TransactionRequest(je.asInstanceOf[IncDes]))
    this(chain)(rf)
  }

  override def next(msg: AnyRef, rf: Any => Unit) {
    val key = msg.asInstanceOf[Next[String]].key
    val jef = new NextStringRequestFactory
    jef.configure(systemServices)
    val je = jef.newActor(null)
    je.setSystemServices(systemServices)
    val chain = new Chain
    chain.op(je, PutString(null, "pathname", pn))
    chain.op(je, PutString(null, "key", key))
    chain.op(db, TransactionRequest(je.asInstanceOf[IncDes]))
    this(chain)(rf)
  }
}

class DbLongSeq[V](db: Actor, pathname: String)
  extends Sequence[String, V] {
  private var pn = pathname

  setSystemServices(db)
  if (pn.startsWith("/")) pn = pn.substring(1)
  if (!pn.endsWith("/") && pn.length > 0) pn = pn + "/"

  setMailbox(db.mailbox)

  override def first(msg: AnyRef, rf: Any => Unit) {
    val je = (new FirstRequestFactory).newActor(null)
    je(Set(null, pn)) {
      rsp =>
        db(TransactionRequest(je.asInstanceOf[IncDes]))(rf)
    }
  }

  override def current(msg: AnyRef, rf: Any => Unit) {
    val key = msg.asInstanceOf[Current[Long]].key
    val jef = new CurrentLongRequestFactory
    jef.configure(systemServices)
    val je = jef.newActor(null)
    je.setSystemServices(systemServices)
    val chain = new Chain
    chain.op(je, PutString(null, "pathname", pn))
    chain.op(je, PutLong(null, "key", key))
    chain.op(db, TransactionRequest(je.asInstanceOf[IncDes]))
    this(chain)(rf)
  }

  override def next(msg: AnyRef, rf: Any => Unit) {
    val key = msg.asInstanceOf[Next[Long]].key
    val jef = new NextLongRequestFactory
    jef.configure(systemServices)
    val je = jef.newActor(null)
    je.setSystemServices(systemServices)
    val chain = new Chain
    chain.op(je, PutString(null, "pathname", pn))
    chain.op(je, PutLong(null, "key", key))
    chain.op(db, TransactionRequest(je.asInstanceOf[IncDes]))
    this(chain)(rf)
  }
}

class DbIntSeq[V](db: Actor, pathname: String)
  extends Sequence[String, V] {
  private var pn = pathname

  setSystemServices(db)
  if (pn.startsWith("/")) pn = pn.substring(1)
  if (!pn.endsWith("/") && pn.length > 0) pn = pn + "/"

  setMailbox(db.mailbox)

  override def first(msg: AnyRef, rf: Any => Unit) {
    val je = (new FirstRequestFactory).newActor(null)
    je(Set(null, pn)) {
      rsp =>
        db(TransactionRequest(je.asInstanceOf[IncDes]))(rf)
    }
  }

  override def current(msg: AnyRef, rf: Any => Unit) {
    val key = msg.asInstanceOf[Current[Int]].key
    val jef = new CurrentIntRequestFactory
    jef.configure(systemServices)
    val je = jef.newActor(null)
    je.setSystemServices(systemServices)
    val chain = new Chain
    chain.op(je, PutString(null, "pathname", pn))
    chain.op(je, PutInt(null, "key", key))
    chain.op(db, TransactionRequest(je.asInstanceOf[IncDes]))
    this(chain)(rf)
  }

  override def next(msg: AnyRef, rf: Any => Unit) {
    val key = msg.asInstanceOf[Next[Int]].key
    val jef = new NextIntRequestFactory
    jef.configure(systemServices)
    val je = jef.newActor(null)
    je.setSystemServices(systemServices)
    val chain = new Chain
    chain.op(je, PutString(null, "pathname", pn))
    chain.op(je, PutInt(null, "key", key))
    chain.op(db, TransactionRequest(je.asInstanceOf[IncDes]))
    this(chain)(rf)
  }
}
