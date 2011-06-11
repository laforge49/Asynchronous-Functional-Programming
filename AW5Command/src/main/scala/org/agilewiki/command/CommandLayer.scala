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
package command

import util._
import com.ark.SystemArkComponent
import com.lng.SystemLongProtocolComponent
import com.shrt.SystemShortProtocolComponent
import util.actors._
import nonblocking.SystemNonBlockingComponent
import res.ClassName
import java.net.URL
import groovy.util.GroovyScriptEngine
import java.io.File
import java.util.{TreeSet, Properties, HashMap}
import util.locks.SystemLocksComponent
import util.com.udp.SystemUdpComponent
import kernel.{Kernel, SystemKernelComponent}
import kernel.element.operation.SystemElementsComponent
import core.SystemCoreComponent
import org.agilewiki.actors.SystemActorLayerComponent

class _CommandLayer(configurationProperties: Properties)
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
  with SystemUdpComponent
  with SystemShortProtocolComponent
  with SystemLongProtocolComponent
  with SystemArkComponent
  with SystemServersComponent
  with SystemActorLayerComponent
  with SystemCommandComponent {
  setProperties(configurationProperties)
  kernel.start
  udp.start
  actorLayer.start
  logger info ("[SERVER:" + configuration.localServerName + "] STARTED")

  override def close {
    actorLayer.close
    kernel.close
    logger info ("[ARK:" + configuration.localServerName + "] STOPPED")
  }

  override def initializeServer {
    core._initializeDb
  }
}

object CommandLayer {
  def apply(context: SystemComposite) = context.asInstanceOf[SystemCommandComponent].commandLayer
}

trait SystemCommandComponent {
  this: SystemComposite
    with SystemShutdownComponent
    with SystemConfigurationComponent
    with SystemElementsComponent
    with SystemKernelComponent
    with SystemCoreComponent
    with Loggable
    with SystemActorsComponent
    with SystemLocksComponent
    with SystemUdpComponent
    with SystemActorLayerComponent =>

  protected lazy val _commandLayer = defineCommandLayer

  protected def defineCommandLayer = new CommandLayer

  def commandLayer = _commandLayer

  class CommandLayer {
    private val gcp: Array[URL] = Array(
      new File("web/groovy").getAbsoluteFile.toURI.toURL)
    val gse = new GroovyScriptEngine(gcp) ////////blocking 6 usages
    var mainDirectory = new File(configuration.requiredProperty(MAIN_DIRECTORY_PARAMETER)).getAbsoluteFile
    var versionCache = new VersionCache(SystemCommandComponent.this)
    var cmds = new HashMap[String, ClassName] {
      var more = true
      var ndx = 0
      while (more) {
        ndx += 1
        val cmdName = configuration.property(cmdNameKey(ndx))
        if (cmdName == null) more = false
        else {
          val cmdClass = configuration.requiredProperty(cmdClassKey(ndx))
          put(cmdName, ClassName(cmdClass))
        }
      }
    }

    initialize

    def initialize {
      var ndx = 0
      var more = true
      while (more) {
        ndx += 1
        val p = "role" + ndx
        val k = p + ".name"
        if (Configuration(SystemCommandComponent.this).contains(k)) {
          val v = String.valueOf(Configuration(SystemCommandComponent.this).property(k))
          Configuration(SystemCommandComponent.this).put("role." + v, p)
        } else
          more = false
      }
    }

    def fileUrl(fileName: String) = {
      var tfn = fileName
      if (tfn.startsWith("/")) tfn = tfn.substring(1)
      var file = new File(mainDirectory, tfn)
      var rv: String = null
      rv = file.toURI.toString
      rv
    }

    private def cmdNameKey(ndx: Int) = "cmd." + ndx + ".cmdName"

    private def cmdClassKey(ndx: Int) = "cmd." + ndx + ".cmdClass"

    def singletonUuid(roleName: String): String = {
      val kernel = Kernel(SystemCommandComponent.this)
      val role = kernel.role(roleName)
      if (role == null) return null
      val manager = role.getArkManager
      if (manager == null) return null
      roleName + "_" + roleName
    }
  }

}