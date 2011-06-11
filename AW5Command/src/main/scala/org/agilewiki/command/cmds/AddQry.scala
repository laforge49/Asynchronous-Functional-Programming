/*
 * Copyright 2010 Alex K.
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
package org.agilewiki.command.cmds

import org.agilewiki.actors.ActorLayer
import org.agilewiki.util.SystemComposite

class AddQry(systemContext: SystemComposite, uuid: String)
        extends SimpleQuery(systemContext, uuid) {
  override protected def query: String = {
    if (!context.contains("name"))
      return "Missing argument from context map: name"
    val name = context.get("name")
    val firstArg = Integer.valueOf(name).intValue
    val named = context.get("name")
    if (!named.isInstanceOf[StringBuilder])
      return "Not a variable: " + name
    var sb = named.asInstanceOf[StringBuilder]
    var secondArg:Int = 0
    if (!context.contains("value"))
      secondArg = 1
    else {
      val value = context.get("value")
      secondArg = Integer.valueOf(value).intValue
    }
    val result = firstArg + secondArg
    sb.clear
    sb.append(result)
    if(result < 0)
      context.setCon("negative", "true")
    else if (result == 0)
      context.setCon("zero", "true")
    else
      context.setCon("positive", "true")
    null
  }
}

object AddQry {
  val name = "qry-add"
  val cls = "org.agilewiki.command.cmds.AddQry"
}
