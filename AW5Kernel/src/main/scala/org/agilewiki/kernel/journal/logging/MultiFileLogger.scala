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
package kernel
package journal
package logging

import util._
import jit.{JitMutableCursor, JitString}
import org.agilewiki.kernel.element.RolonRootElement
import util.Configuration
import org.joda.time.DateTime
import java.util.Properties
import java.io.{File, DataOutputStream, FileOutputStream, IOException}

object MultiFileLogger {
  val LOG_FILE_DIR_NAME_PROPERTY = "LogFileDirName"

  def apply(properties: Properties, journalLogDir: String) {
    properties.put(JOURNAL_ENTRY_LOGGER_CLASS_PARAMETER, classOf[MultiFileLogger].getName)
    properties.put(LOG_FILE_DIR_NAME_PROPERTY, journalLogDir)
  }

  def emptyDirectory(journalLogDir: String) {
    val file = new File(journalLogDir)
    if (!file.exists) return
    val files = file.listFiles
    var i = 0
    while (i < files.size) {
      if (!files(i).delete) throw new IllegalStateException("unable to delete log file " + files(i).getCanonicalPath)
      i += 1
    }
  }
}

class MultiFileLogger extends JournalEntryLogger {
  private var writer: DataOutputStream = _

  override def write(je: RolonRootElement) {
    val systemContext = je.systemContext
    val ts = JitString.createJit(systemContext).asInstanceOf[JitString]
    val nm = je.getJitName
    var _ts = nm.substring(nm.length - Timestamp.TIMESTAMP_LENGTH)
    _ts = Timestamp.invert(_ts)
    ts.setString(_ts)
    val nje = je.clone.asInstanceOf[RolonRootElement]
    nje.contents.remove("Objects")
    val len = ts.jitByteLength + nje.jitByteLength
    val bytes = new Array[Byte](len)
    val mc = new JitMutableCursor(bytes, 0)
    ts.jitToBytes(mc)
    nje.jitToBytes(mc)
    writer.writeInt(len)
    writer.write(bytes)
  }

  override def initialize(systemContext: SystemComposite) {
    val ts = (new DateTime).toString("yyyy-MM-dd_HH-mm-ss_SSS")
    val dirFileName = Configuration(systemContext).requiredProperty(MultiFileLogger.LOG_FILE_DIR_NAME_PROPERTY)
    val dir = new File(dirFileName)

    if (!dir.exists) dir.mkdirs

    val fileName = dir.getCanonicalPath + File.separator + ts + ".jnl"
    writer = new DataOutputStream(new FileOutputStream(fileName))
  }

  override def close {writer.close}
}

