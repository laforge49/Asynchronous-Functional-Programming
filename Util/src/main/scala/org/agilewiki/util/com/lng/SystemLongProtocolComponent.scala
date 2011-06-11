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
package lng

import actors.res.{ResourceName, ClassName}
import actors.{InternalAddress, Actors, SystemActorsComponent}


object LongProtocol {
  def apply(context: SystemComposite) = context.asInstanceOf[SystemLongProtocolComponent].longProtocol
}
trait SystemLongProtocolComponent {
  this: SystemComposite
          with SystemActorsComponent =>

  protected lazy val _orgAgileWikiUtilComLngLongProtocol = defineLongProtocol

  protected def defineLongProtocol = new LongProtocol

  def longProtocol = _orgAgileWikiUtilComLngLongProtocol

  class LongProtocol {
    def sendRequest(sender: InternalAddress,
                destinationArkName: String,
                destinationActorName: ResourceName,
                headers: Any,
                payload: DataOutputStack) {
      val longTransceiverActor = Actors(SystemLongProtocolComponent.this).
              actorFromClassName(ClassName(classOf[LongTransceiverActor])).
              asInstanceOf[LongTransceiverActor]
      longTransceiverActor ! SendLongReq(sender,
        destinationArkName,
        destinationActorName,
        headers,
        payload)
    }
  }
}