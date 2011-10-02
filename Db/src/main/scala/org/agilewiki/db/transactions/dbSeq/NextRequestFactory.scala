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
import incDes._
import blocks._

class NextStringRequestFactory
  extends Factory(DBT_SEQ_STRING_NEXT) {
  override protected def instantiate = {
    val req = new IncDesString
    addComponent(new QueryRequestComponent(req))
    addComponent(new NextRequestComponent(req))
    req
  }
}

class NextLongRequestFactory
  extends Factory(DBT_SEQ_LONG_NEXT) {
  override protected def instantiate = {
    val req = new IncDesLong
    addComponent(new QueryRequestComponent(req))
    addComponent(new NextRequestComponent(req))
    req
  }
}

class NextIntRequestFactory
  extends Factory(DBT_SEQ_INT_NEXT) {
  override protected def instantiate = {
    val req = new IncDesInt
    addComponent(new QueryRequestComponent(req))
    addComponent(new NextRequestComponent(req))
    req
  }
}