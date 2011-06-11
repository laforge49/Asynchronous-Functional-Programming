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
package org.agilewiki.command

import org.agilewiki.kernel.operation.Role
import java.util.LinkedHashMap
import org.agilewiki.actors.application.Context
import org.agilewiki.kernel.Kernel
import org.agilewiki.util.{SystemComposite, Configuration}
import org.agilewiki.actors.application.query.{Relationships, RolonResponse}
import org.agilewiki.core.CoreNames

class VersionCache(systemContext: SystemComposite) {
  private val maxSize: Int = Configuration(systemContext).requiredIntProperty(VERSION_CACHE_SIZE_PARAMETER)
  private val linkedHashMap = new LinkedHashMap[String, RolonResponse]

  def put(versionId: String, rolonResponse: RolonResponse) {
    synchronized{
      val newItem = null == linkedHashMap.remove(versionId)
      linkedHashMap.put(versionId, rolonResponse)
      if (newItem && linkedHashMap.size > maxSize) {
        val it = linkedHashMap.keySet.iterator
        val oldest = it.next
        linkedHashMap.remove(oldest)
      }
    }
  }

  def has(versionId: String): Boolean = {
    synchronized{
      if (!linkedHashMap.containsKey(versionId)) return false
      linkedHashMap.put(versionId, linkedHashMap.get(versionId))
      true
    }
  }

  def load(systemContext: SystemComposite, versionId: String, context: Context): Boolean = {
    var rolonResponse: RolonResponse = null
    synchronized{
      rolonResponse = linkedHashMap.get(versionId)
    }
    if (rolonResponse == null) return false
    val userUuid = context.get("user.uuid")
    val rolonUuid = rolonResponse.uuid
    if (rolonUuid != userUuid) {
      val relationships = new Relationships(rolonResponse.relationships)
      if (!hasPrivilege(systemContext, context, "reader", relationships)) return false
    }
    rolonResponse.load(versionId, context)
    true
  }

  def hasPrivilege(systemContext: SystemComposite, context: Context, rolonUuid: String, privilege: String): Boolean = {
    val timestamp = context.get("timestamp")
    val versionId = timestamp + "|" + rolonUuid
    val relationships = context.getSpecial(timestamp + "|" + rolonUuid + ".relationships").
      asInstanceOf[Relationships]
    hasPrivilege(systemContext, context, privilege, relationships)
  }

  def hasPrivilege(systemContext: SystemComposite, context: Context, privilege: String, relationships: Relationships): Boolean = {
    val extendedContext = context.getSpecial((".extendedContext")).asInstanceOf[ExtendedContext]
    val uTimestamp = context.get("user.timestamp")
    val uUuid = context.get("user.uuid")
    val uVersionId = uTimestamp + "|" + uUuid
    var uRelationships: Relationships = null
    uRelationships = context.getSpecial(uVersionId + ".relationships").asInstanceOf[Relationships]
    if (relationships == null && uRelationships == null) return false
    val pl = privilegeLevel(privilege)
    if (relationships != null) {
      val limit = relationships.value(context.LIMIT_RELATIONSHIP, uUuid)
      if (limit != null) {
        val rpl = privilegeLevel(limit)
        if (rpl < pl) return false
      }
    }
    if (relationships != null) {
      val rpl = privilegeLevel(relationships.value(context.ACCESS_RELATIONSHIP, CoreNames.ACCESS_UUID))
      if (rpl >= pl) return true
    }
    if (uRelationships == null) return false
    if (uUuid == CoreNames.ADMIN_USER_UUID) return true
    val apl = privilegeLevel(uRelationships.value(context.MEMBER_RELATIONSHIP, CoreNames.ADMIN_GROUP_UUID))
    if (apl >= pl) return true
    if (relationships == null) return false
    val rpl = privilegeLevel(relationships.value(context.ACCESS_RELATIONSHIP, uUuid))
    if (rpl >= pl) return true
    val groupUuids = extendedContext.userGroups(context, privilege)
    val sit = groupUuids.iterator
    while (sit.hasNext) {
      val gUuid = sit.next
      val priv = relationships.value(context.ACCESS_RELATIONSHIP, gUuid)
      if (priv != null) {
        val rpl = privilegeLevel(priv)
        if (rpl >= pl) return true
      }
    }
    false
  }

  def privilegeLevel(privilege: String) = {
    if (privilege == null) -1
    else if (privilege == "owner") 2
    else if (privilege == "writer") 1
    else if (privilege == "reader") 0
    else -1
  }

  def role(systemContext: SystemComposite, name: String): Role = Kernel(systemContext).role(name)

  def singletonUuid(systemContext: SystemComposite, roleName: String): String = CommandLayer(systemContext).singletonUuid(roleName)
}