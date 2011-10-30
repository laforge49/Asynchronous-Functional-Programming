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
package blip

import java.util.ArrayList
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.Semaphore

trait SyncMailbox extends Mailbox {
  private val atomicControl = new AtomicReference[Mailbox]
  private val idle = new Semaphore(1)

  override def control = atomicControl.get

  override protected def receive(blkmsg: ArrayList[MailboxMsg]) {
    while (!atomicControl.compareAndSet(null, this)) {
      idle.acquire
      idle.release
    }
    try {
      super.receive(blkmsg)
    } finally {
      atomicControl.set(null)
    }
  }

  private def dispatch(content: AnyRef,
                       responseFunction: Any => Unit,
                       messageFunction: (AnyRef, Any => Unit) => Unit) {
    if (responseFunction == null) messageFunction(content, AnyRef => {})
    else messageFunction(content, responseFunction)
  }

  override def sendReq(bound: Bound,
                       targetActor: Actor,
                       content: AnyRef,
                       responseFunction: Any => Unit,
                       srcMailbox: Mailbox,
                       messageFunction: (AnyRef, Any => Unit) => Unit) {
    val controllingMailbox = srcMailbox.control
    if (controllingMailbox == control) {
      dispatch(content, responseFunction, messageFunction)
    } else if (!atomicControl.compareAndSet(null, controllingMailbox))
      srcMailbox.asyncSendReq(bound, targetActor, content, responseFunction)
    else {
      idle.acquire
      try {
        dispatch(content, responseFunction, messageFunction)
      } finally {
        atomicControl.set(null)
        idle.release
      }
    }
  }
}