/*
 * Copyright 2011 Bill La Forge
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
package core
package batch

import kernel.element.{EmbeddedContainerElement, TreeMapElement, EmbeddedElement, Element}
import rel.GetRelValue

class CreatePreValidate
  extends PreValidate {

  override def process(roleName: String,
                       target: Element) {
    super.process(roleName, target)
    val childCount = target.attributes.getInt("childInc", 0)
    if (childCount < 0) throw NegativeChildCountException(target.getJitName)
  }

  override def relations: java.util.Set[String] = {
    val list = new java.util.HashSet[String]
    var sp = false
    val relationshipsSpecs = target.asInstanceOf[TreeMapElement].contents.get("relationshipsSpecs").
      asInstanceOf[EmbeddedContainerElement]
    if (relationshipsSpecs != null) {
      val rit = relationshipsSpecs.contents.iterator
      while (rit.hasNext) {
        var relType = rit.next
        //println("create pre "+relType)
        val relationshipsSpec = relationshipsSpecs.contents.get(relType).asInstanceOf[EmbeddedElement]
        if (relationshipsSpec != null) {
          val it = relationshipsSpec.attributes.iterator
          while (it.hasNext) {
            var objUuid = it.next
            if (objUuid.startsWith("@")) {
              val value = relationshipsSpec.attributes.get(objUuid)
              objUuid = objUuid.substring(1)
              list.add(relType + " " + objUuid)
              if (relType == CoreNames.PARENT_RELATIONSHIP)
                sp = true
            }
          }
        }
      }
    }
    if (!sp) {
      val uuid = target.getJitName
      //println("create bad")
      throw ImplicitDeleteException(uuid)
    }
    list
  }
}
