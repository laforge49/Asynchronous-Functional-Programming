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
import incDes._
import blocks._
import batch._

object RecordSize {
  def apply(db: SystemServices, batch: IncDes, recordKey: String, pathname: String) = {
    var pn = pathname
    if (!pn.startsWith("/")) pn = "/" + pn
    if (!pn.endsWith("/")) pn = pn + "/"
    pn = recordKey+pn
    val jef = new RecordSizeFactory
    jef.configure(db)
    val je = jef.newActor(null).asInstanceOf[IncDes]
    je.setSystemServices(db)
    val chain = new Chain
    if (batch != null) chain.op(db, Unit => RecordLock(batch, recordKey))
    chain.op(je, Set(null, pn))
    chain.op(db, TransactionRequest(je))
    chain
  }
}

class RecordSizeFactory extends IncDesStringFactory(DBT_RECORD_SIZE) {
  override protected def instantiate = {
    val req = super.instantiate
    addComponent(new QueryRequestComponent(req))
    addComponent(new RecordSizeComponent(req))
    req
  }
}

class RecordSizeComponent(actor: Actor)
  extends Component(actor) {
  bindMessageLogic(classOf[Process], new ChainFactory(process))

  private def process(msg: AnyRef, chain: Chain) {
    chain.op(actor, Value(), "pathname")
    chain.op(systemServices, RecordsPathname(), "recordsPathname")
    chain.op(systemServices, DbRoot(), "dbRoot")
    chain.op(
      Unit => chain("dbRoot"),
      Unit => {
        val rp = chain("recordsPathname").asInstanceOf[String]
        val p = chain("pathname").asInstanceOf[String]
        Resolve(rp + p)
      }, "tuple")
    chain.op(Unit => {
      val (value, key) = chain("tuple").asInstanceOf[(IncDes, String)]
      value
    }, Size())
  }
}
