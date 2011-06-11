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

import util._
import component.AttributesComponent
import component.PersistenceComponent
import component.ContentsComponent
import operation.ElementRole

abstract class Element
        extends ContentsComponent
                with AttributesComponent
                with PersistenceComponent  {

  override def writeLock { persistence.writeLock }

  override def getBlockElement: _BlockElement = super.getBlockElement.asInstanceOf[_BlockElement]

  override def getRolonElement = super.getRolonElement.asInstanceOf[RolonRootElement]

  def elementIsA(et: String) = {
    val met = jitRoleName
    if (met == null) false
    else jitRole.asInstanceOf[ElementRole].isA(et)
  }

  def transactionContexts: TransactionContext = Kernel(systemContext).transactionContexts

  private[kernel] def kernelRootElement = Kernel(systemContext).kernelRootElement

  protected def load {
  }

  def rolonUuid: String = {
    if (this.isInstanceOf[EmbeddedElement]) {
      getBlockElement.rolonUuid
    } else {
      throw new UnsupportedOperationException(""+this)
    }
  }

  /**
   * Print the attributes of the Element
   */
  def printAttributes {
    println("" + attributes.size + " attributes:")
    val it = attributes.iterator
    while (it.hasNext) {
      val name = it.next
      val value = attributes.get(name)
      println(name + " = " + value)
    }
  }

  /**
   * Prints the UUID of the element's rolon
   */
  def printRolonUuid {
    println("rolon UUID = " + rolonUuid)
  }
}
