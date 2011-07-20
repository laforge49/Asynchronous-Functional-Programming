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

import java.util.ArrayDeque

/**
 * An intersection of multiple SequenceSource objects.
 */
class IntersectionSequence extends SequenceSource {
  private var reverse = false

  /**
   * A list of unpositioned SequenceSource objects
   */
  private val candidates = new ArrayDeque[SequenceSource]

  /**
   * A list of SequenceSource objects all positioned to the same key
   */
  private val positioned = new ArrayDeque[SequenceSource]

  /**
   * Makes all the SequenceSource objects candidates.
   */
  private def reset {
    candidates.addAll(positioned)
    positioned.clear
  }

  /**
   * Adds a SequenceSource object to the set of sequences to be
   * processed.
   * @param ss The object being added.
   */
  def add(ss: SequenceSource) {
    if (ss == null)
      throw new IllegalArgumentException("Cannot add a null sequence")
    if (positioned.isEmpty) {
      if (candidates.isEmpty)
        reverse = ss.isReverse
      else if (ss.isReverse != reverse)
        throw new IllegalStateException(
          "All sequences must have the same direction"
          )
      candidates.addLast(ss)
    } else {
      throw new UnsupportedOperationException(
        "Sequences can not be added after position has been called")
    }
  }

  override def isReverse: Boolean = reverse

  override def current = super.current

  override def current(key: String): String = {
    var nxt = key
    if (key != null) {
      reset
      while (nxt != null && !candidates.isEmpty) {
        nxt = candidates.peekLast.current(nxt)
        if (nxt == null) {
          reset
        } else {
          var ss = candidates.removeLast
          if (!positioned.isEmpty) {
            var lst = positioned.peekLast.current
            if (if (isReverse) lst > nxt else lst < nxt)
              reset
            else if (if (isReverse) lst < nxt else lst > nxt)
              throw new IllegalStateException("Unsorted sequence detected")
          }
          positioned add ss
        }
      }
    } else lastIntersection
    if (positioned.isEmpty) null else positioned.peekLast.current
  }

  override def next(key: String): String = {
    var nxt = key
    if (key != null) {
      reset
      var found = true
      while (found && nxt != null && !candidates.isEmpty) {
        nxt = if (nxt == key) candidates.peekLast.next(nxt)
        else candidates.peekLast.current(nxt)
        if (nxt == null) {
          reset
        } else {
          var ss = candidates.removeLast
          if (!positioned.isEmpty) {
            var lst = positioned.peekLast.current
            found = lst == nxt
            if (if (isReverse) lst < nxt else lst > nxt)
              nxt = lst
          }
          positioned add ss
        }
      }
      if (!found) current(nxt)
    } else lastIntersection
    if (positioned.isEmpty) null else positioned.peekLast.current
  }

  private def lastIntersection {
    if (positioned.isEmpty && !candidates.isEmpty) {
      val pk = candidates.peekLast.current
      if (pk != null) current(pk)
    }
  }
}
