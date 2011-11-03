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

import java.util.concurrent.{ConcurrentLinkedQueue, Semaphore, ThreadFactory}

class MailboxThreadManager(threadCount: Int = 12,
                           threadFactory: ThreadFactory = new MailboxThreadFactory)
  extends ThreadManager with Runnable {
  val semaphore = new Semaphore(0)
  val tasks = new ConcurrentLinkedQueue[Runnable]
  var closing = false

  init

  private def init {
    var c = 0
    while (c < threadCount) {
      c += 1
      threadFactory.newThread(this).start
    }
  }

  override def run() {
    semaphore.acquire
    if (closing) return
    val task = tasks.poll
    try {
      task.run
    } catch {
      case t: Throwable => t.printStackTrace()
    }
  }

  override def process(task: Runnable) {
    tasks.add(task)
    semaphore.release
  }

  def close {
    closing = true
    var c = 0
    while (c < threadCount) {
      c += 1
      semaphore.release
    }
  }
}
