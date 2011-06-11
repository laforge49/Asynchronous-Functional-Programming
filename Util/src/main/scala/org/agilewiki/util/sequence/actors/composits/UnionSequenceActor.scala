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
import util.actors.res._
import java.util.{Comparator, PriorityQueue}

class UnionSequenceActor(systemContext: SystemComposite, uuid: String)
        extends AsynchronousActor(systemContext, uuid) with SequenceConvenience {
  private var seqConv: Iterable[SequenceConvenience] = Iterable.empty[SequenceConvenience]
  private var rspActor: Option[InternalAddress] = None
  private var rspHeaders: Any = null
  private var pq = new PriorityQueue[SequenceConvenience]
  private var pending = 0
  private var err: Option[ErrorMsg] = None

  final override def messageHandler = {
    case msg: CurrentMsg if pending == 0 => sendCurrent(msg)
    case msg: NextMsg if pending == 0 => sendNext(msg)
    case msg: ResultMsg if err.isEmpty => resultMsg(msg)
    case msg: EndMsg if err.isEmpty => endMsg(msg)
    case msg: ErrorMsg => errorMsg(msg)
    case msg: InitMsg => init(msg)
    case msg if pending == 0 => unexpectedMsg(msg)
  }

  private def sendCurrent(msg: CurrentMsg) {
    if (err.isDefined)
      msg.requester ! err.get
    else if (pq.isEmpty)
      msg.requester ! EndMsg(msg.header)
    else if (msg.key == null && lastResult != null)
      msg.requester ! ResultMsg(msg.header, lastResult)
    else if (msg.key != null && msg.key == lastResult)
      msg.requester ! ResultMsg(msg.header, lastResult)
    else {
      rspHeaders = msg.header
      rspActor = Some(msg.requester)
      if (isValid(msg.key)) {
        var key = msg.key
        if (key == null)
          key = lastResult
        val lr = pq.peek.lastResult
        if ((key == null) || (reverse && lr <= key) || (!reverse && lr >= key)) {
          lastResult = lr
          msg.requester ! ResultMsg(msg.header, lastResult)
          rspActor = None
        } else {
          while (!pq.isEmpty && (if (reverse) pq.peek.lastResult > key else pq.peek.lastResult < key)) {
            val temp = pq.poll
            temp.sendCurrent(this, key, temp)
            pending += 1
          }
        }
      }
    }
  }

  private def sendNext(msg: NextMsg) {
    if (err.isDefined)
      msg.requester ! err.get
    else if (pq.isEmpty)
      msg.requester ! EndMsg(msg.header)
    else {
      rspHeaders = msg.header
      rspActor = Some(msg.requester)
      if (isValid(msg.key)) {
        var key = msg.key
        if (key == null)
          key = lastResult
        val lr = pq.peek.lastResult
        if ((key == null) || (reverse && lr < key) || (!reverse && lr > key)) {
          lastResult = lr
          msg.requester ! ResultMsg(msg.header, lastResult)
          rspActor = None
        } else {
          while (!pq.isEmpty && (if (reverse) pq.peek.lastResult >= key else pq.peek.lastResult <= key)) {
            val temp = pq.poll
            temp.sendNext(this, key, temp)
            pending += 1
          }
        }
      }
    }
  }

  private def isValid(key: String) = {
    if (key != null && lastResult != null) {
      if ((reverse && key > lastResult) || (!reverse && key < lastResult)) {
        rspActor.get ! ErrorMsg(rspHeaders, "Invalid key",
          util.Configuration(localContext).localServerName, ClassName(getClass))
        rspActor = None
        false
      } else
        true
    } else
      true
  }

  private def resultMsg(msg: ResultMsg) {
    try {
      debug(msg)
      pending -= 1
      val sc = msg.headers.asInstanceOf[SequenceConvenience]
      pq.add(sc)
      if (pending == 0 && rspActor.isDefined) {
        val top = pq.peek
        lastResult = top.lastResult
        rspActor.get ! ResultMsg(rspHeaders, lastResult)
        rspActor = None
      }
    } catch {
      case ex: Throwable => errorMsg(ErrorMsg(msg.headers, ex.toString, util.Configuration(localContext).localServerName, ClassName(getClass)))
    }
  }

  private def endMsg(msg: EndMsg) {
    debug(msg)
    pending -= 1
    if (pending == 0 && rspActor.isDefined) {
      if (pq.isEmpty)
        rspActor.get ! EndMsg(rspHeaders)
      else {
        val top = pq.peek
        lastResult = top.lastResult
        rspActor.get ! ResultMsg(rspHeaders, lastResult)
        rspActor = None
      }
    }
  }

  private def errorMsg(msg: ErrorMsg) {
    debug(msg)
    err = Some(ErrorMsg(rspHeaders, msg.error, msg.serverName, msg.resourceName))
    pending = 0
    if (rspActor.isDefined) {
      rspActor.get ! err.get
      rspActor = None
    }
  }

  def init(msg: InitMsg) {
    seqConv = msg.sequences
    reverse = msg.reverse
    if (reverse)
      pq = new PriorityQueue[SequenceConvenience](1,
        new Comparator[SequenceConvenience] {
          override def compare(o1: SequenceConvenience, o2: SequenceConvenience): Int =
            o2.compareTo(o1)
        })
    for (temp <- seqConv) temp.sendCurrent(this, temp)
    pending = seqConv.size
  }
}

object UnionSequenceActor {
  def apply(systemContext: SystemComposite, sequences: Iterable[SequenceConvenience],
            reverse: Boolean): UnionSequenceActor = {
    val unionSequenceActor = Actors(systemContext).actorFromClassName(ClassName(classOf[UnionSequenceActor]))
            .asInstanceOf[UnionSequenceActor]
    unionSequenceActor ! InitMsg(sequences,reverse)
    unionSequenceActor
  }

  def apply(systemContext: SystemComposite, sequences: java.util.List[SequenceConvenience],
            reverse: Boolean): UnionSequenceActor = {
    var iterable = List[SequenceConvenience]()
    for(i <- 0 until sequences.size){
      iterable :+= sequences.get(i)
    }
    UnionSequenceActor(systemContext,iterable,reverse)
  }
}