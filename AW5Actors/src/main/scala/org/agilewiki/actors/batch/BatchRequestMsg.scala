/*
 * Copyright 2011 B. La Forge
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
package org.agilewiki
package actors
package batch

import util.actors.InternalAddress
import util.actors.msgs.RequestMsg
import kernel.element.RolonRootElement

abstract class BatchRequestMsg(requesterParameter: InternalAddress, headerParameter: Any, startingTimeParameter: String)
  extends RequestMsg(requesterParameter, headerParameter) {

  def startingTime: String
}

case class BatchPhase1RequestMsg(requester: InternalAddress,
                                 header: Any,
                                 startingTime: String,
                                 journalEntryRolon: RolonRootElement)
  extends BatchRequestMsg(requester, header, startingTime) {
}

case class BatchPhase1AbortRequestMsg(requester: InternalAddress,
                                      header: Any,
                                      startingTime: String)
  extends BatchRequestMsg(requester, header, startingTime) {
}

case class BatchPhase2RequestMsg(requester: InternalAddress,
                                 header: Any,
                                 startingTime: String)
  extends BatchRequestMsg(requester, header, startingTime) {
}

case class BatchPhase2AbortRequestMsg(requester: InternalAddress,
                                      header: Any,
                                      startingTime: String)
  extends BatchRequestMsg(requester, header, startingTime) {
}

case class BatchPhase3RequestMsg(requester: InternalAddress,
                                 header: Any,
                                 startingTime: String)
  extends BatchRequestMsg(requester, header, startingTime) {

}

case class BatchRecoveryRequestMsg(requester: InternalAddress,
                                 header: Any,
                                 startingTime: String,
                                 journalEntryRolon: RolonRootElement)
  extends BatchRequestMsg(requester, header, startingTime) {
}
