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
package db

import blip._
import services._

class RandomIOComponentFactory extends ComponentFactory {

  addDependency(classOf[PropertiesComponentFactory])

  override def instantiate(actor: Actor) = new RandomIOComponent(actor)
}

class RandomIOComponent(actor: Actor)
  extends Component(actor) {
  private val randomIO = new RandomIO

  bindSafe(classOf[ReadBytes], new SafeForward(randomIO))
  bindSafe(classOf[WriteBytes], new SafeForward(randomIO))
  bind(classOf[ReadBytesOrNull], {
    (msg, rf) => exceptionHandler(msg, rf, readBytes) {
      (ex, mailbox) => {
        rf(null)
      }
    }
  })

  private def readBytes(msg: AnyRef, rf: Any => Unit) {
    val req = msg.asInstanceOf[ReadBytesOrNull]
    randomIO(ReadBytes(req.offset, req.length))(rf)
  }

  override def open {
    super.open
    randomIO.setSystemServices(systemServices)
    randomIO._open
  }

  override def close {
    randomIO.close
    super.close
  }
}

class RandomIO extends AsyncActor {
  var pathname: String = null
  var accessMode: String = null
  var file: java.io.File = null
  var randomAccessFile: java.io.RandomAccessFile = null

  bind(classOf[ReadBytes], read)
  bind(classOf[WriteBytes], write)

  def read(msg: AnyRef, rf: Any => Unit) {
    val req = msg.asInstanceOf[ReadBytes]
    val bytes = new Array[Byte](req.length)
    randomAccessFile.seek(req.offset)
    randomAccessFile.readFully(bytes)
    rf(bytes)
  }

  def write(msg: AnyRef, rf: Any => Unit) {
    val req = msg.asInstanceOf[WriteBytes]
    randomAccessFile.seek(req.offset)
    randomAccessFile.write(req.bytes)
    rf(null)
  }

  override def open {
    super.open
    pathname = GetProperty.required("dbPathname")
    accessMode = GetProperty.string("dbAccessMode", "rw")
    file = new java.io.File(pathname)
    randomAccessFile = new java.io.RandomAccessFile(file, accessMode)
  }

  override def close {
    try {
      randomAccessFile.close
    } catch {
      case unknown => {}
    }
    super.close
  }
}