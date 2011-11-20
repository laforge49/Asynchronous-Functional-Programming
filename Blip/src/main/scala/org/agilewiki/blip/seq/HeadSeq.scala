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
package seq

class HeadSeq[K, V](seq: Sequence[K, V], limit: K)
  extends Sequence[K, V] {
  setMailbox(seq.exchangeMessenger)
  setSystemServices(seq.systemServices)

  override def first(msg: AnyRef, rf: Any => Unit) {
    seq.first(msg, rsp => h(rsp, rf))
  }

  override def current(msg: AnyRef, rf: Any => Unit) {
    val key = msg.asInstanceOf[Current[K]].key
    if (comparator.compare(key, limit) >= 0) rf(null)
    else seq.current(msg, rsp => h(rsp, rf))
  }

  override def next(msg: AnyRef, rf: Any => Unit) {
    val key = msg.asInstanceOf[Next[K]].key
    if (comparator.compare(key, limit) >= 0) rf(null)
    else seq.next(msg, rsp => h(rsp, rf))
  }

  private def h(msg: Any, rf: Any => Unit) {
    if (msg == null) rf(null)
    else if (comparator.compare(msg.asInstanceOf[KVPair[K, V]].key, limit) < 0) rf(msg)
    else rf(null)
  }

  override protected def _comparator = seq.comparator
}
