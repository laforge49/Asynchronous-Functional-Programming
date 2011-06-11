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
package org.agilewiki.util

import java.util.Properties

object _Configuration {
  def defaultConfiguration(serverName: String) = {
    val properties = new Properties
    Configuration.defaultConfiguration(properties, serverName)
    properties
  }
}

class _Configuration(configurationProperties: Properties)
  extends SystemComposite
  with SystemConfigurationComponent {
  setProperties(configurationProperties)

  def close {}
}

object Configuration {
  val SERVER_NAME = "orgAgileWikiUtilServerName"

  def defaultConfiguration(properties: Properties, serverName: String) {
    properties.put(SERVER_NAME, serverName)
  }

  def apply(context: SystemComposite) = context.asInstanceOf[SystemConfigurationComponent].configuration
}

trait SystemConfigurationComponent {
  this: SystemComposite =>

  lazy val configuration = new Configuration(this)

  private var properties: Properties = null

  def setProperties(properties: Properties) {
    this.properties = properties
  }

  def getProperties = properties
}

class Configuration(context: SystemConfigurationComponent) {
  private val properties = context.getProperties

  def contains(propertyName: String) = properties.containsKey(propertyName)

  def iterator = properties.keySet.iterator

  lazy val propertyNames = new java.util.TreeSet[String] {
    var it = Configuration.this.iterator
    while (it.hasNext) {
      val pn = it.next
      add(String.valueOf(pn))
    }
  }

  def put(propertyName: String, value: String) = properties.put(propertyName, value)

  def property(propertyName: String): String = {
    properties.getProperty(propertyName)
  }

  private def property(propertyName: String, index: Int): String = {
    property(propertyName + "." + index)
  }

  private def countProperties(propertyName: String) = {
    var i = 0
    var more = true
    while (more) {
      if (property(propertyName, i) == null) {
        more = false
      } else {
        i += 1
      }
    }
    i
  }

  def requiredProperty(propertyName: String) = {
    val rv = property(propertyName)
    if (rv == null) {
      throw new IllegalStateException("Missing property: " + propertyName)
    }
    rv
  }

  def requiredIntProperty(propertyName: String) = {
    requiredProperty(propertyName).toInt
  }

  def localServerName = requiredProperty(Configuration.SERVER_NAME)
}
