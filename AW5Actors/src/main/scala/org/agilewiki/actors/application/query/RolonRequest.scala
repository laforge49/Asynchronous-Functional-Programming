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
package actors
package application
package query

import util.actors._
import util.actors.res._
import exchange._
import kernel.Kernel
import util.RolonName
import util.com.{DataInputStack, DataOutputStack}

case class RolonRequest(rolon:RolonName, timestamp:String) extends ApplicationData {
  require(rolon != null)
  require(timestamp != null)
  override def payload(dos: DataOutputStack) = {
    dos writeUTF timestamp
    dos writeRolonName rolon
    dos writeUTF ROLON_REQUEST
    dos
  }
  private[application] override def sendableMessage(source:InternalAddress,header:Any) = {
    if(HomeServer(rolon, source.localContext) == util.Configuration(source.localContext).localServerName)
      Message.localRequestMessage(source,
        ClassName(ROLON_QUERY_ACTOR).actor(source.localContext),
        header,dataMessage)
    else Message.sendableLongMessage(source,
      ExternalAddress(HomeServer(rolon, source.localContext),ClassName(ROLON_QUERY_ACTOR)),
      header, dataMessage.payload)
  }
}

object RolonRequest {
  def apply(payload:DataInputStack)={
    val rolon = payload.readRolonName
    val timestamp = payload.readUTF
    new RolonRequest(rolon,timestamp)
  }
}