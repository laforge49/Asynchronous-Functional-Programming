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
package org.agilewiki.core.rel

import org.agilewiki.kernel.element.RolonRootElement
import org.agilewiki.kernel.TransactionContext

object RelDeleting {
  def apply(target: RolonRootElement) {
    var objectTypesSequence = ObjectTypesSequence(target)
    var relType = if (objectTypesSequence == null) null else objectTypesSequence.next(null)
    while (relType != null) {
      var objectUuidsSequence = ObjectUuidsSequence(target, relType)
      var objUuid = if (objectUuidsSequence == null) null else objectUuidsSequence.next(null)
      while (objUuid != null) {
        SetRelValue(target, relType, objUuid, null)
        objectUuidsSequence = ObjectUuidsSequence(target, relType)
        objUuid = if (objectUuidsSequence == null) null else objectUuidsSequence.next(null)
      }
      objectTypesSequence = ObjectTypesSequence(target)
      relType = if (objectTypesSequence == null) null else objectTypesSequence.next(null)
    }
    val objUuid = target.uuid
    var subjectTypesSequence = SubjectTypesSequence(target)
    relType = if (subjectTypesSequence == null) null else subjectTypesSequence.next(null)
    while (relType != null) {
      var subjectKeysSequence = SubjectKeysSequence(target, relType)
      var key = if (subjectKeysSequence == null) null else subjectKeysSequence.next(null)
      while (key != null) {
        val i = key.indexOf(5.asInstanceOf[Char])
        val value = key.substring(0, i)
        val subjUuid = key.substring(i + 1)
        val subj = TransactionContext().rolonRootElement(subjUuid)
        SetRelValue(subj, relType, objUuid, null)
        subjectKeysSequence = SubjectKeysSequence(target, relType)
        key = if (subjectKeysSequence == null) null else subjectKeysSequence.next(null)
      }
      subjectTypesSequence = SubjectTypesSequence(target)
      relType = if (subjectTypesSequence == null) null else subjectTypesSequence.next(null)
    }
  }
}