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
import org.agilewiki.actors.application.query.RolonRequest
import org.agilewiki.actors.RolonName
import org.agilewiki.actors.application.notification.Notification
import org.agilewiki.actors.application.notification.ChangeElement
import org.agilewiki.actors.application.update.UpdateRolon

Notification notification = notification
java.util.HashMap context = context

if(context.containsKey("message") && context.get("message").header() == "ignore"){
  return
}

def push = {
  String message ->
  CometActor cometActor = sourceActor.systemContext().canonicalActorFromUuid("cometActor")
  cometActor.bang(new PushCometMessage(notification.userUuid(), message))
}

RolonResponse icon = context.get("effectedRolon")

def sendIconMessage = {
  name = ""
  parents = icon.parents()
  if (parents.isEmpty()) {
    throw new IllegalStateException("An icon cannot live without a parent")
  } else name = parents.apply(0).adoptionName()
  tagline = ""
  if (icon.attributes().contains("tagLine")) {
    tagline = icon.attributes().apply("tagLine")
  }

  iconImage = "/icons/unknown48.png"
  if (icon.attributes().contains("targetRole")) iconImage = "/icons/${icon.attributes().apply("targetRole")}48.png"

  def mode = context.get("mode")
  sb = new StringBuilder()
  sb.append("""{"message":"${mode}","id":"${icon.rolonName().rolonUuid()}\"""")
  sb.append(""","name":"$name\"""")
  if (mode == "LoadIcon" || context.containsKey("icon.update.image"))
    sb.append(""","image":"$iconImage\"""")
  if (mode == "LoadIcon" || context.containsKey("icon.update.tagLine")){
    target = context.get("target")
    if (tagline == "" && target != null && target.attributes().contains("tagLine")) {
      targline = target.attributes().apply("tagLine")
    }
    sb.append(""","tagline":"$tagline\"""")
  }
  if (context.get("mode") == "LoadIcon" || context.containsKey("icon.update.position")) {
    sb.append(""","xPosition":"${icon.attributes().apply("xPosition")}\"""")
    sb.append(""","yPosition":"${icon.attributes().apply("yPosition")}\"""")
  }
  if (mode == "LoadIcon" || context.containsKey("icon.update.position") || context.containsKey("icon.update.selected"))
    sb.append(""","selected":${Boolean.parseBoolean(icon.attributes().apply("selected"))}""")
  sb.append(""","timestamp":"${Timestamp.CURRENT_TIME()}"}""")

  json = sb.toString()
  push(json)
}

def registerPosition = {
  desktop = icon.parents().apply(0).rolonName()
  updateDesktop = new UpdateRolon(notification.userUuid,notification.commandId,desktop)
  updateDesktop.setAttribute("location.icon.${icon.rolonName().rolonUuid()}",
          "${icon.attributes().apply("xPosition")}:${icon.attributes().apply("yPosition")}")
  updateDesktop.message(sourceActor,"ignore").send()
}


if (notification.change.rolon.role == "icon" &&
        notification.change.rolon.rolonUuid == notification.destinationRolonUuid) {
  if (!context.containsKey("mode")) {
    if (notification.change.created) { //Icon created; sending load message
      registerPosition()
      context.put("mode", "LoadIcon")
    } else if (!notification.change.deleted) { //Icon modified; sending update message
      it = notification.change.changes.iterator()
      while (it.hasNext()) {
        ChangeElement chgElt = it.next()
        if (chgElt.changeType() == "KernelSetAttributeElement" &&
                (chgElt.effectedElement() == "/")) {
          attribute = chgElt.attributes().apply("attributeName")
          if (attribute == "targetRole") {
            context.put("mode", "UpdateIcon")
            context.put("icon.update.image", true)
          }
          else if (attribute == "tagLine") {
            context.put("mode", "UpdateIcon")
            context.put("icon.update.tagLine", true)
          }
          else if (attribute == "xPosition" || attribute == "yPosition") {
            context.put("mode", "UpdateIcon")
            context.put("icon.update.position", true)
          }
          else if (attribute == "selected") {
            context.put("mode", "UpdateIcon")
            context.put("icon.update.selected", true)
          }
        }
      }
    }
  }
  if (context.containsKey("mode")) {
    if ((context.containsKey("mode") == "LoadIcon" || context.containsKey("icon.update.tagLine")) &&
            !(context.containsKey("message") && context.get("message").header() == "targetLoaded")) {
      icon = context.get("effectedRolon")
      message = new RolonRequest(new RolonName(icon.attributes().apply("targetRolonUuid")),
              Timestamp.timestamp())
      message.message(sourceActor, "targetLoaded").send()
    } else {
      context.put("target", context.get("message"))
      sendIconMessage()
    }
  }
}
