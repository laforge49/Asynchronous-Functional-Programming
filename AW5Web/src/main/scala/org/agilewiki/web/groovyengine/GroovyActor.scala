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
package web
package groovyengine

import util.SystemComposite
import command.CommandLayer
import groovy.util.GroovyScriptEngine
import groovy.lang.Binding
import util.actors.{InternalAddress, Actors, AsynchronousActor}
import actors.application.Context

class GroovyActor(systemContext: SystemComposite, uuid: String)
  extends AsynchronousActor(systemContext, uuid) {
  var gse: GroovyScriptEngine = null

  override def messageHandler = {
    case msg: RunScriptRequest => {
      try {
        msg.groovyScript.run
        msg.requester ! RanScriptResponse(msg.header)
      } catch {
        case ex: Throwable => {
          ex.printStackTrace
          error(msg)
        }
      }
    }
    case msg: GroovyCreateRequest => {
      try {
        val gb = new Binding
        gb.setVariable("context",msg.context)
        val script = CommandLayer(localContext).gse.createScript(msg.scriptName.replace('-','_')+".groovy",gb)
        msg.requester ! ScriptResponse(msg.header, script)
      } catch {
        case ex: Throwable => {
          ex.printStackTrace
          error(msg)
        }
      }
    }
    case msg: GroovyRunRequest => {
      try {
        val gb = new Binding
        gb.setVariable("context",msg.context)
        val script = CommandLayer(localContext).gse.run(msg.scriptName.replace('-','_')+".groovy",gb)
        msg.requester ! RanResponse(msg.header)
      } catch {
        case ex: Throwable => {
          ex.printStackTrace
          error(msg)
        }
      }
    }
  }
}

object GroovyActor {
  def apply(systemContext: SystemComposite) = {
    val groovyActor = Actors(systemContext).
      actorFromClassName(classOf[GroovyActor].getName).asInstanceOf[GroovyActor]
    val gse = CommandLayer(systemContext).gse
    groovyActor.gse = gse
    groovyActor
  }

  def run(systemContext: SystemComposite,
          requester: InternalAddress,
          header: Any,
          scriptName: String,
          context: Context) {
    val actor = apply(systemContext)
    actor ! GroovyRunRequest(requester, header, scriptName, context)
  }
}
