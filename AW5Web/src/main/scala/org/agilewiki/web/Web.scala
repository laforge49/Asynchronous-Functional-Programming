/*
 * Copyright 2010 Bill La Forge
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

import java.util.Properties
import java.util.HashMap
import org.agilewiki.web.server.jetty.JettyServer
import org.agilewiki.web.template.XMLReaderPool
//import org.agilewiki.web.template.TemplateCache
import kernel.SystemKernelComponent
import util.actors.SystemActorsComponent
import util.locks.SystemLocksComponent
import util.com.udp.SystemUdpComponent
import kernel.element.operation.SystemElementsComponent
import core.SystemCoreComponent
import org.agilewiki.actors.SystemActorLayerComponent
import command.SystemCommandComponent
import util._
import actors.nonblocking.SystemNonBlockingComponent
import com.ark.SystemArkComponent
import com.lng.SystemLongProtocolComponent
import com.shrt.SystemShortProtocolComponent
import template.templatecache.TemplateCacheActor

class _Web(configurationProperties: Properties)
  extends SystemComposite
  with SystemShutdownComponent
  with SystemConfigurationComponent
  with SystemElementsComponent
  with SystemKernelComponent
  with SystemCoreComponent
  with Loggable
  with SystemActorsComponent
  with SystemNonBlockingComponent
  with SystemLocksComponent
  with SystemLongProtocolComponent
  with SystemShortProtocolComponent
  with SystemUdpComponent
  with SystemArkComponent
  with SystemServersComponent
  with SystemActorLayerComponent
  with SystemCommandComponent
  with SystemWebComponent {
  setProperties(configurationProperties)
  kernel.start
  udp.start
  actorLayer.start
  web.start
  logger info ("[SERVER:" + configuration.localServerName + "] STARTED")

  override def close {
    web.close
    actorLayer.close
    kernel.close
    logger info ("[ARK:" + configuration.localServerName + "] STOPPED")
  }

  override def initializeServer {
    core._initializeDb
  }
}

object Web {
  def apply(context: SystemComposite) = context.asInstanceOf[SystemWebComponent].web
}

trait SystemWebComponent {
  this: SystemComposite
    with SystemShutdownComponent
    with SystemConfigurationComponent
    with SystemElementsComponent
    with SystemKernelComponent
    with SystemCoreComponent
    with Loggable
    with SystemActorsComponent
    with SystemNonBlockingComponent
    with SystemLocksComponent
    with SystemUdpComponent
    with SystemActorLayerComponent
    with SystemCommandComponent =>

  protected lazy val _web = defineWeb

  protected def defineWeb = new Web

  def web = _web

  class Web extends JettyServer {
    var _server: Option[org.eclipse.jetty.server.Server] = None

    /*
    val xmlReaderPool = new XMLReaderPool(
      configuration.requiredIntProperty(MAX_XMLREADER_POOL_SIZE_PARAMETER))
    val templateCache = new TemplateCache(
      configuration.requiredIntProperty(MAX_TEMPLATE_CACHE_SIZE_PARAMETER),
      xmlReaderPool)
      */
    val templateCacheActor = TemplateCacheActor(SystemWebComponent.this)
    val elementMap: HashMap[String, String] = new HashMap[String, String]

    initialize

    def initialize {
      var more = true
      var i = 1
      while (more) {
        val elementName = configuration.property("xml." + i + ".elementName")
        if (elementName == null) more = false
        else {
          val actorClass = configuration.property("xml." + i + ".actorClass")
          elementMap.put(elementName, actorClass)
          i += 1
        }
      }
    }

    def start {
      startWebServer(SystemWebComponent.this)
    }

    def server = _server match {
      case None => null
      case Some(x) => x
    }

    def close {
      stopWebServer
    }
  }

}