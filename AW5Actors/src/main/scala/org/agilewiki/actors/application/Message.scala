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
import exchange._
import java.util.UUID
import util.com.{DataInputStack, DataOutputStack}
import util.com.shrt.{ShortProtocol, ShortReq, ShortRsp}
import util.com.lng.{LongProtocol, LongReq, LongRsp}

class Message {
  private var _payload: Option[Any] = None
  private var _header: Option[Any] = None

  def payload = _payload match {case None => null; case Some(x) => x}

  protected def payload(value: Any) {
    _payload match {
      case None => _payload = Some(value)
      case Some(x) if (value != x) => throw new IllegalStateException
      case _ =>
    }
  }

  def header = _header match {case None => null; case Some(x) => x}

  protected[application] def header(value: Any) {
    _header match {
      case None => _header = Some(value)
      case Some(x) => throw new IllegalArgumentException
    }
  }
}

object Message {
  def sendableLongMessage(source: InternalAddress, destination: ExternalAddress, header: Any, data: DataOutputStack) = {
    val msg = new Message with SendableLongMessage
    msg.source(source)
    msg.destination(destination)
    msg._header = Some(header)
    msg.payload(data)
    msg
  }

  def sendableShortMessage(source: InternalAddress, destination: ExternalAddress, header: Any, data: DataOutputStack) = {
    val msg = new Message with SendableShortMessage
    msg.source(source)
    msg.destination(destination)
    msg._header = Some(header)
    msg.payload(data)
    msg
  }

  def receivableMessage(msg: LongRsp) = {
    val message = new Message with ReceivableMessage
    message.payload(msg.payload)
    message.header(msg.headers)
    message
  }

  def receivableMessage(msg: ShortRsp) = {
    val message = new Message with ReceivableMessage
    message.payload(msg.payload)
    message.header(msg.headers)
    message
  }

  def repliableMessage(reference: Any) = {
    val msg = new Message with RepliableMessage
    msg.asInstanceOf[RepliableMessage].reference(reference)
    msg
  }

  def localRequestMessage(source: InternalAddress, destination: Address, header: Any, data: ApplicationData) = {
    val msg = new Message with LocalMessage
    msg.source(source)
    msg.destination(destination)
    msg._header = Some(header)
    msg.asInstanceOf[RepliableMessage].reference(data)
    msg
  }

  def localResponseMessage(header: Any, data: ApplicationData) = {
    val msg = new Message with ReceivableMessage
    msg.payload(data)
    msg._header = Some(header)
    msg
  }

  def errorMessage(header: Any, errorText: String, errorSource: ExternalAddress) = {
    val msg = ErrorMessage(errorText, errorSource)
    msg.header(header)
    msg
  }

}


trait ReceivableMessage extends Message {
  def content: ApplicationData = {
    val rv = payload match {
      case data: ApplicationData => data
      case data: DataInputStack => ApplicationData(data)
      case _ => throw new UnsupportedOperationException
    }
    rv.reference(this)
    rv
  }
}


trait RepliableMessage extends ReceivableMessage {
  private var _reference: Option[Any] = None
  private lazy val _content: Option[ApplicationData] = Some(super.content)

  override def content = _content.get

  private[application] def reference(ref: Any) {
    _reference match {
      case None => ref match {
        case x: ApplicationData => _reference = Some(this);
        payload(x)
        case x: LongReq => _reference = Some(ref);
        payload(x.payload);
        header(x.msgUuid)
        case x: ShortReq => _reference = Some(ref);
        payload(x.payload);
        header(x.msgUuid)
        case _ => throw new UnsupportedOperationException
      }
      case _ => throw new IllegalStateException
    }
  }

  def reply(content: ApplicationData) {
    _reference match {
      case None => throw new IllegalStateException
      case Some(x) => x match {
        case ref: LocalMessage => ref.source ! Message.localResponseMessage(header, content)
        case ref: LongReq => ref.sendRsp(content.payload)
        case ref: ShortReq => ref.sendRsp(content.payload)
        case _ => throw new UnsupportedOperationException
      }
    }
  }

  def error(errorText: String, errorSource: ExternalAddress) {
    _reference match {
      case None => throw new IllegalStateException
      case Some(x) => x match {
        case ref: LocalMessage => ref.source ! Message.errorMessage(header, errorText, errorSource)
        case ref: LongReq => ref.sendError(errorText, errorSource.ark, errorSource.messageConsumer)
        case ref: ShortReq => ref.sendError(errorText, errorSource.ark, errorSource.messageConsumer)
        case _ => throw new UnsupportedOperationException
      }
    }
  }

  def messageId : String = String.valueOf(header)
}


trait SendableMessage extends Message {
  private var _source: Option[InternalAddress] = None

  def source: InternalAddress = _source match {case None => null; case Some(x) => x}

  def source(value: InternalAddress) {
    _source match {
      case None => _source = Some(value)
      case _ => throw new IllegalStateException
    }
  }

  private var _destination: Option[Address] = None

  def destination: Address = _destination match {case None => null; case Some(x) => x}

  def destination(value: Address) {
    _destination match {
      case None => _destination = Some(value)
      case _ => throw new IllegalStateException
    }
  }

  def send
}


trait SendableLongMessage extends SendableMessage {
  override def destination: ExternalAddress = super.destination.asInstanceOf[ExternalAddress]

  override def payload: DataOutputStack = super.payload.asInstanceOf[DataOutputStack]

  def send {
    LongProtocol(source.localContext).sendRequest(source, destination.ark, destination.messageConsumer, header, payload)
  }
}


trait SendableShortMessage extends SendableMessage {
  override def destination: ExternalAddress = super.destination.asInstanceOf[ExternalAddress]

  override def payload: DataOutputStack = super.payload.asInstanceOf[DataOutputStack]

  def send {
    ShortProtocol(source.localContext).actor.sendReq(source, destination.ark, destination.messageConsumer, header, payload)
  }
}


trait LocalMessage extends SendableMessage with RepliableMessage {
  private lazy val msgId = UUID.randomUUID.toString
  override def destination: InternalAddress = super.destination.asInstanceOf[InternalAddress]

  override def payload: ApplicationData = super.payload.asInstanceOf[ApplicationData]

  def send {
    destination ! this
  }

  override def messageId : String = msgId
}

case class ErrorMessage(error: String, source: ExternalAddress) extends Message