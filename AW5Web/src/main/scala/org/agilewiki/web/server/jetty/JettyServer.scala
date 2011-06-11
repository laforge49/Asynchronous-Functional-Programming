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
package web
package server
package jetty

import javax.servlet.Servlet
import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.server.handler.ContextHandler
import org.eclipse.jetty.server.handler.HandlerList
import org.eclipse.jetty.server.handler.ResourceHandler
import org.eclipse.jetty.server.handler.DefaultHandler
import org.eclipse.jetty.webapp.WebAppContext
import java.util.EventListener
import util.{SystemComposite, Configuration}

trait JettyServer extends WebServer {
  var localContext: SystemComposite = _

  def server: Server

  override def isWebServerStarted = server != null && server.isStarted

  override def isWebServerStopped = server == null || server.isStopped

  override def startWebServer(systemContext: SystemComposite) {
    localContext = systemContext
    val port = Configuration(localContext).requiredIntProperty(ConfigParams.WEB_SERVER_BINDING_PORT)
    val host = Configuration(localContext).property(ConfigParams.WEB_SERVER_BINDING_HOST)

    val connector = new SelectChannelConnector
    connector setPort port
    if (host != null) connector setHost host
    Web(localContext)._server = Some(new Server)
    server addConnector connector

    val handler = new HandlerList
    server setHandler handler

    loadContexts(handler)
    org.agilewiki.web.server.jetty.integration.Servlet.register(port, localContext)


    new Thread {
      override def run {
        server.start
        server.join
      }
    }.start
  }

  override def stopWebServer {
    server.stop
  }

  private def loadServletContexts(context: ServletContextHandler) {
    var ndx = 1
    def pathNameKey = "servletContexts." + ndx + ".url-pattern"
    def classNameKey = "servletContexts." + ndx + ".servlet-class"
    def nxt {ndx += 1}
    var more = true
    while (more) {
      val pathname = Configuration(localContext).property(pathNameKey)
      more = pathname != null
      if (more) {
        val servletClass = ClassLoader.getSystemClassLoader.loadClass(Configuration(localContext).property(classNameKey))
        require(servletClass.newInstance.isInstanceOf[Servlet], "Invalid servlet class name: " + servletClass.getName)
        val servletHolder = new ServletHolder(servletClass)
        val params = ServletInitParameters(Configuration(localContext).property, ndx)
        servletHolder.setInitParameters(params)
        servletHolder.setInitOrder(1)
        context.addServlet(servletHolder, pathname)
        nxt
      }
    }
  }

  private def loadEventListeners(context: ServletContextHandler) {
    var ndx = 1
    def classNameKey = "servletContexts." + ndx + ".listener-class"
    def nxt { ndx += 1}
    var more = true
    while(more) {
      val listenerClassName = Configuration(localContext).property(classNameKey)
      if(listenerClassName != null){
        val listenerClass = ClassLoader.getSystemClassLoader.loadClass(listenerClassName).
                asInstanceOf[Class[EventListener]]
        val listener = listenerClass.getConstructors.find(p =>
          p.getParameterTypes.size == 1 &&
          p.getParameterTypes.apply(0).isInstanceOf[Class[AnyRef]]) match {
          case None => listenerClass.newInstance
          case Some(c) => c.newInstance(localContext)
        }
        require(listener.isInstanceOf[EventListener],
          "Invalid event listener class name: " + listenerClassName)
        context.addEventListener(listener.asInstanceOf[EventListener])
      } else more = false
      nxt
    }

  }

  private def loadContexts(context: HandlerList) {
    val sContext = new ServletContextHandler(ServletContextHandler.SESSIONS)
    context.addHandler(sContext)
    loadServletContexts(sContext)
    loadEventListeners(sContext)
    val resourceBase = Configuration(localContext).property("jetty.resourceBase")
    if (resourceBase != null) {
      val handler = new ResourceHandler()
      handler.setResourceBase(resourceBase)
      context.addHandler(handler)
    }

    //context.addHandler(new DefaultHandler)
/*
    val webAppPath: String = {
      def trim(x: String): String = if(x == null) null else if(x.endsWith("/"))
        trim(x.substring(0,x.length - 1)) else x
      trim(property("jetty.webapp.dir"))
    }
    if(webAppPath != null && webAppPath.isDefinedAt(0)){
      val webapp = new WebAppContext
      webapp.setDescriptor(webAppPath + "/WEB-INF/web.xml")
      webapp setResourceBase webAppPath
      webapp.setContextPath("/" + webAppPath.split("/").last)
      webapp setParentLoaderPriority true
      context addHandler webapp
    }
*/

  }
}
