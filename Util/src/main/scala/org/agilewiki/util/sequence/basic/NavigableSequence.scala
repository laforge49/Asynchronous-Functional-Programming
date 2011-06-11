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
package basic

import java.util.NavigableSet

/**
 * Wraps a SequenceSource around a NavigableSet
 * @param navigableSet The NavigableSet being wrapped.
 */
class NavigableSequence(navigableSet: NavigableSet[String], reverse: Boolean) extends SequenceSource {
  require(navigableSet != null, "NavigableSet must be not null")

  def this(navigableSet: NavigableSet[String]) = this (navigableSet, false)

  override def isReverse = reverse

  var value = if (navigableSet.isEmpty)
    null
  else if (isReverse)
    navigableSet.last
  else
    navigableSet.first

  private def rightNext(key: String) = if (isReverse) navigableSet.lower(key) else navigableSet.higher(key)

  private def closestMatch(key: String) = if (isReverse) navigableSet.floor(key) else navigableSet.ceiling(key)

  override def current = super.current

  override def current(key: String): String = {
    if (navigableSet.isEmpty)
      value = null
    else if (key != null)
      value = closestMatch(key)
    value
  }

  override def next(key: String): String = {
    if (navigableSet.isEmpty) {
      value = null
    } else if (key != null) {
      value = rightNext(key)
    } else if (value != null) {
      value = closestMatch(value)
    }
    if (key != null && key == value) throw new IllegalStateException("next must be not equal")
    value
  }
}
