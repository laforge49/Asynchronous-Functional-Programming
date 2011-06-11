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
package util
package sequence
package actors
package basic
package composites

import util.actors._
import msgs.ErrorMsg
import util.actors.res._

abstract class SingleSequenceComposite(systemContext: SystemComposite, uuid: String)
        extends AsynchronousActor(systemContext, uuid) with SequenceConvenience {
  protected var requester: Option[InternalAddress] = None
  protected var sequence: SequenceConvenience = null
  protected var header: Any = null


  override def messageHandler = {
    case msg: CurrentMsg if requester.isEmpty => process(msg)
    case msg: NextMsg if requester.isEmpty => process(msg)
    case msg: ResultMsg if requester.isDefined => process(msg)
    case msg: EndMsg if requester.isDefined => process(msg)
    case msg: ErrorMsg if requester.isDefined => process(msg)
    case msg: ResultMsg if requester.isEmpty => unexpectedMsg(msg)
    case msg: EndMsg if requester.isEmpty => unexpectedMsg(msg)
    case msg: ErrorMsg if requester.isEmpty => unexpectedMsg(msg)
    case msg => unexpectedMsg(msg)
  }

  override def exceptionHandler = {
    case ex => {
      requester match {
        case Some(req) => process(ErrorMsg(header, ex.toString, util.Configuration(localContext).localServerName, ClassName(getClass)))
        case None => super.exceptionHandler(ex)
      }
    }
  }

  protected def process(msg: CurrentMsg)
  protected def process(msg: NextMsg)
  protected def process(msg: ResultMsg)
  protected def process(msg: EndMsg)
  protected def process(msg: ErrorMsg)
}