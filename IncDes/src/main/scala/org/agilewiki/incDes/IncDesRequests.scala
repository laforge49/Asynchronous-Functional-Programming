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

import blip._

case class Length()

case class Bytes()

case class Copy(mailbox: Mailbox)

case class Value()

case class Set[V](transactionContext: TransactionContext, value: V)

case class Changed(transactionContext: TransactionContext, diff: Int, what: IncDes)

case class Writable(transactionContext: TransactionContext)

case class VisibleElement()

case class AddValue[K](transactionContext: TransactionContext, value: K)

case class Add[V <: IncDesItem[V1], V1](transactionContext: TransactionContext, value: V)

case class Insert[V <: IncDesItem[V1], V1](transactionContext: TransactionContext, index: Int, value: V)

case class Size()

case class Remove[K](transactionContext: TransactionContext, key: K)

case class Seq()

case class Put[K, V <: IncDesItem[V1], V1](transactionContext: TransactionContext, key: K, value: V)

case class MakePut[K](transactionContext: TransactionContext, key: K)

case class MakeSet(transactionContext: TransactionContext, factoryId: FactoryId)

case class GetValue[K](key: K)

case class MakePutSet[K, V1](transactionContext: TransactionContext, key: K, value: V1)

case class MakePutMakeSet[K](transactionContext: TransactionContext, key: K, factoryId: FactoryId)
