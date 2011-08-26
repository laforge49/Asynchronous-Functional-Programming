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
package transactions

import blip._
import annotation.tailrec

class TransactionProcessor extends Actor {
  var activityLevel = 0
  var transactionContext: TransactionContext = null
  val pending = new java.util.ArrayDeque[MailboxReq]
  val active = new java.util.HashSet[MailboxReq]

  def isInvalid = activityLevel < 0

  def isActive = activityLevel > 0

  def isIdle = activityLevel == 0

  def maxLevel = {
    var level = 0
    val it = active.iterator
    while (it.hasNext) {
      val l = it.next.binding.asInstanceOf[Transaction].level
      if (l > level) level = l
    }
    level
  }

  def addActive(mailboxReq: MailboxReq) {
    val l = mailboxReq.binding.asInstanceOf[Transaction].level
    if (l > activityLevel) activityLevel = l
    active.add(mailboxReq)
  }

  def removeActive(mailboxReq: MailboxReq) {
    val l = mailboxReq.binding.asInstanceOf[Transaction].level
    active.remove(mailboxReq)
    if (l == activityLevel) activityLevel = maxLevel
  }

  def isCompatible(mailboxReq: MailboxReq) =
    mailboxReq.binding.asInstanceOf[Transaction].maxCompatibleLevel >= activityLevel

  def addPending(mailboxReq: MailboxReq) {
    pending.addLast(mailboxReq)
    runPending
  }

  @tailrec final def runPending {
    val mailboxReq = pending.peekFirst
    if (mailboxReq == null) return
    if (!isCompatible(mailboxReq)) return
    pending.removeFirst
    addActive(mailboxReq)
    val transaction = mailboxReq.binding.asInstanceOf[Transaction]
    transaction.processTransaction(mailbox, mailboxReq)
    runPending
  }
}