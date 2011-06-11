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
package org.agilewiki.util
package jit

import sequence.{SequenceIterator, SequenceSource}
import sequence.basic.NavigableSequence

abstract class JitNamedJitTreeMap extends Jit {

  protected def wrapper(cursor: JitMutableCursor, name: String): Jit

  protected def wrapper(jit: Jit, name: String): Jit

  protected var treeMap = new java.util.TreeMap[String, Jit]
  protected var length = intByteLength

  def iterator = treeMap.keySet.iterator

  override def loadJit(cursor: JitMutableCursor) {
    super.loadJit(cursor)
    var s = cursor.readInt
    var i = 0
    while (i < s) {
      i += 1
      val name = cursor.readString
      val value = initialValue(cursor, name)
      treeMap.put(name, value)
    }
    length = cursor.offset - jitCursor.offset
  }

  protected def initialValue(cursor: JitMutableCursor, name: String): Jit = wrapper(cursor, name)

  override def jitByteLength: Int = {
    validateByteLength
    length
  }

  override def jitUpdater(lenDiff: Int, source: Jit) {
    length += lenDiff
    if (debugJit) {
      System.err.println()
      System.err.println("map updater " + lenDiff + " " + source.getClass.getName)
      System.err.println("this " + this)
      System.err.println("this bytelen " + jitByteLength)
    }
    validateByteLength
    jitUpdated(lenDiff, source)
  }

  override protected def serializeJit(cursor: JitMutableCursor) {
    cursor.writeInt(treeMap.size)
    val it = jitIterator
    while (it.hasNext) {
      val name = it.next
      cursor.writeString(name)
      val value = treeMap.get(name)
      value.jitToBytes(cursor)
    }
  }

  override def validateByteLength {
    if (!debugJit) return
    var l = intByteLength
    val it = jitIterator
    while (it.hasNext) {
      val name = it.next
      val value = treeMap.get(name)
      if (value != null) {
        l += stringByteLength(name)
        value.validateByteLength
        l += value.jitByteLength
      }
    }
    println("expecting "+length+" not "+l)
    if (l != length) throw new IllegalStateException("wrong length")
  }

  def size = treeMap.size

  def isEmpty = treeMap.isEmpty

  def contains(name: String) = treeMap.containsKey(name)

  def jitSequence(reverse: Boolean): SequenceSource = new NavigableSequence(treeMap.navigableKeySet, reverse)

  def jitIterator = new SequenceIterator(jitSequence(false))

  def get(name: String): Jit

  def getWrapper(name: String): Jit

  def isJitDeserialized(name: String) = {
    val w = getWrapper(name)
    if (w == null) true
    else w.isJitDeserialized
  }

  def removeWrapper(name: String): Jit

  def remove(name: String): Jit

  def putWrapper(name: String, w: Jit) {
    if (treeMap.get(name) != null) throw new IllegalArgumentException("name already in use: " + name)
    writeLock
    validateByteLength
    treeMap.put(name, w)
    w.partness(this, name, this)
    w.validateByteLength
    if (debugJit) {
      System.err.println()
      System.err.println("put" + (stringByteLength(name) + w.jitByteLength))
      System.err.println("this " + this)
      System.err.println("old map len " + length)
    }
    jitUpdater(stringByteLength(name) + w.jitByteLength, this)
  }

  def put(name: String, jit: Jit) {
    if (treeMap.get(name) != null) throw new IllegalArgumentException("name already in use: " + name)
    writeLock
    validateByteLength
    val w = wrapper(jit, name)
    treeMap.put(name, w)
    w.partness(this, name, this)
    jit.validateByteLength
    if (debugJit) {
      System.err.println()
      System.err.println("put" + (stringByteLength(name) + w.jitByteLength))
      System.err.println("this " + this)
      System.err.println("old map len " + length)
    }
    jitUpdater(stringByteLength(name) + w.jitByteLength, this)
  }

  def add(name: String, roleName: String) = {
    val  jit = Jits(systemContext).createJit(roleName)
    put(name, jit)
    jit
  }

  def make(name: String, roleName: String) = {
    var jit = get(name)
    if (jit == null) jit = add(name, roleName)
    jit
  }

  def floor(name: String) = treeMap.floorKey(name)

  def ceiling(name: String) = treeMap.ceilingKey(name)

  def lower(name: String) = treeMap.lowerKey(name)

  def higher(name: String) = treeMap.higherKey(name)

  def first = treeMap.firstKey

  def last = treeMap.lastKey
}
