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
package org.agilewiki
package actors
package application

import java.util.{TreeSet, TreeMap}
import core.CoreNames

case class Context(var contextMap: Map[String, Any]) extends CoreNames {
  def contains(name: String) = contextMap.contains(name)

  def get(name: String) = Context.get(contextMap, name)

  def getVar(name: String) = Context.getVar(contextMap, name)

  def setCon(name: String, value: String) {
    contextMap = Context.setCon(contextMap, name, value)
  }

  def newVar(name: String, value: String) {
    contextMap = Context.newVar(contextMap, name, value)
  }

  def isVar(name: String) = Context.isVar(contextMap, name)

  def setVar(name: String, value: String) {
    contextMap = Context.setVar(contextMap, name, value)
  }

  def makeVar(name: String, value: String) {
    contextMap = Context.makeVar(contextMap, name, value)
  }

  def stringMap(prefix: String) = Context.stringMap(contextMap, prefix)

  def stringSet = {
    val keys = new TreeSet[String]
    val it = contextMap.keys.iterator
    while (it.hasNext) {
      val k = it.next
      keys.add(k)
    }
    keys
  }

  def setSpecial(name: String, value: Any) {contextMap += (name -> value)}

  def getSpecial(name: String): Any = {
    if (contextMap.contains(name))
      contextMap(name)
    else
      null
  }
}

object Context {
  def stringMap(map: Map[String, Any], prefix: String) = {
    var p = prefix
    if (p.length > 0)
      p += "."
    val it = map.keysIterator
    val m: TreeMap[String, String] = new TreeMap[String, String]()
    while (it.hasNext) {
      val key = it.next
      if (key.startsWith(p)) {
        m.put(key.substring(p.length), String.valueOf(map(key)))
      }
    }
    m
  }

  def get(map: Map[String, Any], name: String) =
    if (map.contains(name)) String.valueOf(map(name))
    else ""

  def getVar(map: Map[String, Any], name: String): StringBuilder = {
    if (!map.contains(name))
      return null
    val rv = map(name)
    if (rv.isInstanceOf[StringBuilder])
      return rv.asInstanceOf[StringBuilder]
    null
  }

  def setCon(map: Map[String, Any], name: String, value: String) = {
    var m = map
    if (value.length > 0)
      m += (name -> value)
    else if (map.contains(name))
      m -= (name)
    m
  }

  def newVar(map: Map[String, Any], name: String, value: String) = {
    var m = map
    m += (name -> new StringBuilder(value))
    m
  }

  def isVar(map: Map[String, Any], name: String) =
    map.contains(name) && map(name).isInstanceOf[StringBuilder]

  def setVar(map: Map[String, Any], name: String, value: String) = {
    var sb = map(name).asInstanceOf[StringBuilder]
    sb.replace(0, sb.length, value)
    map
  }

  def makeVar(map: Map[String, Any], name: String, value: String) = {
    if (isVar(map, name)) setVar(map, name, value)
    else newVar(map, name, value)
  }
}