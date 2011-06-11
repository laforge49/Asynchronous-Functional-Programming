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
import org.agilewiki.web.comet.PushCometMessage
import org.agilewiki.web.comet.CometActor
import org.agilewiki.actors.application.notification.Notification
import org.agilewiki.actors.application.notification.ChangeElement
import org.agilewiki.actors.application.update.UpdateRolon

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
if ((context.get("effectedRolon") instanceof RolonResponse) &&
        (context.get("desktinationRolon") instanceof RolonResponse)) {
  RolonResponse rolon = context.get("effectedRolon")
  RolonResponse window = context.get("destinationRolon")

  it = notification.change.changes.iterator()
  while (it.hasNext()) {
    ChangeElement chgElt = it.next()
    if (chgElt.changeType() == "KernelSetAttributeElement" &&
            (chgElt.effectedElement() == "/")) {
      attribute = chgElt.attributes().apply("attributeName")
      if (attribute == "tagLine") {
        def update = new UpdateRolon(notification.userUuid, notification.commandId, window.rolonName)
        update.setAttribute("tagLine", rolon.getAttribute("tagLine"))
        update.message(sourceActor, "ignore").send()
      }
    }
  }
}