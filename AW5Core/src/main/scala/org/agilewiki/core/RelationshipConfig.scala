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

import java.util.Properties

class RelationshipConfig(properties: Properties) {
  private var ndx = 1

  private def relTypeKey = "rel." + ndx + ".relTyp"

  private def objUuidKey = "rel." + ndx + ".objUuid"

  private def objRoleKey = "rel." + ndx + ".objRole"

  private def subjUuidKey = "rel." + ndx + ".subjUuid"

  private def subjRoleKey = "rel." + ndx + ".subjRole"

  private def relValueKey = "rel." + ndx + ".value"

  private def nxt {ndx += 1}

  while (properties.containsKey(objUuidKey)) nxt

  def apply(relationshipType: String,
            subjUuid: String, subjRoleName: String,
            objUuid: String, objRoleName: String,
            relValue: String) {
    if (objUuid == null || subjUuid == null)
      throw new IllegalArgumentException("A uuid cannot be null")
    if (objUuid.contains("_") || subjUuid.contains("_"))
      throw new IllegalArgumentException("A uuid cannot contain a '_' charachter")
    properties.put(relTypeKey, relationshipType)
    properties.put(objUuidKey, objUuid)
    properties.put(objRoleKey, objRoleName)
    properties.put(subjUuidKey, subjUuid)
    properties.put(subjRoleKey, subjRoleName)
    properties.put(relValueKey, relValue)
    nxt
  }
}
