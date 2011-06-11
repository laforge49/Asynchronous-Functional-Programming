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

import org.agilewiki.kernel.component.BTreeContainerComponent
import util.Configuration

class DocumentElement
        extends BTreeElement
                with Document {
  override def setDoc(doc: Array[Byte]) {
    val it = contents.iterator
    while(it.hasNext) {
      val k = it.next
      contents.remove(k)
    }
    if (doc == null || doc.length == 0) return
    val SMALL = Configuration(systemContext).requiredIntProperty(MAX_BTREE_ROOT_SIZE_PARAMETER)
    val LARGE = Configuration(systemContext).requiredIntProperty(MAX_BTREE_LEAF_SIZE_PARAMETER)
    if (doc.length < SMALL) {
      val be = contents.add("x", BYTES_ELEMENT_ROLE_NAME).asInstanceOf[BytesElement]
      be.contents.setBytes(doc)
      return
    }
    if (doc.length < LARGE) {
      val len1: Int = doc.length / 2
      var be = contents.add("a", BYTES_ELEMENT_ROLE_NAME).asInstanceOf[BytesElement]
      be.contents.setBytes(doc, 0, len1)
      be = contents.add("b", BYTES_ELEMENT_ROLE_NAME).asInstanceOf[BytesElement]
      be.contents.setBytes(doc, len1, doc.length - len1)
      return
    }
    val count: Int = doc.length / LARGE
    var i = 0
    var off = 0
    while (i < count) {
      var k = "" + i
      k = "000000".substring(k.length) + k
      val be = contents.add(k, BYTES_ELEMENT_ROLE_NAME).asInstanceOf[BytesElement]
      var len = LARGE
      if (off + len > doc.length) len = doc.length - off
      be.contents.setBytes(doc, off, len)
    }
  }

  override def contentLength: Int = {
    var len = 0
    val it = contents.iterator
    while (it.hasNext) {
      val k = it.next
      val be = contents.get(k).asInstanceOf[BytesElement]
      len += be.contents.contentLength
    }
    len
  }

  override def getDoc: Array[Byte] = {
    val doc = new Array[Byte](contentLength)
    var off = 0
    val it = contents.iterator
    while(it.hasNext) {
      val k = it.next
      val be = contents.get(k).asInstanceOf[BytesElement]
      off += be.contents.getBytes(doc,off)
    }
    doc
  }
}
