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

import org.agilewiki.web.comet.PushCometMessage
import org.agilewiki.web.comet.CometActor
import org.agilewiki.kernel.Timestamp
import org.agilewiki.actors.application.query.RolonResponse
import org.agilewiki.actors.application.notification.Notification
import org.agilewiki.actors.application.notification.ChangeElement
import org.agilewiki.actors.application.query.RolonRequest
import org.agilewiki.actors.RolonName
import org.agilewiki.actors.application.update.UpdateRolon
import org.agilewiki.actors.application.Qualification

Notification notification = notification
java.util.HashMap context = context

if (context.containsKey("message") && context.get("message").header() == "ignore") {
  return
}

def push = {
  String message ->
  CometActor cometActor = sourceActor.systemContext().canonicalActorFromUuid("cometActor")
  cometActor.bang(new PushCometMessage(notification.userUuid(), message))
}
RolonResponse window = context.get("effectedRolon")

def viewsURL = "/templates/loadContent.html" +
        "?xml=/templates/webtop/views.xml" +
        "&rolonUuid=${window.getAttribute("targetRolonUuid")}" +
        "&timestamp=${Timestamp.timestamp()}"

def sendWindowMessage = {
  mode = context.get("mode")

  name = ""
  parents = window.parents()
  if (parents.isEmpty()) {
    throw new IllegalStateException("An window cannot live without a parent")
  } else name = parents.apply(0).adoptionName()

  def sb = new StringBuilder()
  sb.append("""{"message":"${context.get("mode")}\"""")
  sb.append(""","id":"${window.rolonName.rolonUuid}\"""")

  if (mode == "LoadWindow") {
    sb.append(""","refferredRolonUuid":"${window.getAttribute("targetRolonUuid")}\"""")
    sb.append(""","name":"$name\"""")
    sb.append(""","image":"/icons/${window.getAttribute("targetRole")}24.png\"""")
    sb.append(""","rolonType":"${window.getAttribute("targetRole")}\"""")
    sb.append(""","rolonName":"${window.getAttribute("targetName")}\"""")
  }
  if (mode == "LoadWindow" || context.containsKey("window.update.position")) {
    sb.append(""","xPosition":"${window.getAttribute("xPosition")}\"""")
    sb.append(""","yPosition":"${window.getAttribute("yPosition")}\"""")
  }
  if (mode == "LoadWindow" || context.containsKey("window.update.size")) {
    sb.append(""","width":"${window.getAttribute("width")}\"""")
    sb.append(""","height":"${window.getAttribute("height")}\"""")
  }
  if (mode == "LoadWindow" || context.containsKey("window.update.tagLine")) {
    sb.append(""","tagline":"${window.getAttribute("tagLine")}\"""")
  }
  if (mode == "LoadWindow" || context.containsKey("window.update.mode")) {
    wmode = "normal"
    if (window.getAttribute("maximized") == "true") wmode = "maximized"
    if (window.getAttribute("minimized") == "true") wmode = "minimized"
    sb.append(""","mode":"${wmode}\"""")
  }
  if (mode == "LoadWindow" || context.containsKey("window.update.view")) {
    sb.append(""","localizedView":"${window.getAttribute("viewDescription")}\"""")
    sb.append(""","internalView":"${window.getAttribute("viewName")}\"""")
    sb.append(""","content":"${window.getAttribute("content")}\"""")
  }
  if (mode == "LoadWindow" || context.containsKey("window.update.selected")) {
    if (window.attributes().contains("selected"))
      sb.append(""","selected":${window.getAttribute("selected")}""")
  }
  sb.append(""","timestamp":"${Timestamp.CURRENT_TIME()}"}""")
  json = sb.toString()

  push(json)
}

def linkWindow = {
  if (context.containsKey("loadingTarget")) {
    if(context.get("message") instanceof RolonResponse){
      RolonResponse target = context.get("message")
      def targetUpdate = new UpdateRolon(notification.userUuid,notification.commandId,target.rolonName,"")
      def qualification = new Qualification(window.rolonName)
      qualification.setAttribute("notification", "webtop/window/targetUpdated")
      targetUpdate.updateQualification(qualification)
      targetUpdate.message(sourceActor,"ignore").send()
    }
    context.put("mode", "LoadWindow")
  } else {
    context.put("loadingTarget",true)
    def rolon = new RolonName(window.getAttribute("targetRolonUuid"))
    request = new RolonRequest(rolon,Timestamp.timestamp())
    request.message(sourceActor,"loadingTarget").send()
  }
}

if (notification.change.rolon.role == "window" &&
        notification.change.rolon.rolonUuid == notification.destinationRolonUuid) {

  if (!context.containsKey("mode")) {
    if (notification.change.created) { //Window created; sending load message
      linkWindow()
    } else if (!notification.change.deleted) { //Window modified; sending update message
      it = notification.change.changes.iterator()
      while (it.hasNext()) {
        ChangeElement chgElt = it.next()
        if (chgElt.changeType() == "KernelSetAttributeElement" &&
                chgElt.effectedElement() == "/") {
          attribute = chgElt.attributes().apply("attributeName")
          if (attribute == "selected") {
            context.put("mode", "UpdateWindow")
            context.put("window.update.selected", true)
          } else if (attribute == "tagLine") {
            context.put("mode", "UpdateWindow")
            context.put("window.update.tagLine", true)
          } else if (attribute == "xPosition" || attribute == "yPosition") {
            context.put("mode", "UpdateWindow")
            context.put("window.update.position", true)
          } else if (attribute == "width" || attribute == "height") {
            context.put("mode", "UpdateWindow")
            context.put("window.update.size", true)
          } else if (attribute == "minimized" || attribute == "maximized") {
            context.put("mode", "UpdateWindow")
            context.put("window.update.mode", true)
          } else if (attribute == "content") {
            context.put("mode", "UpdateWindow")
            context.put("window.update.view", true)
          }
        }
      }
    }
  }

  if (context.containsKey("mode")) {
    sendWindowMessage()
  }

}
