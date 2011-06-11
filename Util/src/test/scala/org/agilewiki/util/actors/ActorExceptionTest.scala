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
package actors

import junit.framework._
import res.ClassName

class ActorExceptionTest extends TestCase {
  val serverName = "Master"
  var systemContext: SystemComposite = _

  override protected def setUp() = {
    val properties = _Actors.defaultConfiguration(serverName)
    systemContext = new _Actors(properties)
  }

  def testStartingActorLayer {
    val actor = Actors(systemContext).actorFromClassName(ClassName(classOf[ExceptionActor])).asInstanceOf[ExceptionActor]
    var reply = new InternalAddressFuture(systemContext)
    actor ! reply
    val value = reply.get
    systemContext.asInstanceOf[SystemComposite].close
    Assert.assertEquals("Actor received an other exception",
      "Custom Exception", value)
  }
}

class ExceptionActor(systemContext: SystemComposite, uuid: String) extends AsynchronousActor(systemContext, uuid) {
  var reply: InternalAddress = _

  override def messageHandler = {
    case msg: InternalAddress =>
      reply = msg;
      throw new Exception("Custom Exception")
      reply ! "normal case"
    case _ =>
  }

  override def exceptionHandler = {
    case ex: Exception => {
      reply ! ex.getMessage
    }
  }
}