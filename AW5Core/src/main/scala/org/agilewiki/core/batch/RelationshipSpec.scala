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

import kernel._
import element.{EmbeddedContainerElement, RolonRootElement, TreeMapElement, EmbeddedElement}
import util.UuidExtension

object RelationshipSpec {
  def add(je: RolonRootElement,
          uuid: String,
          relType: String,
          objUuid: String,
          value: String) {
    if (value == null || value == "")
      throw new IllegalArgumentException("value must not be null or empty")
    val action = BatchJE.update(je, uuid)
    if (action.jitRoleName == DELETE_ACTION_ROLE_NAME)
      throw new IllegalArgumentException("delete may not add a relationship")
    apply(je, action, uuid, relType, objUuid, value, false)
  }

  def update(je: RolonRootElement,
             uuid: String,
             relType: String,
             objUuid: String,
             value: String) {
    if (value == null || value == "")
      throw new IllegalArgumentException("value must not be null or empty")
    val action = BatchJE.update(je, uuid)
    if (value == null || value == "")
      throw new IllegalArgumentException("value must not be null or empty")
    if (action.jitRoleName == DELETE_ACTION_ROLE_NAME)
      throw new IllegalArgumentException("delete may not update a relationship")
    apply(je, action, uuid, relType, objUuid, value, true)
  }

  def remove(je: RolonRootElement,
             uuid: String,
             relType: String,
             objUuid: String) {
    val action = BatchJE.update(je, uuid)
    apply(je, action, uuid, relType, objUuid, "", false)
  }

  private def apply(je: RolonRootElement,
                    action: TreeMapElement,
                    uuid: String,
                    relType: String,
                    objUuid: String,
                    value: String,
                    update: Boolean) {
    //println("spec "+uuid+" "+relType+ " "+objUuid+" >"+value+"<")
    val subjRoleName = UuidExtension(uuid)
    val objRoleName = UuidExtension(objUuid)
    val systemContext = action.systemContext
    val kernel = Kernel(systemContext)
    val subjRole = kernel.role(subjRoleName)
    val allowedObjRoleName = subjRole.property("obj." + relType).asInstanceOf[String]
    if (allowedObjRoleName == null)
      throw new IllegalArgumentException(subjRoleName + " may not be the subject of " + relType)
    val objRole = kernel.role(objRoleName)
    val allowedSubjRoleName = objRole.property("subj." + relType).asInstanceOf[String]
    if (allowedSubjRoleName == null)
      throw new IllegalArgumentException(objRoleName + " may not be the object of " + relType)
    if (!objRole.isA(allowedObjRoleName))
      throw new IllegalArgumentException(objRoleName + " is not a " + allowedObjRoleName)
    if (!subjRole.isA(allowedSubjRoleName)) {
      System.err.println("objRoleName="+objRoleName)
      System.err.println("relType="+relType)
      throw new IllegalArgumentException(subjRoleName + " is not a " + allowedSubjRoleName)
    }
    val relationshipsSpecs = action.contents.make("relationshipsSpecs", TREE_MAP_ELEMENT).
      asInstanceOf[EmbeddedContainerElement]
    val relationshipsSpec = relationshipsSpecs.contents.make(relType, EMBEDDED_ELEMENT_TREE_MAP_ROLE_NAME).
      asInstanceOf[EmbeddedElement]
    if (update) relationshipsSpec.attributes.put("@" + objUuid, "$" + value)
    else relationshipsSpec.attributes.put("@" + objUuid, "%" + value)
    if (!update) {
      if (relType == CoreNames.PARENT_RELATIONSHIP) {
        val objAction = BatchJE.update(je, objUuid)
        var childInc = objAction.attributes.getInt("childInc", 0)
        if (value == "") {
          childInc -= 1
        } else {
          childInc += 1
        }
        objAction.attributes.putInt("childInc", childInc, 0)
      } else if (value != "") {
        val i = objUuid.indexOf("_")
        val uuid = objUuid.substring(0,i)
        val roleName = objUuid.substring(i+1)
        if (uuid != roleName)
          BatchJE.update(je, objUuid)
      }
    }
  }
}
