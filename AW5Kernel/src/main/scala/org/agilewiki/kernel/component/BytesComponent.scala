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

import util.jit.{JitMutableCursor, JitBytes}
import element.Element

/**
 * Defines a common API for containers, and also implements a null-container.
 * @author Bill La Forge
 */
trait BytesComponent extends ContentsComponent {
  this: Element =>

  private var _jitBytes: JitBytes = _

  override protected def defineContents = new Bytes

  override def contents = {_contents.asInstanceOf[Bytes]}

  protected def jitBytes = {
    deserialize
    _jitBytes
  }

  override def builder = {
    _jitBytes = JitBytes.createJit(systemContext)
    _jitBytes.partness(this,null,this)
    super.builder + _jitBytes.jitByteLength
  }

  override protected def serializeJit(cursor: JitMutableCursor) {
    super.serializeJit(cursor)
    jitBytes.jitToBytes(cursor)
  }

  override def elementLoader(cursor: JitMutableCursor) {
    super.elementLoader(cursor)
    jitBytes.loadJit(cursor)
  }

  class Bytes extends Contents {

    def contentLength = jitBytes.contentLength

    def setBytes(bytes: Array[Byte]) {
      persistence.writeLock
      jitBytes.setBytes(bytes)
    }

    def setBytes(bytes: Array[Byte], offset: Int, length: Int) {
      persistence.writeLock
      jitBytes.setBytes(bytes, offset, length)
    }

    def getBytes: Array[Byte] = {
      jitBytes.getBytes
    }

    def getBytes(bytes: Array[Byte], offset: Int): Int = {
      jitBytes.getBytes(bytes, offset)
    }
  }
}
