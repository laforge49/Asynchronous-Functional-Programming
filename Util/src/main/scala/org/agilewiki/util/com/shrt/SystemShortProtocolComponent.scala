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

package org.agilewiki.util
package com
package shrt

import actors.SystemActorsComponent
import actors.res.ClassName
import java.util.Properties

object ShortProtocol {
  val SHORT_ACTOR = "short"
  val SHORT_TIMEOUT_MIN_PROPERTY = "shortTimeoutMin"
  val SHORT_TIMEOUT_INC_PROPERTY = "shortTimoutInc"
  val SHORT_TIMEOUT_MAX_PROPERTY = "shortTimeoutMax"
  val SHORT_LIMIT_PROPERTY = "shortLimit"

  val MAX_PAYLOAD_SIZE_PROPERTY = "maxPayloadSize"

  val MAX_SHORT_MSG_UUID_CACHE_SIZE_PROPERTY = "maxShortMsgUuidCacheSize"

  def defaultConfiguration(properties: Properties,
                           timeOutMin: Int,
                           timeOutInc: Int,
                           timeOutMax: Int,
                           limit: Int,
                           maxMessageUuuidCacheSize: Int,
                           maxPayloadSize: Int) {
    properties.put(SHORT_TIMEOUT_MIN_PROPERTY, "" + timeOutMin)
    properties.put(SHORT_TIMEOUT_INC_PROPERTY, "" + timeOutInc)
    properties.put(SHORT_TIMEOUT_MAX_PROPERTY, "" + timeOutMax)
    properties.put(SHORT_LIMIT_PROPERTY, "" + limit)
    properties.put(MAX_SHORT_MSG_UUID_CACHE_SIZE_PROPERTY, "" + maxMessageUuuidCacheSize)
    properties.put(MAX_PAYLOAD_SIZE_PROPERTY, "" + maxPayloadSize)
  }

  def apply(context: SystemComposite) = context.asInstanceOf[SystemShortProtocolComponent].shortProtocol
}

trait SystemShortProtocolComponent {
  this: SystemComposite with SystemActorsComponent =>

  protected lazy val _orgAgileWikiUtilComShrtShortProtocol = defineShortProtocol

  protected def defineShortProtocol = new ShortProtocol

  def shortProtocol = _orgAgileWikiUtilComShrtShortProtocol


  class ShortProtocol {
    def actor = {
      var rv = actors.canonicalActorFromUuid(ShortProtocol.SHORT_ACTOR)
      if (rv == null) synchronized {
        rv = if (rv == null) 
          actors.actorFromClassName(ClassName(classOf[ShortReqRouterActor]), ShortProtocol.SHORT_ACTOR)
        else actors.canonicalActorFromUuid(ShortProtocol.SHORT_ACTOR)
      }
      rv.asInstanceOf[ShortReqRouterActor]
    }
  }
}