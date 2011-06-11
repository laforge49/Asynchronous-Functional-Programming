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

import util.actors._
import query.{RolonResponse, RolonRequest}
import update._
import util.com.{DataInputStack, DataOutputStack}

abstract class ApplicationData extends Messageable {
  def payload(dos: DataOutputStack): DataOutputStack
  final def payload: DataOutputStack = {
    val dos = new DataOutputStack
    payload(dos)
  }
}

object ApplicationData {
  def apply(payload: DataInputStack): ApplicationData = {
    val objectType = payload.readUTF
    objectType match {
      case EMPTY_RESPONSE => EmptyResponse(payload)
      case ROLON_DOES_NOT_EXIST_REPONSE => RolonDoesNotExist(payload)
      case ROLON_REQUEST => RolonRequest(payload)
      case ROLON_RESPONSE => RolonResponse(payload)
      case ROLON_ALREADY_EXISTS => RolonAlreadyExists(payload)
      case ROLON_OUT_OF_SYNC => RolonOutOfSync(payload)
      case _ => throw new UnsupportedOperationException
    }
  }
}

trait Messageable {
  this: ApplicationData =>

  private var _header: Option[Any] = None
  private var ref: Option[RepliableMessage] = None

  protected[application] def reference(reference: ReceivableMessage) {
    ref match {
      case None if (reference.isInstanceOf[RepliableMessage]) => ref = Some(reference.asInstanceOf[RepliableMessage])
      case None if (reference.isInstanceOf[ReceivableMessage]) => _header = Some(reference.header)
      case _ => throw new IllegalStateException
    }
  }

  final def header = {
    _header match {
      case Some(hdr) => hdr
      case None => message.header
    }
  }

  final def message: RepliableMessage = {
    ref match {
      case None => throw new UnsupportedOperationException("Source and Header arguments are missing")
      case Some(x) => x
    }
  }

  final def message(source: InternalAddress, header: Any): SendableMessage = {
    ref match {
      case None => source match {
        case src: ApplicationActor => {
          sendableMessage(source, header)
        }
        case src => sendableMessage(source, header)
      }
      case Some(x) => throw new UnsupportedOperationException("This object is already messageable")
    }
  }

  private[application] def sendableMessage(source: InternalAddress, header: Any): SendableMessage

  private[application] var dataMessage: ApplicationData = this

}