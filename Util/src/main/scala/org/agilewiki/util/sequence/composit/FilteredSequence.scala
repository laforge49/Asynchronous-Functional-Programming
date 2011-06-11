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
 * Filters out unwanted keys.
 * @param ss The SequenceSource object being wrapped.
 * @param filter A function which must approve the positioning of the sequence. Positioning
 * continues to advance untill the filter function returns true.
 */
class FilteredSequence(ss: SequenceSource, filter: (String) => Boolean) extends SequenceSource {
  require(ss != null, "The wrapped sequence cannot be null")
  require(filter != null, "The filter function cannot be null")

  current(null)

  override def isReverse = ss.isReverse

  override def current = super.current

  override def current(key: String): String = {
    var _key = ss.current(key)
    while (_key != null && !filter(_key)) {
      _key = ss.next(_key)
    }
    _key
  }

  override def next(key: String): String = {
    var _key = ss.next(key)
    while (_key != null && !filter(_key)) {
      _key = ss.next(_key)
    }
    _key
  }
}
