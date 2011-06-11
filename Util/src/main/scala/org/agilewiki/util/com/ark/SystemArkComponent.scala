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

package org.agilewiki.util
package com
package ark

import actor.ArkActor
import com.udp.SystemUdpComponent
import actors.SystemActorsComponent
import actors.res.ClassName

object Ark {
  def apply(context: SystemComposite) = context.asInstanceOf[SystemArkComponent].ark

  def startArkServices(context: SystemComposite) = context.asInstanceOf[SystemArkComponent].startArkServices
}

trait SystemArkComponent {
  this: SystemComposite
          with SystemConfigurationComponent
          with SystemServersComponent
          with SystemUdpComponent
          with SystemActorsComponent =>

  protected lazy val _orgAgileWikiUtilComArkArk = defineArk

  protected def defineArk = new Ark

  def ark = _orgAgileWikiUtilComArkArk

  def startArkServices {}

  class Ark {
    def start {
      actors.actorFromClassName(ClassName(classOf[ArkActor]), ArkActor.UUID)
    }

    def actor = {
      var rv = actors.canonicalActorFromUuid(ArkActor.UUID)
      if (rv == null) synchronized {
        rv = if (rv == null) actors.actorFromClassName(ClassName(classOf[ArkActor]), ArkActor.UUID)
        else actors.canonicalActorFromUuid(ArkActor.UUID)
      }
      rv.asInstanceOf[ArkActor]
    }
  }

}

