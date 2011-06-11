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
package kernel
package jits

import java.lang.ref.WeakReference

import element.{KernelINodeElement, _BlockElement}

private[kernel] class KernelINodeHandleElement extends KernelHandleElement {
  private var weakReference = new WeakReference(null.asInstanceOf[_BlockElement])

  override def reference = {
    val rv = weakReference.get.asInstanceOf[KernelINodeElement]
    Kernel(systemContext).kernelINodeCache.accessed(rv)
    rv
  }

  override def hasReference = reference != null

  override def clearReference {weakReference.clear}

  override def setReference(be: _BlockElement) {weakReference = new WeakReference(be)}

  override def resolve = super.resolve.asInstanceOf[KernelINodeElement]

  override def read = {
    val rv = super.read.asInstanceOf[KernelINodeElement]
    Kernel(systemContext).kernelINodeCache.accessed(rv)
    rv
  }

}
