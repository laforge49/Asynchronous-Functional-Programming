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
package template
package templatecache

import java.io.File
import java.net.URI
import util.cache._
import util.actors.{Actors, AsynchronousActor}
import util.{Configuration, SystemComposite}

class TemplateCacheActor(systemContext: SystemComposite, uuid: String)
  extends AsynchronousActor(systemContext, uuid) {
  var xmlReaderPool: XMLReaderPool = null
  var cm: CanonicalMap[SaxMessageList] = null

  override def messageHandler = {
    case msg: TemplateRequest => {
      val templateUri = msg.templateUri
      try {
        var template = cm.get(templateUri)
        if (template == null || isFileModified(templateUri, template.lastModified, template.length)) {
          val xmlReader = xmlReaderPool.get
          template = new SaxMessageList
          val captureHandler = xmlReader.getContentHandler.asInstanceOf[CaptureHandler]
          captureHandler.initialize(template)
          val uri = new URI(templateUri)
          val file = new File(uri)
          template.lastModified = file.lastModified
          template.length = file.length
          xmlReader.parse(templateUri)
          captureHandler.initialize(null)
          xmlReaderPool.release(xmlReader)
          cm.put(templateUri, template)
        }
        msg.requester ! TemplateResponse(msg.header, template)
      } catch {
        case ex: Throwable => {
          System.err.println("error on file" + msg.templateUri)
          ex.printStackTrace
          error(msg, "file " + msg.templateUri)
        }
      }
    }
  }

  private def isFileModified(templateUri: String, lastModifed: Long, length: Long): Boolean = {
    val uri = new URI(templateUri)
    var file = new File(uri)
    if (file.lastModified != lastModifed || file.length != length)
      return true
    else
      return false
  }
}

object TemplateCacheActor {
  def apply(systemContext: SystemComposite) = {
    val configuration = Configuration(systemContext)
    val xmlReaderPool = new XMLReaderPool(
      configuration.requiredIntProperty(MAX_XMLREADER_POOL_SIZE_PARAMETER))
    val templateCacheActor = Actors(systemContext).
      actorFromClassName(classOf[TemplateCacheActor].getName, "templateCacheActor").asInstanceOf[TemplateCacheActor]
    templateCacheActor.xmlReaderPool = xmlReaderPool
    val cm = new CanonicalMap[SaxMessageList](configuration.requiredIntProperty(MAX_TEMPLATE_CACHE_SIZE_PARAMETER))
    templateCacheActor.cm = cm
    templateCacheActor
  }
}
