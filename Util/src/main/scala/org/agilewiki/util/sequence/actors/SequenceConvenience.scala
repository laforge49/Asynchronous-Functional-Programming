/*
 * Copyright 2010 Bill La Forge.
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

import util.actors._

trait SequenceConvenience
        extends Agent
                with Comparable[SequenceConvenience] {

  this: InternalAddressActor =>

  var lastResult: String = null

  protected var reverse = false

  def isReverse = reverse

  def sendCurrent(reply: InternalAddress) {
    this ! CurrentMsg(reply, null, this)
  }

  def sendCurrent(reply: InternalAddress,
                  headers: Any) {
    this ! CurrentMsg(reply, null, headers)
  }

  def sendCurrent(reply: InternalAddress,
                  key: String) {
    this ! CurrentMsg(reply, key, this)
  }

  def sendCurrent(reply: InternalAddress,
                  key: String,
                  headers: Any) {
    this ! CurrentMsg(reply, key, headers)
  }

  def sendNext(reply: InternalAddress) {
    this ! NextMsg(reply, null, this)
  }

  def sendNext(reply: InternalAddress,
               headers: Any) {
    this ! NextMsg(reply, null, headers)
  }

  def sendNext(reply: InternalAddress,
               key: String) {
    this ! NextMsg(reply, key, this)
  }

  def sendNext(reply: InternalAddress,
               key: String,
               headers: Any) {
    this ! NextMsg(reply, key, headers)
  }

  override def equals(x: Any): Boolean = {
    x.isInstanceOf[SequenceConvenience] &&
            (x.asInstanceOf[SequenceConvenience].lastResult == lastResult)
  }

  override def compareTo(sc: SequenceConvenience): Int = {
    try {
      lastResult.compareTo(sc.lastResult)
    } catch {
      case ex: NullPointerException => throw new NoSuchElementException
    }
  }
}
   

