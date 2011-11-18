/*
 * Copyright 2011 Bill La Forge
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
package org.agilewiki.blip.bind

import org.agilewiki.blip.exchange._

trait BindActor
  extends ExchangeMessengerActor
  with Bindings {

  private var _superior: BindActor = null
  private val _activeActor = ActiveActor(this)

  implicit def activeActor: ActiveActor = _activeActor

  def setSuperior(superior: BindActor) {
    _superior = superior
  }

  def superior = _superior

  def newRequest(rf: Any => Unit,
                 data: AnyRef,
                 bound: QueuedLogic,
                 src: ExchangeMessengerSource) =
    new BindRequest(this, rf, data, bound, src)

  def apply(msg: AnyRef)
           (responseFunction: Any => Unit)
           (implicit srcActor: ActiveActor) {
    val messageLogic = messageLogics.get(msg.getClass)
    if (messageLogic != null) messageLogic.func(this, msg, responseFunction)(srcActor)
    else if (superior != null) superior(msg)(responseFunction)(srcActor)
    else {
      System.err.println("bindActor = " + this.getClass.getName)
      throw new IllegalArgumentException("Unknown type of message: " + msg.getClass.getName)
    }
  }
}