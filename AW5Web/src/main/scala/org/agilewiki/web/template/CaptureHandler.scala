/*
 * Copyright 2010 Ruchi B
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

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import org.xml.sax.helpers.AttributesImpl
import org.agilewiki.web.template.saxmessages.{StartElementMsg, EndElementMsg, CharactersMsg}

class CaptureHandler extends DefaultHandler {
  var messageList = new SaxMessageList

  def initialize(msgLst: SaxMessageList) {
    messageList = msgLst
  }

  override def startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
    val attrs = new AttributesImpl(attributes)
    messageList.add(StartElementMsg(uri, localName, qName, attrs))
  }


  override def endElement(uri: String, localName: String, qName: String) {
    messageList.add(EndElementMsg(uri, localName, qName))
  }

  override def characters(ch: Array[Char], start: Int, length: Int) {
    val ch1 = new String(ch, start, length)
    messageList.add(CharactersMsg(ch1))
  }
}