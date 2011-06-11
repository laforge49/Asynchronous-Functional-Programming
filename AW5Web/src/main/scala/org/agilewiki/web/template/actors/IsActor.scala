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
package org.agilewiki.web.template.actors

import org.agilewiki.actors.ActorLayer
import org.agilewiki.web.template.saxmessages.{EndElementMsg, CharactersMsg, StartElementMsg}
import org.xml.sax.Attributes
import org.agilewiki.util.SystemComposite

class IsActor(systemContext: SystemComposite, uuid: String)
        extends ElementActor(systemContext, uuid) {
  var include = false

  protected def test(attributes: Attributes) {
    var value = attributes.getValue("value")
    if (value == null)
      value = ""
    val name = attributes.getValue("name")
    if (name != null && context.contains(name))
      value = context.get(name)
    include = value.length > 0
  }

  override protected def firstStartElement(msg: StartElementMsg) {
    debug(msg)
    try {
      test(msg.attributes)
      router ! CharactersMsg("")
    } catch {
      case ex: Throwable => error(router, ex)
    }
  }

  override protected def startElement(msg: StartElementMsg) {
    debug(msg)
    if (include) router ! msg
    else router ! CharactersMsg("")
  }

  override protected def characters(msg: CharactersMsg) {
    debug(msg)
    if (include) router ! msg
    else router ! CharactersMsg("")
  }

  override protected def endElement(msg: EndElementMsg) {
    debug(msg)
    if (include) router ! msg
    else router ! CharactersMsg("")
  }

  override protected def lastEndElement(msg: EndElementMsg) {
    debug(msg)
    router ! CharactersMsg("")
  }

}

object IsActor extends TemplateActor("aw:is", classOf[IsActor].getName)
