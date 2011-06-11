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
package kernel
package component

import java.io.DataInput
import util.jit.{JitMutableCursor, Jit, JitNamedStringTreeMap}
import util.jit.structure.JitElement
import element.operation.ElementRole
import element.{KernelRootElement, Element}

trait ContentsComponent extends JitElement {
  this: Element =>

  private var _jitAttributes: JitNamedStringTreeMap = _
  protected val _contents = defineContents
  protected var length = 0
  protected var dser = true
  protected var built = false

  def elementRole = {
    jitRole.asInstanceOf[ElementRole]
  }

  protected def defineContents = new Contents

  def contents = _contents

  override def isJitDeserialized = dser

  override def jitByteLength: Int = {
    if (length == 0) length = builder
    intByteLength + length
  }

  protected def jitAttributes = {
    deserialize
    _jitAttributes
  }

  protected def builder = {
    _jitAttributes = JitNamedStringTreeMap.createJit(systemContext)
    _jitAttributes.partness(this, null, this)
    built = true
    _jitAttributes.jitByteLength
  }

  def deserialize {
//    if (ContentsComponent.this.isInstanceOf[KernelRootElement]) println("ContentsComponent deserialize")
    if (length == 0) length = builder
    else if (!built) builder
    if (dser) return
    dser = true
    val transactionContexts = Kernel(systemContext).transactionContexts
    var capture = false
    if (transactionContexts != null) {
      capture = transactionContexts.capture
      transactionContexts.capture = false
    }
    val cursor = jitCursor.mutable
    elementLoader(cursor)
    if (transactionContexts != null) {
      transactionContexts.capture = capture
    }
    load
  }

  def elementLoader(cursor: JitMutableCursor) {
    super.loadJit(cursor)
    cursor.skip(intByteLength)
    _jitAttributes.loadJit(cursor)
  }

  override def loadJit(cursor: JitMutableCursor) {
    super.loadJit(cursor)
    length = cursor.readInt
    if (length < 1) throw new IllegalStateException("invalid element length")
    cursor.skip(length)
    dser = false
  }

  override def jitUpdated(lenDiff: Int, source: Jit) {
    length += lenDiff
    if (jitContainer == null) {
      if (getJitName != null) persistence.markDirty
    }
    else jitContainer.jitUpdater(lenDiff, source)
    jitCursor = null
  }

  override protected def serializeJit(cursor: JitMutableCursor) {
    cursor.writeInt(length)
    jitAttributes.jitToBytes(cursor)
  }

  /**
   * Operations relating to any form of content information.
   */
  class Contents {

    /**
     * Load contents data using the given input source.
     * @param dataInput the input source information.
     */
    private[kernel] def load(
                              dataInput: DataInput) {}

    /**
     * Locks the element for writing, when applicable
     */
    protected def writeLock {
      persistence.writeLock
    }
  }

}
