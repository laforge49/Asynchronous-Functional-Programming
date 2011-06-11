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
package core
package batch

import kernel.TransactionContext
import rel._
import kernel.element.{EmbeddedContainerElement, TreeMapElement, EmbeddedElement, Element}

class UpdatePreValidate
  extends PreValidate {

  override def process(roleName: String,
                       target: Element) {
    super.process(roleName, target)
    val uuid = target.getJitName
    val rolon = TransactionContext().rolonRootElement(uuid)
    if (rolon == null) {
      System.err.println("Missing Rolon, UUID = " + uuid)
      throw MissingUuidException(uuid)
    }
    val queryTimestamp = je.attributes.get("queryTimestamp")
    val updateTimestamp = rolon.attributes.get("updateTimestamp")
    if (updateTimestamp != null && queryTimestamp < updateTimestamp) {
      throw SyncException(uuid)
    }
    val childCount = rolon.attributes.getInt("childCount", 0) + target.attributes.getInt("childInc", 0)
    if (childCount < 0) throw NegativeChildCountException(uuid)
  }

  override def relations: java.util.Set[String] = {
    val list = new java.util.HashSet[String]
    val uuid = target.getJitName
    val subj = TransactionContext().rolonRootElement(uuid)
    var sp = false
    var cp = 0
    val relationshipsSpecs = target.asInstanceOf[TreeMapElement].contents.get("relationshipsSpecs").
      asInstanceOf[EmbeddedContainerElement]
    if (relationshipsSpecs == null) return list
    val rit = relationshipsSpecs.contents.iterator
    while (rit.hasNext) {
      var relType = rit.next
      //println("update pre "+relType)
      val relationshipsSpec = relationshipsSpecs.contents.get(relType).asInstanceOf[EmbeddedElement]
      if (relationshipsSpec != null) {
        val it = relationshipsSpec.attributes.iterator
        while (it.hasNext) {
          var objUuid = it.next
          if (objUuid.startsWith("@")) {
            val value = relationshipsSpec.attributes.get(objUuid)
            objUuid = objUuid.substring(1)
            if (value == "%" || value.startsWith("$")) {
              if (GetRelValue(subj, relType, objUuid) == null) {
                //println("um1")
                throw MissingRelationshipException(uuid, objUuid, relType)
              }
              if (relType == CoreNames.PARENT_RELATIONSHIP && value == "%")
                cp += 1
            }
            else {
              list.add(relType + " " + objUuid)
              if (GetRelValue(subj, relType, objUuid) != null) {
                //println("ud1")
                throw DuplicateRelationshipException(uuid, objUuid, relType)
              }
              if (relType == CoreNames.PARENT_RELATIONSHIP)
                sp = true
            }
          }
        }
      }
    }
    if (!sp && cp > 0) {
      val uuid = target.getJitName
      val parentLinks = GetObjectLinks(subj, CoreNames.PARENT_RELATIONSHIP)
      if (parentLinks == null || cp >= parentLinks.contents.size) {
        //println("update bad")
        throw ImplicitDeleteException(uuid)
      }
    }
    list
  }
}
