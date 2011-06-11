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

import java.util.Properties

class ServletContextsConfig(properties: Properties) {
  private var ndx = 1

  private def servletId = "servletContexts." + ndx

  private def pathNameKey =  servletId + ".url-pattern"

  private def classNameKey = servletId + ".servlet-class"

  private def nxt {ndx += 1}

  while (properties.containsKey(pathNameKey)) nxt

  def apply(pathName: String, className: String, initParams: Map[String,String]) {
    properties.put(pathNameKey, pathName)
    properties.put(classNameKey, className)
    var x = 1
    def initParamName = servletId + ".init-param." + x + ".name"
    def initParamValue = servletId + ".init-param." + x + ".value"
    initParams.foreach(att => {
      properties.put(initParamName, att._1)
      properties.put(initParamValue, att._2)
      x += 1
    })
    nxt
  }

  def apply(pathName: String, className: String) {
    this(pathName,className,Map.empty[String,String])
  }

}

