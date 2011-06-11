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

import element.Element
import util.jit.{JitMutableCursor, JitString}

trait LinkContentsComponent
  extends ContentsComponent with Link {
  this: Element =>

  private var referenceUuid: JitString = _

  override def setReferenceUuid(uuid: String) {
    deserialize
    if (uuid == null) referenceUuid.setString("")
    else referenceUuid.setString(uuid)
  }

  override def getReferenceUuid = {
    deserialize
    val rv = referenceUuid.getString
    if (rv == "") null
    else rv
  }

  override protected def builder = {
    val l = super.builder
    referenceUuid = JitString.createJit(systemContext)
    referenceUuid.setString("")
    referenceUuid.partness(this, null, this)
    l + referenceUuid.jitByteLength
  }

  override def elementLoader(cursor: JitMutableCursor) {
    super.elementLoader(cursor)
    referenceUuid.loadJit(cursor)
  }

  override protected def serializeJit(cursor: JitMutableCursor) {
    super.serializeJit(cursor)
    referenceUuid.jitToBytes(cursor)
  }
}
