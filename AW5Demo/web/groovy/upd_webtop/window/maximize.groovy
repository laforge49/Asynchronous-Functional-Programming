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
import org.agilewiki.command.RolonProxy
import org.agilewiki.command.update.RolonUpdateRequest

ExtendedContext extendedContext = context.getSpecial(".extendedContext")

String windowUuid = context.get("target.wrapper.uuid")
RolonProxy window = extendedContext.rolonProxy(context, windowUuid)

def maximize = context.get("comet.data.userEvent.maximize")

RolonUpdateRequest updateWindow = extendedContext.rolonUpdateRequest("webtop.window.maximize",context,window,"")
updateWindow.setAttribute("maximized",maximize)
extendedContext.updateRequests().add(updateWindow)
extendedContext.updateParameters().put("webtop.window.maximize.executed",true)