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


import org.agilewiki.actors.application.query.RolonResponse
import org.agilewiki.actors.application.notification.Notification
import org.agilewiki.actors.application.update.UpdateRolon
import org.agilewiki.actors.RolonName
import org.agilewiki.actors.application.notification.ChangeElement
import org.agilewiki.web.comet.CometActor
import org.agilewiki.web.comet.PushCometMessage
import org.agilewiki.actors.application.update.DeleteRolon
import org.agilewiki.actors.application.query.ChildRequest
import org.agilewiki.util.Timestamp

Notification notification = notification
java.util.HashMap context = context
RolonResponse desktop = context.get("destinationRolon")

ICON_WDTH = 80
ICON_HGHT = 80

def push = {
  String message ->
  CometActor cometActor = sourceActor.systemContext().canonicalActorFromUuid("cometActor")
  cometActor.bang(new PushCometMessage(notification.userUuid(), message))
}


if (notification.destinationRolonUuid == notification.change.rolon.rolonUuid &&
        !(context.containsKey("message") && context.get("message").header() == "ignore")) {
  updates = new HashMap<String, UpdateRolon>()

  if (desktop.attributes().contains("unselect")) {
    uuid = desktop.attributes().apply("unselect")
    itemUpdate = new UpdateRolon(notification.userUuid, notification.commandId, new RolonName(uuid))
    itemUpdate.setAttribute("selected", "false")
    itemUpdate.message(sourceActor, "ignore").send()
    desktopUpdate = new UpdateRolon(notification.userUuid, notification.commandId, desktop.rolonName)
    updates.put(desktop.rolonName().rolonUuid(), desktopUpdate)
    desktopUpdate.removeAttribute("unselect")
  }

  it = notification.change.changes.iterator()
  while (it.hasNext()) {
    ChangeElement chgElt = it.next()
    if (chgElt.changeType() == "KernelSetAttributeElement" &&
            chgElt.effectedElement() == "/") {
      String changedAttribute = chgElt.attributes().apply("attributeName")
      if (changedAttribute == "selected" && desktop.attributes().contains("selected")) {
        uuid = desktop.attributes().apply("selected")
        if (!updates.containsKey(uuid))
          updates.put(uuid, new UpdateRolon(notification.userUuid, notification.commandId, new RolonName(uuid)))
        itemUpdate = updates.get(uuid)
        itemUpdate.setAttribute("selected", "true")
      } else if (changedAttribute.startsWith("move.icon.") &&
              chgElt.attributes().contains("attributeValue")) {
        location = chgElt.attributes().apply("attributeValue").split(":")
        String icon = changedAttribute.substring(10)
        String x = location[0]
        String y = location[1]
        def changePosition = {
          xVal = x.toDouble()
          yVal = y.toDouble()
          xVal = (xVal / ICON_WDTH).round()
          yVal = (yVal / ICON_HGHT).round()
          xVal = (xVal * ICON_WDTH)
          yVal = (yVal * ICON_HGHT)
          if (xVal < 0) xVal = 0
          if (yVal < 0) yVal = 0
          x = xVal.toString()
          y = yVal.toString()
          rv = true
          it = desktop.attributes().keysIterator()
          while (it.hasNext() && rv) {
            key = it.next().toString()
            if (key.startsWith("location.icon.")) {
              locatedIcon = key.substring(14)
              if (locatedIcon != icon && desktop.attributes().apply(key) == "$x:$y") {
                location = desktop.attributes().apply("location.icon.$icon".toString()).split(":")
                x = location[0]
                y = location[1]
                rv = false
              }
            }
          }
          rv
        }
        if (!updates.containsKey(desktop.rolonName().rolonUuid()))
          updates.put(desktop.rolonName.rolonUuid,
                  new UpdateRolon(notification.userUuid, notification.commandId, desktop.rolonName))
        desktopUpdate = updates.get(desktop.rolonName.rolonUuid)
        desktopUpdate.removeAttribute("move.icon.$icon")
        if (changePosition()) {
          desktopUpdate.setAttribute("location.icon.$icon", "$x:$y")
          if (!updates.containsKey(icon))
            updates.put(icon, new UpdateRolon(notification.userUuid, notification.commandId, new RolonName(icon)))
          iconUpdate = updates.get(icon)
          iconUpdate.setAttribute("xPosition", x)
          iconUpdate.setAttribute("yPosition", y)
        } else {
          selected = desktop.attributes().contains("selected") && desktop.attributes().apply("selected") == icon

          json = """{"message":"UpdateIcon","id":"$icon","selected":$selected,"xPosition":"$x","yPosition":"$y"}"""
          push(json)
        }
      } else if ((changedAttribute.startsWith("delete.icon.") ||
              changedAttribute.startsWith("delete.window.")) &&
              chgElt.attributes().contains("attributeValue")) {
        if (!updates.containsKey(desktop.rolonName().rolonUuid()))
          updates.put(desktop.rolonName.rolonUuid,
                  new UpdateRolon(notification.userUuid, notification.commandId, desktop.rolonName))
        desktopUpdate = updates.get(desktop.rolonName.rolonUuid)
        def params = changedAttribute.split("\\.")
        if (params[1] == "icon") {
          String icon = params[2]
          desktopUpdate.removeAttribute(changedAttribute)
          if (desktop.attributes().contains("location.icon.$icon".toString())) {
            desktopUpdate.removeAttribute("location.icon.$icon")
          }
          if (desktop.attributes().contains("selected") && desktop.attributes().apply("selected") == icon)
            desktopUpdate.removeAttribute("selected")
          json = """{"message":"DropIcon","id":"$icon"}"""
          push(json)
          deleteIcon = new DeleteRolon(notification.userUuid, notification.commandId, new RolonName(icon))
          deleteIcon.message(sourceActor, "ignore").send()
        } else if(params[1] == "window") {
          String window = params[2]
          desktopUpdate.removeAttribute(changedAttribute)
          if (desktop.attributes().contains("selected") && desktop.attributes().apply("selected") == window)
            desktopUpdate.removeAttribute("selected")
          json = """{"message":"DropWindow","id":"$window"}"""
          push(json)
          deleteWindow = new DeleteRolon(notification.userUuid, notification.commandId, new RolonName(window))
          deleteWindow.message(sourceActor, "ignore").send()
        }
      }
    } else if (chgElt.changeType() == "KernelAddElement" &&
            chgElt.effectedElement() == "//Children") {
      childName = chgElt.attributes().apply("elementName")
      if (context.containsKey("message") && context.get("message").header() == "newChildRequest") {
        if (context.get("message") instanceof RolonResponse) {
          RolonResponse child = context.get("message")
          if (child.rolonName.role == "window") {
            if (!updates.containsKey(desktop.rolonName().rolonUuid()))
              updates.put(desktop.rolonName.rolonUuid,
                      new UpdateRolon(notification.userUuid, notification.commandId, desktop.rolonName))
            desktopUpdate = updates.get(desktop.rolonName.rolonUuid)
            uuid = desktop.getAttribute("selected")
            if (uuid != "") desktopUpdate.setAttribute("unselect", uuid)
            desktopUpdate.setAttribute("selected", child.rolonName.rolonUuid)
          }
        }
      } else {
        childRequest = new ChildRequest(desktop.rolonName, childName, Timestamp.timestamp())
        childRequest.message(sourceActor, "newChildRequest").send()
      }
    }
  }

  updates.each {
    update ->
    update.getValue().message(sourceActor, "ignore").send()
  }
}
