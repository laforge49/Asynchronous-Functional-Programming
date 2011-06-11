/*
 * Copyright 2009 Bill La Forge
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

import java.util.Iterator

/**
 * Wraps an Iterator[String] around a SequenceSource
 * @param ss The SequenceSource to be wrapped.
 */
case class SequenceIterator(sequence: SequenceSource) extends Iterator[String] {
  require(sequence != null, "The wrapped sequence cannot be null")

  /**
   * Sequence position
   */
  private var _after: String = null

  /**
   * True when the end has been reached.
   */
  private var atEnd = false

  /**
   * True when the sequence has been positioned
   */
  private var positioned = false

  /**
   * Position the sequence just after the last key accessed
   */
  private def position {
    if (!positioned) {
      positioned = true
      atEnd = sequence.next(_after) == null
    }
  }

  /**
   * Test for the end of the sequence.
   * @return True when not at the end of the sequence.
   */
  def hasNext: Boolean = {
    position
    !atEnd
  }

  /**
   * Fetches the next key in the sequence
   * @return The next key.
   */
  def next: String = {
    position
    positioned = false
    if (atEnd) {
      throw new NoSuchElementException
    }
    _after = sequence.current
    _after
  }

  /**
   * Remove a key from the sequence--not supported.
   */
  def remove {
    throw new UnsupportedOperationException
  }

}

