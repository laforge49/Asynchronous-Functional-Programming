/*
 * Copyright 2010 M.Naji
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

import org.agilewiki.command.ExtendedContext
import org.agilewiki.actors.application.Context
import org.agilewiki.command.update.RolonUpdateRequest
import org.agilewiki.command.RolonProxy

Context context = context
ExtendedContext extendedContext = context.getSpecial(".extendedContext")

def desktopUuid = context.get("target.desktop.uuid")
RolonProxy desktop = extendedContext.rolonProxy(context, desktopUuid)

def wrapperUuid = context.get("target.wrapper.uuid")
RolonProxy wrapper = extendedContext.rolonProxy(context, wrapperUuid)


if (wrapper.getRoleName() == "icon" || wrapper.getRoleName() == "window") {
  RolonUpdateRequest updateDesktop = extendedContext.rolonUpdateRequest("webtop.delete", context, desktop, "")
  updateDesktop.setAttribute("delete.${wrapper.getRoleName()}.$wrapperUuid", "true")

  extendedContext.updateRequests.add(updateDesktop)
}
extendedContext.updateParameters().put("webtop.delete.executed", "true")  