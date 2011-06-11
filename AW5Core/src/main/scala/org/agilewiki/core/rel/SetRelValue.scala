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
package rel

import kernel.element.RolonRootElement
import kernel.TransactionContext
import kernel.jits.RolonJitLinkElement

object SetRelValue {
  def apply(subj: RolonRootElement, relType: String, objectUuid: String, value: String) {
    var v = if ("" == value) null else value
    var objectLinks = MakeObjectLinks(subj, relType)
    var objectLink = objectLinks.contents.get(objectUuid).asInstanceOf[ObjectLink]
    if (objectLink == null && v == null) {
      return
    }
    if (objectLink != null && objectLink.getValue == v) {
      return
    }
    val obj = TransactionContext().makeRolonRootElement(objectUuid)
    var subjectLinks = MakeSubjectLinks(obj, relType)
    var oldValue: String = null
    if (objectLink != null) {
      oldValue = objectLink.getValue
      if (v == null) {
        objectLinks.contents.delete(objectUuid)
        if (objectLinks.contents.size == 0) {
          objectLinks = null
          var objects = GetObjects(subj)
          objects.contents.delete(relType)
          if (objects.contents.size == 0) {
            objects = null
            subj.contents.delete("Objects")
          }
        }
      }
      else {
        objectLink.setValue(v)
      }
    }
    else {
      objectLink = objectLinks.contents.add(objectUuid, OBJECT_LINK_ROLE_NAME).asInstanceOf[ObjectLink]
      objectLink.setReferenceUuid(objectUuid)
      objectLink.setValue(v)
    }
    val subjectUuid = subj.uuid
    if (oldValue != null) {
      val oldKey = oldValue + 5.asInstanceOf[Char] + subjectUuid
      subjectLinks.contents.delete(oldKey)
      if (subjectLinks.contents.size == 0 && v == null) {
        subjectLinks = null
        var subjects = GetSubjects(obj)
        subjects.contents.delete(relType)
        if (subjects.contents.size == 0) {
          subjects = null
          obj.contents.delete("Subjects")
        }
      }
    }
    if (v != null) {
      val key = v + 5.asInstanceOf[Char] + subjectUuid
      val subjectLink = subjectLinks.contents.add(key, SUBJECT_LINK_ROLE_NAME).asInstanceOf[RolonJitLinkElement]
      subjectLink.setReferenceUuid(subjectUuid)
    }
  }
}
