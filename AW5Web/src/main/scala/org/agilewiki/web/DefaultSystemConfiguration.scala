/*
 * Copyright 2010  Bill La Forge
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

import comet.CometIntegrationListener
import java.util.Properties
import org.agilewiki.web.server.jetty.ConfigParams
import org.agilewiki.web.template.continuation.TemplateContinuationServlet
import org.cometd.server.continuation.ContinuationCometdServlet

object DefaultSystemConfiguration {
  def apply(properties: Properties, dbName: String, arkName: String, host: String, port: Int) {
    org.agilewiki.command.DefaultSystemConfiguration(properties, dbName, arkName, host, port)

    new WConfig(properties) {
      addServletContext(SLASH_TEMPLATES_SLASH + "*",
        classOf[TemplateContinuationServlet].getName)
      addServletContext("/comet/*",
        classOf[ContinuationCometdServlet].getName,Map(
          ("timeout","120000"),
          ("interval","0"),
          ("maxInterval","10000"),
          ("multiFrameInterval","2000"),
          ("logLevel","0"),
          ("directDeliver","true"),
          ("refsThreshold","10"),
          ("requestAvailable","true")))
      addServletContextEventListener(classOf[CometIntegrationListener].getName)
    }

    properties.put(MAX_XMLREADER_POOL_SIZE_PARAMETER, "" + 16)
    properties.put(MAX_TEMPLATE_CACHE_SIZE_PARAMETER, "" + 200)
    properties.put(DEFAULT_LANGUAGE_PARAMETER, "En")
    properties.put(DEFAULT_TIMEZONE_PARAMETER, "+05:30")
    properties.put(ConfigParams.WEB_SERVER_BINDING_PORT, "8080")
    //properties.put(ConfigParams.WEB_SERVER_BINDING_HOST, "localhost")
    properties.put(COMET_ROUTER_TEMPLATE_PARAMETER, "comet/router.xml")
  }
}
