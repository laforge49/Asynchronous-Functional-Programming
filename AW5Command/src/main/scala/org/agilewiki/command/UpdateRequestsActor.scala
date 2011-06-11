/*
 * Copyright 2010 Bill La Forge
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
package command

import messages.UpdateResponseMsg
import util.actors._
import nonblocking.NBActor
import util.actors.res._
import java.util.Map
import org.agilewiki.actors.application.ErrorMessage
import util.SystemComposite
import kernel.element.RolonRootElement
import actors.batch._
import core.CoreNames
import actors.application.Context

class UpdateRequestsActor(systemContext: SystemComposite, uuid: String)
  extends NBActor(systemContext, uuid) {
  var requester: InternalAddress = null
  var journalEntry: RolonRootElement = null
  var updateParameters: Map[String, Any] = null
  var context:Context = null

  def process {
    val rolonType = journalEntry.rolonType
    if (rolonType == null) driverAck
    else BatchDriverActor(systemContext, this, null, journalEntry)
  }

  override def messageHandler = {
    case msg: DriverAckMsg => driverAck
    case msg: DuplicateRelationshipMsg => duplicateRelationship(msg)
    case msg: MissingRelationshipMsg => missingRelationship(msg)
    case msg: ImplicitDeleteMsg => implicitDelete(msg)
    case msg: DeleteWithChildrenMsg => deleteWithChildren(msg)
    case msg: NegativeChildCountMsg => negativeChildCount(msg)
    case msg: BatchMissingUuidMsg => batchMissingUuid(msg)
    case msg: BatchSyncMsg => batchSyncMsg(msg)
    case msg: ErrorMessage => errorMessage(msg.error)
    case msg => {
      unexpectedMsg(requester, msg)
    }
  }

  def driverAck {
    val updateResponseMsg = UpdateResponseMsg(updateParameters)
    requester ! updateResponseMsg
  }

  def errorMessage(err: String) {
    try {
      val name = ""
      updateParameters.put(name + ".error", err)
      val updateResponseMsg = UpdateResponseMsg(updateParameters)
      requester ! updateResponseMsg
    } catch {
      case ex: Throwable => {
        ex.printStackTrace
        error(requester, ex)
      }
    }
  }

    def duplicateRelationship(msg: DuplicateRelationshipMsg) {
      try {
        val name = msg.uuid
        updateParameters.put("updateRequestInError", name)
        updateParameters.put(name + ".duplicateRelationship", "true")
        updateParameters.put(name + ".rolonUuid", msg.objUuid)
        val updateResponseMsg = UpdateResponseMsg(updateParameters)
        requester ! updateResponseMsg
      } catch {
        case ex: Throwable => {
          ex.printStackTrace
          error(requester, ex)
        }
      }
    }

    def missingRelationship(msg: MissingRelationshipMsg) {
      try {
        val name = msg.uuid
        updateParameters.put("updateRequestInError", name)
        updateParameters.put(name + ".missingRelationship", "true")
        updateParameters.put(name + ".rolonUuid", msg.objUuid)
        val updateResponseMsg = UpdateResponseMsg(updateParameters)
        requester ! updateResponseMsg
      } catch {
        case ex: Throwable => {
          ex.printStackTrace
          error(requester, ex)
        }
      }
    }

    def implicitDelete(msg: ImplicitDeleteMsg) {
      try {
        val name = msg.uuid
        updateParameters.put("updateRequestInError", name)
        updateParameters.put(name + ".implicitDelete", "true")
        //      updateParameters.put(name + ".rolonUuid", msg.uuid)
        val updateResponseMsg = UpdateResponseMsg(updateParameters)
        requester ! updateResponseMsg
      } catch {
        case ex: Throwable => {
          ex.printStackTrace
          error(requester, ex)
        }
      }
    }

  def deleteWithChildren(msg: DeleteWithChildrenMsg) {
    try {
      val name = msg.uuid
      updateParameters.put("updateRequestInError", name)
      updateParameters.put(name + ".deleteWithChildren", "true")
      //      updateParameters.put(name + ".rolonUuid", msg.uuid)
      val updateResponseMsg = UpdateResponseMsg(updateParameters)
      requester ! updateResponseMsg
    } catch {
      case ex: Throwable => {
        ex.printStackTrace
        error(requester, ex)
      }
    }
  }

  def negativeChildCount(msg: NegativeChildCountMsg) {
    try {
      val name = msg.uuid
      updateParameters.put("updateRequestInError", name)
      updateParameters.put(name + ".negativeChildCount", "true")
      //      updateParameters.put(name + ".rolonUuid", msg.uuid)
      val updateResponseMsg = UpdateResponseMsg(updateParameters)
      requester ! updateResponseMsg
    } catch {
      case ex: Throwable => {
        ex.printStackTrace
        error(requester, ex)
      }
    }
  }

  def batchMissingUuid(msg: BatchMissingUuidMsg) {
    try {
      val name = msg.uuid
      updateParameters.put("updateRequestInError", name)
      updateParameters.put(name + ".missing", "true")
      //      updateParameters.put(name + ".rolonUuid", msg.uuid)
      val updateResponseMsg = UpdateResponseMsg(updateParameters)
      requester ! updateResponseMsg
    } catch {
      case ex: Throwable => {
        ex.printStackTrace
        error(requester, ex)
      }
    }
  }

  def batchSyncMsg(msg: BatchSyncMsg) {
    try {
      val name = msg.uuid
      updateParameters.put("updateRequestInError", name)
      updateParameters.put(name + ".outdated", "true")
      //      updateParameters.put(name + ".rolonUuid", msg.uuid)
      val updateResponseMsg = UpdateResponseMsg(updateParameters)
      requester ! updateResponseMsg
    } catch {
      case ex: Throwable => {
        ex.printStackTrace
        error(requester, ex)
      }
    }
  }
}

object UpdateRequestsActor {
  def apply(systemContext: SystemComposite,
            requester: InternalAddress,
            journalEntry: RolonRootElement,
            updateParameters: Map[String, Any],
            context: Context) {
    val updateRequestsActor = Actors(systemContext).actorFromClassName(ClassName(classOf[UpdateRequestsActor])).
      asInstanceOf[UpdateRequestsActor]
    updateRequestsActor.requester = requester
    updateRequestsActor.journalEntry = journalEntry
    updateRequestsActor.updateParameters = updateParameters
    updateRequestsActor.context = context
    updateRequestsActor.process
  }
}