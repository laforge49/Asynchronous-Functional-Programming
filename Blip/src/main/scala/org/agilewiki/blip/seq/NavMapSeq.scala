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

import java.util.{Comparator, NavigableMap}

class NavMapSeq[K, V](mailbox: Mailbox, factory: Factory, navigableMap: NavigableMap[K, V])
  extends Sequence[K, V](mailbox, factory) {

  override def first(msg: AnyRef, rf: Any => Unit) {
    if (navigableMap.isEmpty) rf(null)
    else {
      val entry = navigableMap.firstEntry
      rf(KVPair(entry.getKey, entry.getValue))
    }
  }

  override def current(msg: AnyRef, rf: Any => Unit) {
    if (navigableMap.isEmpty) rf(null)
    else {
      var key = msg.asInstanceOf[Current[K]].key
      val entry = navigableMap.ceilingEntry(key)
      if (entry == null) rf(null)
      else rf(KVPair(entry.getKey, entry.getValue))
    }
  }

  override def next(msg: AnyRef, rf: Any => Unit) {
    if (navigableMap.isEmpty) rf(null)
    else {
      var key = msg.asInstanceOf[Next[K]].key
      val entry = navigableMap.higherEntry(key)
      if (entry == null) rf(null)
      else rf(KVPair(entry.getKey, entry.getValue))
    }
  }

  override protected def _comparator: Comparator[_ >: K] = {
    val c = navigableMap.comparator
    if (c != null) c
    else super._comparator
  }
}