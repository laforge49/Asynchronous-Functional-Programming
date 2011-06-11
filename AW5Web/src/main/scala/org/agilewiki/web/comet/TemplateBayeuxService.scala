/*
 * Copyright 2010  M.Naji
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
package web
package comet

import collection.mutable.{HashMap, Set}
import util.actors.res._
import org.cometd.{Client, ClientBayeuxListener, Bayeux}
import org.cometd.server.BayeuxService
import org.eclipse.jetty.util.ajax.JSON
import util.actors.Actors
import util.{SystemComposite, Configuration}
import core.CoreNames

class TemplateBayeuxService(bayeux: Bayeux, systemContext: SystemComposite) extends BayeuxService(bayeux, "TemplateBayeux")
        with ClientBayeuxListener {
  private var authorizedClients = HashMap[String, String]()
  private var channelsSubscribers = HashMap[String, Set[String]]()
  subscribe(CometChannel.wildcardChannel, "handleMessage")
  private val cometActor = Actors(systemContext).actorFromClassName(ClassName(classOf[CometActor]), COMET_ACTOR).asInstanceOf[CometActor]
  cometActor.service = this

  override def clientAdded(client: Client) {
    if (client != null && !client.isLocal) {
      val session = bayeux.getCurrentRequest.getSession
      var userUuid = session.getAttribute("userUuid").asInstanceOf[String]
      if (userUuid == null || userUuid.trim.isEmpty) userUuid = CoreNames.ANONYMOUS_UUID
      var languageCode = session.getAttribute("language").asInstanceOf[String]
      if (languageCode == null || languageCode.trim.isEmpty) {
        languageCode = Configuration(systemContext).requiredProperty(DEFAULT_LANGUAGE_PARAMETER)
        session.setAttribute("language", languageCode)
      }
      var timezone = session.getAttribute("timezone").asInstanceOf[String]
      if (timezone == null || timezone.trim.isEmpty) {
        timezone = Configuration(systemContext).requiredProperty(DEFAULT_TIMEZONE_PARAMETER)
        session.setAttribute("timezone", timezone)
      }
      val clientId = client.getId
      authorizedClients += (clientId -> userUuid)
      if (!channelsSubscribers.contains(userUuid))
        channelsSubscribers += (userUuid -> Set.empty[String])
      channelsSubscribers(userUuid) += clientId

    }
  }

  override def clientRemoved(client: Client) {
    val user = authorizedClients.get(client.getId)
    if (user.isDefined) {
      val userId = user.get
      if (userId != null) {
        authorizedClients remove client.getId
        if (channelsSubscribers contains userId) {
          channelsSubscribers(userId) remove client.getId
          if (channelsSubscribers(userId).isEmpty) channelsSubscribers remove userId
        }
      }
    }
  }

  def handleMessage(client: Client, data: org.cometd.Message) {
    if (client != null && !client.isLocal && data.getChannel != null &&
            authorizedClients.contains(client.getId) &&
            CometChannel.userChannel(authorizedClients(client.getId)) == data.getChannel) {
      val actor = Actors(systemContext).actorFromClassName(ClassName(classOf[TemplateBayeuxActor])).
              asInstanceOf[TemplateBayeuxActor]
      val session = bayeux.getCurrentRequest.getSession
      var userUuid = session.getAttribute("userUuid").asInstanceOf[String]
      if (userUuid == null || userUuid.trim.isEmpty) userUuid = CoreNames.ANONYMOUS_UUID
      val languageCode = session.getAttribute("language").asInstanceOf[String]
      val timezone = session.getAttribute("timezone").asInstanceOf[String]
      val msg = CometRequest(userUuid, languageCode, timezone, Message(data))
      actor(this, session)
      actor ! msg
    }
  }

  private def push(clientId: String, userUuid: String, data: Any, broadcast: Boolean) {
    val json = data.asInstanceOf[java.util.Map[String, AnyRef]]
    json.put("broadcast", broadcast.asInstanceOf[AnyRef])
    bayeux.getClient(clientId).deliver(getClient, CometChannel.userChannel(userUuid), json, null)
  }

  def deliver(userUuid: String, data: Any) {
    if (channelsSubscribers contains userUuid) {
      channelsSubscribers(userUuid).foreach(clientId => {
        data match {
          case null =>
          case scpt: String => {
            try {
              val json = new JSON
              if (scpt.trim.isDefinedAt(0)) push(clientId, userUuid, json.fromJSON(scpt), true)
            } catch {
              case ex: java.lang.IllegalStateException => {
                println("JSON: " + scpt)
                throw ex
              }
              case ex => throw ex
            }
          }
          case _ => push(clientId, userUuid, data, true)
        }
      })
    }
  }

  def deliver(userUuid: String, data: Any, clientId: String) {
    if (channelsSubscribers contains userUuid) {
      if (channelsSubscribers(userUuid).contains(clientId)) {
        data match {
          case null =>
          case scpt: String => {
            try {
              val json = new JSON
              if (scpt.trim.isDefinedAt(0)) push(clientId, userUuid, json.fromJSON(scpt), false)
            } catch {
              case ex => {
                println("JSON: " + scpt)
                throw ex
              }
              case ex => throw ex
            }
          }
          case _ => push(clientId, userUuid, data, false)
        }
      }
    }
  }

  def sendErrorMessage(userUuid: String, error: String) {
    val data = "{\"message\":\"warning\",\"text\":\"" + error + "\"}"
    deliver(userUuid, data)
  }

}