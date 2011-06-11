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

import java.io.File
import java.util.Properties
import org.specs.SpecificationWithJUnit
import org.eclipse.jetty.client.HttpClient
import org.eclipse.jetty.client.ContentExchange
import org.eclipse.jetty.client.HttpExchange

class IntegratedJettyServerLifeCycleTestOld extends SpecificationWithJUnit {
  "Web Layer" should {
    "Start and Stop Web Server" in {
      val dbName = "JettyTest.aw5db"
      new File(dbName).delete

      val properties = new Properties()
      DefaultSystemConfiguration(properties, dbName, "Master", "localhost", 4444)

      ConfigWeb(properties)
      val sconfig = new ServletContextsConfig(properties)
      sconfig("/hello", classOf[HelloServlet].getName)
      val web = new _Web(properties)

      while (web.web.server == null || !web.web.server.isStarted) {}

      web.web.server == null must be equalTo false

      web.web.isWebServerStarted must be equalTo true
      web.web.isWebServerStopped must be equalTo false

      val client = new HttpClient
      client.start

      val exchange = new ContentExchange(true)
      exchange setURL "http://localhost:8080/hello"

      client send exchange

      exchange.waitForDone == HttpExchange.STATUS_COMPLETED must be equalTo true

      exchange.getResponseContent must be equalTo "Hello"

      web.close

      web.web.isWebServerStarted must be equalTo false
      web.web.isWebServerStopped must be equalTo true
    }
  }
}
