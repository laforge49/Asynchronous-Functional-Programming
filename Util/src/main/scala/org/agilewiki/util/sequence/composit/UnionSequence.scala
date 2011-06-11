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

import java.util.PriorityQueue
import java.util.Comparator

/**
 * A union of multiple SequenceSource objects.
 */
class UnionSequence extends SequenceSource {
  private var reverse = false

  /**
   * An ordered set of SequenceSource objects
   */
  private var pq = new PriorityQueue[SequenceSource]

  /**
   * Remains true until the position method has been called.
   */
  private var initializing = true

  /**
   * Adds a SequenceSource object to the set of sequences to be
   * processed.
   * @param ss The object being added.
   */
  def add(ss: SequenceSource) {
    require(ss != null, "A sequence must be not null")
    if (!initializing) {
      throw new UnsupportedOperationException(
        "Sequences can not be added after position has been called")
    }
    if (ss.current != null) {
      if (pq.isEmpty) {
        reverse = ss.isReverse
        if (reverse)
          pq = new PriorityQueue[SequenceSource](1,
            new Comparator[SequenceSource] {
              override def compare(o1: SequenceSource, o2: SequenceSource): Int =
                o2.compareTo(o1)
            }
            )
      } else if (ss.isReverse != reverse)
        throw new IllegalStateException(
          "All sequences must have the same direction"
          )
      pq.add(ss)
    }
  }

  override def isReverse = reverse

  override def current = super.current

  override def current(key: String): String = {
    def crt = if (pq.isEmpty) null else pq.peek.current
    if (key != null) {
      initializing = false
      var pk = crt
      while (!pq.isEmpty && pk != null && (if (isReverse) pk > key else pk < key)) {
        var ss = pq.poll
        if (ss.current(key) != null) {
          pq add ss
          pk = crt
        }
      }
    }
    crt
  }

  override def next(key: String): String = {
    def crt = if (pq.isEmpty) null else pq.peek.current
    if (key != null) {
      initializing = false
      var pk = crt
      while (!pq.isEmpty && pk != null && (if (isReverse) pk >= key else pk <= key)) {
        var ss = pq.poll
        if (ss.next(key) != null) {
          pq add ss
          pk = crt
        }
      }
    }
    crt
  }
}


object UnionSequence {
  def apply(ss:SequenceSource*): UnionSequence ={
    val us = new UnionSequence
    for(s <- ss){
      us.add(s)
    }
    us
  }
}