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

import com.HostPort
import java.util.Properties

object Servers {

  def add(properties: Properties, serverName: String, host: String, port: Int) {
    var ndx = 1

    def serverNameKey = "server." + ndx + ".name"
    def hostKey = "server." + ndx + ".host"
    def portKey = "server." + ndx + ".port"
    def nxt {ndx += 1}

    while (properties.containsKey(serverNameKey)) nxt

    properties.put(serverNameKey, serverName)
    properties.put(hostKey, host)
    properties.put(portKey, "" + port)
    nxt
  }

  def apply(context: SystemComposite) = context.asInstanceOf[SystemServersComponent].servers
}

trait SystemServersComponent {
  this: SystemConfigurationComponent with SystemComposite =>

  protected lazy val _orgAgileWikiUtilServers = defineServers

  def servers = _orgAgileWikiUtilServers

  protected def defineServers = new Servers

  class Servers {
    var map = Map.empty[String,HostPort]
    def names = map.keySet
    def info(name: String) = map(name)
    init

    private def init{
      var ndx = 1
      var more = true
      while (more) {
        val prefix = "server." + ndx + "."
        val arkName = configuration.property(prefix + "name")
        if (arkName == null) more = false
        else {
          val host = configuration.requiredProperty(prefix + "host")
          val port = configuration.requiredIntProperty(prefix + "port")
          map += (arkName -> HostPort(host, port))
          ndx += 1
        }
      }
    }

  }
}