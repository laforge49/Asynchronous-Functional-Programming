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

import kernel._
import org.agilewiki.kernel.{Kernel, JournalEntryFactory}
import org.agilewiki.kernel.queries.ExistsQuery
import org.agilewiki.util.{Configuration, Timestamp, SystemComposite}
import core.batch._
import util.actors.{Actors, InternalAddress, AsynchronousActor}
import util.actors.res.ClassName

object BatchActor {
  def apply(systemContext: SystemComposite) = {
    val actors = Actors(systemContext)
    actors.actorFromClassName(ClassName(classOf[BatchActor]), BATCH_ACTOR).asInstanceOf[BatchActor]
  }
}

class BatchActor(context: SystemComposite, uuid: String)
  extends AsynchronousActor(context, uuid) {

  val kernel = Kernel(context)
  var requester: InternalAddress = null
  var startingTime: String = null
  var jef: JournalEntryFactory = null
  var phaseCompleted = 0

  Actors(context).remember(this)

  def clear {
    requester = null
    startingTime = null
    jef = null
    phaseCompleted = 0
  }

  override def messageHandler = {
    case msg: BatchPhase1RequestMsg if startingTime == null || startingTime == msg.startingTime =>
      batchPhase1Request(msg)
    case msg: BatchPhase1AbortRequestMsg if startingTime == null || startingTime == msg.startingTime =>
      batchPhase1AbortRequest(msg)
    case msg: BatchPhase2RequestMsg if startingTime == null || startingTime == msg.startingTime =>
      batchPhase2Request(msg)
    case msg: BatchPhase2AbortRequestMsg if startingTime == null || startingTime == msg.startingTime =>
      batchPhase2AbortRequest(msg)
    case msg: BatchPhase3RequestMsg if startingTime == null || startingTime == msg.startingTime =>
      batchPhase3Request(msg)
    case msg: BatchRecoveryRequestMsg if startingTime == null =>
      batchRecoveryRequest(msg)
    case msg if startingTime == null =>
      unexpectedMsg(requester, msg)
  }

  def batchPhase1Request(msg: BatchPhase1RequestMsg) {
    requester = msg.requester
    var rsp: AnyRef = BatchPhase1AckMsg(msg.header)
    if (phaseCompleted == 1) {
      requester ! rsp
    } else if (phaseCompleted == 0) {
      startingTime = msg.startingTime
      jef = BatchJE.journalEntryFactory(msg.journalEntryRolon, startingTime)
      try {
        kernel.transactionPhase1(jef)
        phaseCompleted = 1
        requester ! rsp
      } catch {
        case ex: OutOfOrderException => {
          requester ! OutOfOrderMsg(msg.header)
          clear
        }
        case ex: DuplicateRelationshipException => {
          requester ! DuplicateRelationshipMsg(msg.header, ex.uuid, ex.objUuid, ex.relType)
          clear
        }
        case ex: MissingRelationshipException => {
          requester ! MissingRelationshipMsg(msg.header, ex.uuid, ex.objUuid, ex.relType)
          clear
        }
        case ex: ImplicitDeleteException => {
          requester ! ImplicitDeleteMsg(msg.header, ex.uuid)
          clear
        }
        case ex: DeleteWithChildrenException => {
          requester ! DeleteWithChildrenMsg(msg.header, ex.uuid)
          clear
        }
        case ex: NegativeChildCountException => {
          requester ! NegativeChildCountMsg(msg.header, ex.uuid)
          clear
        }
        case ex: MissingUuidException => {
          requester ! BatchMissingUuidMsg(msg.header, ex.uuid)
          clear
        }
        case ex: SyncException => {
          requester ! BatchSyncMsg(msg.header, ex.uuid)
          clear
        }
        case ex => {
          error(msg, ex)
          clear
        }
      }
    }
  }

  def batchPhase1AbortRequest(msg: BatchPhase1AbortRequestMsg) {
    requester = msg.requester
    try {
      if (phaseCompleted == 1) {
        kernel.transactionPhase1Abort
        clear
      }
      requester ! BatchPhase1AbortAckMsg(msg.header)
    } catch {
      case ex => {
        error(msg, ex)
        clear
      }
    }
  }

  def batchPhase2Request(msg: BatchPhase2RequestMsg) {
    requester = msg.requester
    try {
      if (phaseCompleted == 1) {
        kernel.transactionPhase2
        phaseCompleted = 2
      }
      if (phaseCompleted == 2)
        requester ! BatchPhase2AckMsg(msg.header)
    } catch {
      case ex => {
        error(msg, ex)
        clear
      }
    }
  }

  def batchPhase2AbortRequest(msg: BatchPhase2AbortRequestMsg) {
    requester = msg.requester
    try {
      if (phaseCompleted == 2) {
        kernel.transactionPhase2Abort
        clear
      }
      requester ! BatchPhase2AbortAckMsg(msg.header)
    } catch {
      case ex => {
        error(msg, ex)
        clear
      }
    }
  }

  def batchPhase3Request(msg: BatchPhase3RequestMsg) {
    requester = msg.requester
    try {
      if (phaseCompleted == 2)
        kernel.transactionPhase3
      requester ! BatchPhase3AckMsg(msg.header)
      clear
    } catch {
      case ex => {
        error(msg, ex)
        clear
      }
    }
  }

  def batchRecoveryRequest(msg: BatchRecoveryRequestMsg) {
    requester = msg.requester
    try {
      if (!ExistsQuery(context,
        Timestamp.invert(startingTime) + "_" + Configuration(context).localServerName)) {
        startingTime = msg.startingTime
        jef = BatchJE.journalEntryFactory(msg.journalEntryRolon, startingTime)
        kernel.transactionPhase1(jef)
        kernel.transactionPhase2
        kernel.transactionPhase3
      }
      requester ! BatchPhase3AckMsg(msg.header)
    } catch {
      case ex => {
        error(msg, ex)
        clear
      }
    }
  }
}