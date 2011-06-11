/*
 * Copyright 2010 Bill La Forge
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
package util.cache

import java.lang.ref.WeakReference
import java.lang.ref.ReferenceQueue
import java.util.HashMap

class CanonicalMap[T](maxSize: Int) {
  private val referenceQueue = new ReferenceQueue[T]
  private val hashMap = new HashMap[String, NamedWeakReference[T]]
  private var cache = new Cache[T](maxSize)

  def clear {cache.clear}

  def iterator = hashMap.keySet.iterator

  def accessed(item: T) {
    cache(item)
  }

  def put(name: String, item: T) {
    cache(item)
    val nwr = new NamedWeakReference[T](item, referenceQueue, name)
    hashMap.put(name, nwr)
    var more = true
    while (more) {
      val x = referenceQueue.poll.asInstanceOf[NamedWeakReference[T]]
      if (x == null) {
        more = false
      } else {
        hashMap.remove(x.name)
      }
    }
  }

  def get(name: String): T = {
    val nwr = hashMap.get(name)
    var rv = null.asInstanceOf[T]
    if (nwr != null) {
      rv = nwr.get
      cache(rv)
    }
    rv
  }

  def has(name: String) = get(name) != null

  def remove(name: String) = {
    hashMap.remove(name)
  }
}
