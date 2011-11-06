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

object RecordGet {
  def apply(db: SystemServices, batch: IncDes, recordKey: String, pathname: String) = {
    var pn = pathname
    if (!pn.startsWith("/")) pn = "/" + pn
    if (!pn.endsWith("/")) pn = pn + "/"
    pn = recordKey + pn
    val jef = new RecordGetFactory
    jef.configure(db)
    val je = jef.newActor(null).asInstanceOf[IncDes]
    je.setSystemServices(db)
    val chain = new Chain
    chain.op(je, Set(null, pn))
    chain.op(db, TransactionRequest(je), "tuple")
    chain.op(db, Unit => LeftReq(chain("tuple").asInstanceOf[(Any, Any)]), "lockTimestamp")
    chain.op(batch, Unit => ValidateTimestamp(recordKey, chain("lockTimestamp").asInstanceOf[Long]))
    chain.op(db, Unit => RightReq(chain("tuple").asInstanceOf[(Any, Any)]))
    chain
  }
}

class RecordGetFactory extends IncDesStringFactory(DBT_RECORD_GET) {
  override protected def instantiate = {
    val req = super.instantiate
    addComponent(new QueryRequestComponent(req))
    addComponent(new RecordGetComponent(req))
    req
  }
}

class RecordGetComponent(actor: Actor)
  extends Component(actor) {
  bind(classOf[Process], process)

  private def process(msg: AnyRef, rf: Any => Unit) {
    val chain = new Chain
    chain.op(actor, Value(), "pathname")
    chain.op(systemServices, Unit => {
      val pathname = chain("pathname").asInstanceOf[String]
      val i = pathname.indexOf("/")
      val recordKey = pathname.substring(0, i)
      GetRecord(recordKey)
    }, "record")
    chain.op(Unit => chain("record"), GetTimestamp(), "timestamp")
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
    }, Copy(null))
    actor(chain) {
      rsp => {
        val timestamp = chain("timestamp").asInstanceOf[Long]
        rf((timestamp, rsp))
      }
    }
  }
}
