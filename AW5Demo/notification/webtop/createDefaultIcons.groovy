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
import org.agilewiki.actors.application.update.CreateChild
import org.agilewiki.actors.RolonName
import org.agilewiki.actors.application.query.RolonRequest
import org.agilewiki.actors.application.update.RolonAlreadyExists
import org.agilewiki.actors.application.update.UpdateRolon
import org.agilewiki.actors.application.Qualification
import org.agilewiki.actors.application.EmptyResponse
import org.agilewiki.core.CoreNames


def newIcon = {
  parent, iconName, xPosition, yPosition, targetRolonUuid ->
  message = new CreateChild(notification.userUuid, notification.commandId, parent, "icon", iconName)
  message.setAttribute("targetRolonUuid", targetRolonUuid)
  message.setAttribute("xPosition", xPosition)
  message.setAttribute("yPosition", yPosition)
  message.setAttribute("selected", "false")
  header = iconName
  context.put(header, targetRolonUuid)
  qualification = new Qualification(null)
  qualification.setAttribute("notification", "webtop/icon/updated")
  message.updateQualification(qualification)

  qualification = new Qualification(new RolonName(notification.userUuid))
  qualification.setAttribute("access", "owner")
  message.updateQualification(qualification)

  message.message(sourceActor, header).send()
}

def linkIcon = {
  RolonName icon, String target, header ->
  if (context.containsKey(target)) {
    RolonResponse targetInfo = context.get(target).get(1)
    String commandId = null
    if (targetInfo.attributes.contains("lastCommand")) {
      commandId = targetInfo.attributes.apply("lastCommand")
    }
    message = new UpdateRolon(notification.userUuid(), notification.commandId(), targetInfo.rolonName, commandId)
    qualification = new Qualification(icon)
    qualification.setAttribute("notification", "webtop/icon/targetUpdated")
    message.updateQualification(qualification)
    message.message(sourceActor, header).send()

    message = new UpdateRolon(notification.userUuid(), notification.commandId(), icon, notification.commandId())
    message.setAttribute("targetRole",targetInfo.rolonName.role)
    if (targetInfo.attributes().contains("tagLine")) {
      message.setAttribute("tagLine", targetInfo.attributes().apply("tagLine"))
    }
    message.message(sourceActor, header).send()

  } else {
    context.put(target, [message.rolonName])
    message = new RolonRequest(new RolonName(target), org.agilewiki.kernel.Timestamp.timestamp())
    message.message(sourceActor, header).send()
  }

}

if (!context.containsKey("started")) {
  if (notification.change.rolon.role == "webtop" && notification.change.created) {
    context.put("started", true)
    parent = notification.change.rolon
    newIcon(parent, "Knowledge Base", "0", "0", context.HOME_UUID)
    newIcon(parent, context.USERS_NAME, "0", "80", context.USERS_UUID)
    newIcon(parent, context.GROUPS_NAME, "0", "160", context.GROUPS_UUID)
    newIcon(parent, context.HOME_NAME, "0", "240", notification.userUuid())
  }
} else {
  message = context.get("message")
  if (!context.containsKey(message.header())) {
    sourceActor.unexpectedMsg(context.get(message))
  } else {
    if (message instanceof RolonResponse) {
      target = context.get(message.header())
      if (context.containsKey(target)) {
        //The message is the RolonResponse containing the target Rolon
        rolonName = context.get(target).get(0)
        if (context.get(target).size() == 1) context.get(target).add(message)
        linkIcon(rolonName, target, message.header())
      } else {
        //The message is the RolonResponse containing the icon Rolon  
        linkIcon(message.rolonName, target, message.header())
      }
    } else {
      if (!(message instanceof RolonAlreadyExists || message instanceof EmptyResponse)) {
        sourceActor.unexpectedMsg(message)
      }
    }
  }
}
