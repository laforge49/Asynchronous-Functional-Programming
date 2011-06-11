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

ExtendedContext extendedContext = context.getSpecial(".extendedContext")

timestamp = context.get("timestamp")
itemUuid = context.get("itemUuid")
versionId = timestamp + "|" + itemUuid
desktopUuid = context.get("desktopUuid")
RolonProxy desktop = extendedContext.rolonProxy(context,desktopUuid)
json = ""
itemRole = context.get("${versionId}.role")
if (itemRole == "icon") {
  RolonProxy icon = extendedContext.rolonProxy(context,itemUuid)
  sb = new StringBuilder()
  sb.append("""{"message":"LoadIcon\"""")
  sb.append(""","id":"$itemUuid\"""")
  sb.append(""","name":"${context.get(versionId + ".name")}\"""")
  sb.append(""","image":"/icons/${icon.getAttribute("targetRole")}48.png\"""")
  sb.append(""","xPosition":"${icon.getAttribute("xPosition")}\"""")
  sb.append(""","yPosition":"${icon.getAttribute("yPosition")}\"""")
  sb.append(""","tagline":"${icon.getAttribute("tagLine")}\"""")
  sb.append(""","selected":${icon.getAttribute("selected")}""")
  sb.append(""","timestamp":"${org.agilewiki.kernel.Timestamp.CURRENT_TIME()}"}""")
  json = sb.toString()
} else if (itemRole == "window") {
  RolonProxy window = extendedContext.rolonProxy(context,itemUuid)
  mode = "normal"
  if(window.getAttribute("maximized") == "true") mode = "maximized"
  if(window.getAttribute("minimized") == "true") mode = "minimized"


//  def viewsURL = "/templates/loadContent.html" +
//            "?xml=/templates/webtop/views.xml" +
//            "&rolonUuid=${window.getAttribute("targetRolonUuid")}" +
//            "&timestamp=$timestamp"

  sb = new StringBuilder()
  sb.append("""{"message":"LoadWindow\"""")
  sb.append(""","timestamp":"${org.agilewiki.kernel.Timestamp.CURRENT_TIME()}\"""")
  sb.append(""","id":"${itemUuid}\"""")
  sb.append(""","refferredRolonUuid":"${window.getAttribute("targetRolonUuid")}\"""")
  sb.append(""","name":"${window.getChildName(desktopUuid)}\"""")
  sb.append(""","image":"/icons/${window.getAttribute("targetRole")}24.png\"""")
  sb.append(""","xPosition":"${window.getAttribute("xPosition")}\"""")
  sb.append(""","yPosition":"${window.getAttribute("yPosition")}\"""")
  sb.append(""","width":"${window.getAttribute("width")}\"""")
  sb.append(""","height":"${window.getAttribute("height")}\"""")
  sb.append(""","tagline":"${window.getAttribute("tagLine")}\"""")
  sb.append(""","selected":${desktop.getAttribute("selected") == itemUuid}""")
  sb.append(""","rolonType":"${window.getAttribute("targetRole")}\"""")
  sb.append(""","rolonName":"${window.getAttribute("targetName")}\"""")
  sb.append(""","mode":"${mode}\"""")
  sb.append(""","localizedView":"${window.getAttribute("defaultView")}\"""")
  sb.append(""","internalView":"${window.getAttribute("defaultView")}\"""")
  sb.append(""","content":"${window.getAttribute("content")}"}""")
  json = sb.toString()
}

if (json.size() > 0) extendedContext.pushToClient(json)