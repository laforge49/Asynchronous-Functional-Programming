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
package element

import java.util.Iterator
import org.agilewiki.util.Timestamp

import org.agilewiki.kernel.component.KernelHomogeniousBTreeContainerComponent
import org.agilewiki.util.sequence.SequenceSource
import jits.KernelHandleElement

private[kernel] class KernelAddressMapElement
        extends EmbeddedContainerElement
                with KernelHomogeniousBTreeContainerComponent {
  def has(uuid: String): Boolean = {
    val nm = getName(uuid)
    nm != null
  }

  def getName(uuid: String): String = {
    val nm = getName(uuid, Timestamp.CURRENT_TIME)
    nm
  }

  def getName(uuid: String, timestamp: String) = {
    var rv: String = null
    val key = uuid + 1.asInstanceOf[Char]
    val seq = sequence(uuid, timestamp)
    if (seq != null) {
      rv = key + seq.current
      //      println("--->>>>>> "+rv)
      val handle = contents.get(rv).asInstanceOf[KernelHandleElement]
      if (handle.empty) {
        rv = null
      }
    }
    rv
  }

  def sequence(uuid: String): SequenceSource = {
    sequence(uuid, Timestamp.CURRENT_TIME)
  }

  def sequence(uuid: String, timestamp: String): SequenceSource = {
    //    println("am seq--uuid: "+uuid+" timestamp: "+timestamp)
    var rv = contents.subSequence(uuid + 1.asInstanceOf[Char])
    val inverted = Timestamp.invert(timestamp)
    if (rv.current(inverted) == null) {
      rv = null
    }
    rv
  }
}
