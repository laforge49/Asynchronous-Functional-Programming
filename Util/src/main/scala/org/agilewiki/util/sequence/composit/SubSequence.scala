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
package util
package sequence
package composit

/**
 * Provides access to the keys in a sequence which begin with a given prefix.
 * Note however that the prefix is removed from the keys that are returned by this class.
 * @param ss The SequenceSource to be wrapped.
 * Note also that keys equal to the prefix are also excluded from the sequence of
 * returned keys.
 * @param prefix The key prefix used to select the keys and which is removed from the 
 * keys that are returned.
 */
class SubSequence(ss: SequenceSource, prefix: String) extends SequenceSource {
  require(ss != null, "The super sequence cannot be null")
  require(prefix != null, "The prefix cannot be null")

  if (isReverse) {
    var pk = ss.current
    while (pk != null && !pk.startsWith(prefix)) {
      pk = ss.next(pk)
    }
    if (pk == prefix) pk = ss next pk
  } else ss.next(prefix)

  override def isReverse = ss.isReverse

  override def current = super.current

  override def current(key: String): String = {
    val crt = ss.current(if (key == null) null else (prefix + key))
    if (crt == null)
      null
    else if (!crt.startsWith(prefix))
      null
    else if (key != null && (if (isReverse) crt > (prefix + key) else crt < (prefix + key)))
      throw new IllegalStateException("The underlying sequence is not sorted")
    else crt.substring(prefix.length)
  }

  override def next(key: String): String = {
    val nxt = ss.next(if (key == null) null else (prefix + key))
    if (nxt == null)
      null
    else if (!nxt.startsWith(prefix))
      null
    else if (key != null && (if (isReverse) nxt >= (prefix + key) else nxt <= (prefix + key)))
      throw new IllegalStateException("The underlying sequence may be not sorted or has duplicates")
    else nxt.substring(prefix.length)
  }
}
