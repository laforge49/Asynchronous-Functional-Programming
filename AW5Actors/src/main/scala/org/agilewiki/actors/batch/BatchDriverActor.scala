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

import org.agilewiki.util.actors.res.ClassName
import org.agilewiki.util.SystemComposite
import kernel.element.RolonRootElement
import util.actors.{InternalAddress, AsynchronousActor, Actors}

object BatchDriverActor {
  def apply(systemContext: SystemComposite,
            requester: InternalAddress,
            header: Any,
            journalEntryRolon: RolonRootElement) {
    val actors = Actors(systemContext)
    val batchDriver = actors.actorFromClassName(ClassName(classOf[BatchDriverActor]), BATCH_DRIVER_ACTOR).
      asInstanceOf[BatchDriverActor]
    batchDriver ! DriverStartMsg(requester, header, journalEntryRolon)
  }
}

private[batch] case class DriverStartMsg(requester: InternalAddress,
                                         header: Any,
                                         journalEntryRolon: RolonRootElement)

case class DriverAckMsg(header: Any)

class BatchDriverActor(context: SystemComposite, uuid: String)
  extends AsynchronousActor(context, uuid) {
  val actorLayer = ActorLayer(context)
  val timestampManager = actorLayer.timestampManager
  var ts = timestampManager.openTransaction(this)
  val batchActor = BatchActor(context)
  var driverStartMsg: DriverStartMsg = null

  override def messageHandler = {
    case msg: DriverStartMsg => driverStart(msg)
    case msg: BatchPhase1AckMsg => batchPhase1Ack(msg)
    case msg: BatchPhase2AckMsg => batchPhase2Ack(msg)
    case msg: BatchPhase3AckMsg => batchPhase3Ack(msg)
    case msg: OutOfOrderMsg => outOfOrder(msg)
    case msg: DuplicateRelationshipMsg => duplicateRelationship(msg)
    case msg: MissingRelationshipMsg => missingRelationship(msg)
    case msg: ImplicitDeleteMsg => implicitDelete(msg)
    case msg: DeleteWithChildrenMsg => deleteWithChildren(msg)
    case msg: NegativeChildCountMsg => negativeChildCount(msg)
    case msg: BatchMissingUuidMsg => batchMissingUuid(msg)
    case msg: BatchSyncMsg => batchSyncMsg(msg)
    case msg => {
      timestampManager.closeTransaction(ts)
      unexpectedMsg(driverStartMsg.requester, msg)
    }
  }

  def driverStart(msg: DriverStartMsg) {
    driverStartMsg = msg
    batchActor ! BatchPhase1RequestMsg(this, null, ts, msg.journalEntryRolon)
  }

  def batchPhase1Ack(msg: BatchPhase1AckMsg) {
    batchActor ! BatchPhase2RequestMsg(this, null, ts)
  }

  def batchPhase2Ack(msg: BatchPhase2AckMsg) {
    batchActor ! BatchPhase3RequestMsg(this, null, ts)
  }

  def batchPhase3Ack(msg: BatchPhase3AckMsg) {
    timestampManager.closeTransaction(ts)
    driverStartMsg.requester ! DriverAckMsg(driverStartMsg.header)
  }

  def outOfOrder(msg: OutOfOrderMsg) {
    timestampManager.closeTransaction(ts)
    ts = timestampManager.openTransaction(this)
    batchActor ! BatchPhase1RequestMsg(this, null, ts, driverStartMsg.journalEntryRolon)
  }

  def duplicateRelationship(msg: DuplicateRelationshipMsg) {
    timestampManager.closeTransaction(ts)
    driverStartMsg.requester ! DuplicateRelationshipMsg(driverStartMsg.header, msg.uuid, msg.objUuid, msg.relType)
  }

  def missingRelationship(msg: MissingRelationshipMsg) {
    timestampManager.closeTransaction(ts)
    driverStartMsg.requester ! MissingRelationshipMsg(driverStartMsg.header, msg.uuid, msg.objUuid, msg.relType)
  }

  def implicitDelete(msg: ImplicitDeleteMsg) {
    timestampManager.closeTransaction(ts)
    driverStartMsg.requester ! ImplicitDeleteMsg(driverStartMsg.header, msg.uuid)
  }

  def deleteWithChildren(msg: DeleteWithChildrenMsg) {
    timestampManager.closeTransaction(ts)
    driverStartMsg.requester ! DeleteWithChildrenMsg(driverStartMsg.header, msg.uuid)
  }

  def negativeChildCount(msg: NegativeChildCountMsg) {
    timestampManager.closeTransaction(ts)
    driverStartMsg.requester ! NegativeChildCountMsg(driverStartMsg.header, msg.uuid)
  }

  def batchMissingUuid(msg: BatchMissingUuidMsg) {
    timestampManager.closeTransaction(ts)
    driverStartMsg.requester ! BatchMissingUuidMsg(driverStartMsg.header, msg.uuid)
  }

  def batchSyncMsg(msg: BatchSyncMsg) {
    timestampManager.closeTransaction(ts)
    Thread.sleep(1)
    driverStartMsg.requester ! BatchSyncMsg(
      driverStartMsg.header,
      msg.uuid)
  }
}
