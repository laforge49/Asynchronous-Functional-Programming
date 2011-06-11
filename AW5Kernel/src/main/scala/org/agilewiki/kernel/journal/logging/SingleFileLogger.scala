/*
 * Copyright 2010 M.NAJI
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
package kernel
package journal
package logging

import util._
import jit.{JitMutableCursor, JitString}
import org.agilewiki.kernel.element.RolonRootElement
import util.Configuration
import java.io.{DataOutputStream, FileOutputStream, IOException}
import org.joda.time.DateTime

object SingleFileLogger {
  val LOG_FILE_NAME_PROPERTY = "LogFileNameProperty"
}

class SingleFileLogger extends JournalEntryLogger {
  private var writer: DataOutputStream = _

  override def write(je: RolonRootElement) {
    val systemContext = je.systemContext
    val ts = JitString.createJit(systemContext).asInstanceOf[JitString]
    val nm = je.getJitName
    var _ts = nm.substring(nm.length - Timestamp.TIMESTAMP_LENGTH)
    _ts = Timestamp.invert(_ts)
    ts.setString(_ts)
    val nje = je.clone.asInstanceOf[RolonRootElement]
    nje.contents.remove("effectedRolons")
    nje.contents.remove("Qualifications")
    nje.contents.remove("QualifierUsage")
    nje.contents.remove("Children")
    nje.contents.remove("Parents")
    val len = ts.jitByteLength + nje.jitByteLength
    val bytes = new Array[Byte](len)
    val mc = new JitMutableCursor(bytes, 0)
    ts.jitToBytes(mc)
    nje.jitToBytes(mc)
    writer.writeInt(len)
    writer.write(bytes)
  }

  override def initialize(systemContext: SystemComposite) {
    val fileName = Configuration(systemContext).requiredProperty(SingleFileLogger.LOG_FILE_NAME_PROPERTY)
    writer = new DataOutputStream(new FileOutputStream(fileName, true))
  }

  override def close {writer.close}
}

