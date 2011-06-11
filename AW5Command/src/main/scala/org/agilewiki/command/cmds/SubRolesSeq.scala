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
package command.cmds

import java.util.TreeSet
import org.agilewiki.kernel.Kernel
import util.sequence.actors.basic.NavigableSequenceActor
import util.{SystemComposite, Configuration}
import core.CoreNames

class SubRolesSeq(systemContext: SystemComposite, uuid: String)
        extends SimpleSequence(systemContext, uuid) {
  protected def seq: NavigableSequenceActor = {
    var loopPrefix = context.get("loopPrefix")
    if (loopPrefix.length > 0)
      loopPrefix += "."
    val roleName = context.get(loopPrefix+"role")
    if (roleName.length==0) {
      error(requester, "Missing from sub roles sequence: role")
      return null
    }
    val role = Kernel(localContext).role(CoreNames.typeName(roleName))
    if (role == null) {
      error(requester, "no such role: " + roleName)
      return null
    }
    val keys = new TreeSet[String]
    var i = 0
    while (i > -1) {
      i += 1
      val cRoleName = Configuration(localContext).property("role" + i + ".name")
      if (cRoleName == null) {
        i = -1
      } else if (cRoleName != roleName) {
        val cRole = Kernel(localContext).role(cRoleName)
        if (cRole != null && cRole.superRoles.contains(roleName)) {
          keys.add(cRoleName)
        }
      }
    }
    NavigableSequenceActor(localContext, keys, false)
  }
}

object SubRolesSeq {
  val name = "seq-subRoles"
  val cls = "org.agilewiki.command.cmds.SubRolesSeq"
}
