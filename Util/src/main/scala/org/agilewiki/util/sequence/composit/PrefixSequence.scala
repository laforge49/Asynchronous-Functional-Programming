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

class PrefixSequence(ss: SequenceSource, delimiter: Char) extends SequenceSource {
  require(ss != null, "The super sequence cannot be null")
  private val nextDelimiter = (if(isReverse)delimiter else delimiter + 1).asInstanceOf[Char]
  private var last: String = null

  private def prefix(v: String) = {
    var rv = v
    if (v != null) {
      val i = v.indexOf(delimiter)
      if (i == -1) throw new IllegalStateException("sequence item does not contain delimiter")
      rv = v.substring(0, i)
    }
    rv
  }

  override def isReverse: Boolean = ss.isReverse

  override def current(key: String): String = {
    var rv: String = null
    var k = if (key == null) last else key
    if (k != null) k += delimiter
    rv = prefix(ss.current(k))
    if (rv != null) {
      last = rv
    }
    rv
  }

  override def next(key: String): String = {
    var rv: String = null
    var k = if (key == null) last else key
    if (k != null) k += nextDelimiter
    rv = prefix(ss.next(k))
    if (rv != null) last = rv
    rv
  }

}
