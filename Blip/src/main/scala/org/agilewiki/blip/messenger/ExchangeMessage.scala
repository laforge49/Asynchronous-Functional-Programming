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
package org.agilewiki.blip.messenger

/**
 * All messages sent to an ExchangeMessenger must ExchangeMessages.
 * And all ExchangeMessages must be either ExchangeRequests
 * or ExchangeResponses.
 */
class ExchangeMessage

/**
 * All requests sent to an ExchangeMessenger must be either
 * ExchangeRequests or subclasses of ExchangeRequest.
 */
class ExchangeRequest(_sender: MessageSource)
  extends ExchangeMessage {
  /**
   * The sender method returns the object which sent the request.
   */
  def sender = _sender
}

/**
 * All responses sent to an ExchangeMessenger must be either
 * ExchangeResponses or subclasses of ExchangeResponse.
 */
class ExchangeResponse extends ExchangeMessage