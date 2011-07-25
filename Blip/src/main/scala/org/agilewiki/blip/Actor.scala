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
package org.agilewiki
package blip

import seq.NavSetSeq

class Actor(_mailbox: Mailbox, _factory: Factory)
  extends Responder with MsgSrc {
  val components = new java.util.LinkedHashMap[Class[_ <: ComponentFactory], Component]

  def component(componentFactoryClass: Class[_ <: ComponentFactory]) = {
    val c = components.get(componentFactoryClass)
    if (c == null) throw new IllegalArgumentException("Component not found: " +
      componentFactoryClass.getName)
    c
  }

  override def mailbox = _mailbox

  override def factory = _factory

  private val _activeActor = ActiveActor(this)

  implicit def activeActor: ActiveActor = _activeActor

  private var actorId: ActorId = null

  override def id = actorId

  def id(_id: ActorId) {
    if (actorId != null) throw new UnsupportedOperationException
    actorId = _id
  }

  private var _isSingleton = false

  def isSingleton = _isSingleton

  def singleton {
    id(ActorId(factoryId.value))
    _isSingleton = true
  }

  var _systemServices: SystemServices = null

  def setSystemServices(systemServices: SystemServices) {
    _systemServices = systemServices
  }

  override def systemServices: SystemServices = _systemServices

  def apply(msg: AnyRef)
           (responseFunction: Any => Unit)
           (implicit srcActor: ActiveActor) {
    val srcMailbox = {
      if (srcActor == null) null
      else srcActor.actor.mailbox
    }
    if (srcMailbox == null && mailbox != null) throw new UnsupportedOperationException(
      "An immutable actor can only send to another immutable actor."
    )
    if (safes.containsKey(msg.getClass)) sendSafe(msg, responseFunction, srcActor)
    else if (mailbox == null || mailbox == srcMailbox) sendSynchronous(msg, responseFunction)
    else srcMailbox.send(this, msg)(responseFunction)
  }

  def sendSynchronous(msg: AnyRef, responseFunction: Any => Unit) {
    val reqFunction = messageFunctions.get(msg.getClass)
    if (reqFunction == null) throw new UnsupportedOperationException(msg.getClass.getName)
    reqFunction(msg, responseFunction)
  }

  def sendSafe(msg: AnyRef, responseFunction: Any => Unit, srcActor: ActiveActor) {
    val safe = safes.get(msg.getClass)
    if (safe == null) throw new UnsupportedOperationException(msg.getClass.getName)
    safe.func(msg, responseFunction)(srcActor)
  }

  override def response(msg: MailboxRsp) {
    mailbox.response(msg)
  }

  lazy val messageClasses = {
    val smf = new java.util.TreeSet[Class[_ <: AnyRef]](
      new ClassComparator
    )
    smf.addAll(messageFunctions.keySet)
    smf.addAll(safes.keySet)
    new NavSetSeq(_mailbox, null, smf)
  }

  lazy val componentFactoryClasses = {
    val smf = new java.util.TreeSet[Class[_ <: ComponentFactory]](
      new ClassComparator
    )
    smf.addAll(components.keySet)
    new NavSetSeq(_mailbox, null, smf)
  }

  def requiredService(reqClass: Class[_ <: AnyRef]) {
    if (!messageFunctions.containsKey(reqClass) &&
      !safes.containsKey(reqClass))
      throw new UnsupportedOperationException("service missing for " + reqClass.getName)
  }
}
