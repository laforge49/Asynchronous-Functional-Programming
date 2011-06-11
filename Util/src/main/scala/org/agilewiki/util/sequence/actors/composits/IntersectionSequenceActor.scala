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

package org.agilewiki
package util
package sequence
package actors
package basic
package composites

import util.actors._
import msgs.ErrorMsg
import res.ClassName

class IntersectionSequenceActor(systemContext: SystemComposite, uuid: String)
        extends AsynchronousActor(systemContext, uuid) with SequenceConvenience {
  private var seqConv = Set[SequenceConvenience]()
  private var candidates = Set[SequenceConvenience]()
  private var positioned = Set[SequenceConvenience]()
  private var pending = Set[SequenceConvenience]()

  private var status = "new"
  private var ended = false

  private var rspActor: Option[InternalAddress] = None
  private var rspHeader: Any = null
  private var err: Option[ErrorMsg] = None

  final override def messageHandler = {
    case msg: CurrentMsg if status == "ready" => sendCurrent(msg)
    case msg: NextMsg if status == "ready" => sendNext(msg)
    case msg: ResultMsg if status == "fetching"=> resultMessage(msg)
    case msg: EndMsg if status == "fetching" => endMessage(msg)
    case msg: ErrorMsg if status == "fetching" => errorMessage(msg)
    case msg: InitMsg if status == "new" => init(msg)
    case msg if status == "ready" => unexpectedMsg(msg)
  }

  private def init(msg: InitMsg) {
    seqConv ++= msg.sequences
    ended = seqConv.isEmpty
    reverse = msg.reverse
    status = "ready"
    candidates = seqConv
  }

  private def sendCurrent(msg: CurrentMsg) {
    debug(msg)
    if (err.isDefined) msg.requester ! err.get
    else if (ended) msg.requester ! EndMsg(msg.header)
    else {
      rspHeader = msg.header
      rspActor = Some(msg.requester)
      if (isValid(msg.key)) {
        var key = msg.key
        if (lastResult != null && ((key == null) || (reverse && lastResult <= key) || (!reverse && lastResult >= key))) {
          rspActor.get ! ResultMsg(msg.header, lastResult)
          rspActor = None
        } else {
          status = "fetching"
          if (key == null) key = lastIntersection
          while (!candidates.isEmpty) {
            val tmp = candidates.last
            candidates -= tmp
            tmp.sendCurrent(this, key, tmp)
            pending += tmp
          }
        }
      }
    }
  }

  private def sendNext(msg: NextMsg) {
    debug(msg)
    if (err.isDefined) msg.requester ! err.get
    else if (ended) msg.requester ! EndMsg(msg.header)
    else {
      rspHeader = msg.header
      rspActor = Some(msg.requester)
      if (isValid(msg.key)) {
        var key = msg.key
        if (lastResult != null && key != null && ((reverse && lastResult < key) || (!reverse && lastResult > key))) {
          rspActor.get ! ResultMsg(msg.header, lastResult)
          rspActor = None
        } else {
          status = "fetching"
          if (key == null) key = if (lastResult != null) lastResult else lastIntersection
          while (!candidates.isEmpty) {
            val tmp = candidates.last
            candidates -= tmp
            tmp.sendNext(this, key, tmp)
            pending += tmp
          }
        }
      }
    }
  }

  private def resultMessage(msg: ResultMsg) {
    debug(msg)
    if (!ended && err.isEmpty) {
      try {
        val sc = msg.headers.asInstanceOf[SequenceConvenience]
          if (positioned.isEmpty) {
            positioned += sc
            pending -= sc
          } else {
            val lResult = positioned.last.lastResult
            val cResult = msg.result
            if (lResult == cResult) {
              positioned += sc
              pending -= sc
            } else if ((reverse && lResult > cResult) || (!reverse && lResult < cResult)) {
              while (!positioned.isEmpty) {
                val tmp = positioned.last
                positioned -= tmp
                tmp.sendCurrent(this, lResult, tmp)
                pending += tmp
              }
              positioned += sc
              pending -= sc
            } else {
              sc.sendCurrent(this, cResult, sc)
            }
          }

          if (positioned == seqConv) {
            lastResult = positioned.last.lastResult
            rspActor.get ! ResultMsg(rspHeader, lastResult)
            rspActor = None
          }
        if (pending.isEmpty) {
          status = "ready"
          candidates = seqConv
          positioned = Set.empty[SequenceConvenience]
        }
      } catch {
        case ex: Throwable => {
          err = Some(ErrorMsg(rspHeader, ex.toString, util.Configuration(localContext).localServerName, ClassName(getClass)))
          status = "ready"
          rspActor.get ! err.get
        }
      }
    }
  }

  private def endMessage(msg: EndMsg) {
    debug(msg)
    if (!ended) {
      ended = true
      status = "ready"
      candidates = Set.empty[SequenceConvenience]
      pending = Set.empty[SequenceConvenience]
      positioned = Set.empty[SequenceConvenience]
      rspActor.get ! EndMsg(rspHeader)
    }
  }

  private def errorMessage(msg: ErrorMsg) {
    error(msg)
    if (err.isEmpty) {
      err = Some(msg)
      status = "ready"
      candidates = Set.empty[SequenceConvenience]
      pending = Set.empty[SequenceConvenience]
      positioned = Set.empty[SequenceConvenience]
      rspActor.get ! err.get
    }
  }

  private def isValid(key: String) = {
    if (key != null && lastResult != null) {
      if ((reverse && key > lastResult) || (!reverse && key < lastResult)) {
        rspActor.get ! ErrorMsg(rspHeader, "Invalid key", util.Configuration(localContext).localServerName, ClassName(getClass))
        rspActor = None
        false
      } else true
    } else true
  }

  private def lastIntersection = {
    var pk: String = null
    for (seq <- candidates) {
      val tmp = seq.lastResult
      if ((pk == null) || (reverse && tmp < pk) || (!reverse && tmp > pk)) pk = tmp
    }
    pk
  }

}

object IntersectionSequenceActor {
  def apply(systemContext: SystemComposite, sequences: Iterable[SequenceConvenience],
            reverse: Boolean): IntersectionSequenceActor = {
    val intersection = Actors(systemContext).actorFromClassName(ClassName(classOf[IntersectionSequenceActor])).
            asInstanceOf[IntersectionSequenceActor]
    intersection ! InitMsg(sequences, reverse)
    intersection
  }

  def apply(systemContext: SystemComposite, sequences: java.util.List[SequenceConvenience],
            reverse: Boolean): IntersectionSequenceActor = {
    var iterable = List[SequenceConvenience]()
    for(i <- 0 until sequences.size){
      iterable :+= sequences.get(i)
    }
    IntersectionSequenceActor(systemContext, iterable, reverse)
  }
}