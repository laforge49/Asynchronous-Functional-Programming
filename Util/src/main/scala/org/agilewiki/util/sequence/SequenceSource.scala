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

/**
 * Provides a sequence of keys in ascending order.
 * <p>
 * This trait is used for high-speed intersection and union operations.
 */
trait SequenceSource extends Comparable[SequenceSource] {
  def isReverse = false

  /**
   * Gets the current key of the sequence. returns null if the sequence is empty.
   * @return the current sequence key or null
   */
  def current: String = current(null)

  /**
   * Positions the sequence to the given key, if it exists, and returns
   * it.
   * @param key The key used to determine the position of the sequence
   * @return The key next to the given key if it exists. Null if it doesn't.
   */
  def current(key: String): String

  /**
   * Positions the sequence next to the given key, if it exists, and returns
   * it.
   * @param key The key used to determine the position of the sequence
   * @return The key next to the given key if it exists. Null if it doesn't.
   */
  def next(key: String): String

  override def equals(x: Any): Boolean = {
    x.isInstanceOf[SequenceSource] &&
            (x.asInstanceOf[SequenceSource].current == current)
  }

  override def compareTo(ss: SequenceSource): Int = {
    try {
      current.compareTo(ss.current)
    } catch {
      case ex: NullPointerException => throw new NoSuchElementException
    }
  }
}
