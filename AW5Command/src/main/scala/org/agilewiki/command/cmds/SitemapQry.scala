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
package org.agilewiki.command.cmds

import org.agilewiki.actors.ActorLayer
import java.util.{Properties}
import java.io.{FileInputStream, InputStreamReader, FileReader, File}
import org.agilewiki.actors.application.Context
import org.agilewiki.util.SystemComposite

class SitemapQry(systemContext: SystemComposite, uuid: String)
        extends SimpleQuery(systemContext, uuid) {
  val fileSeperator = "/"

  override protected def query: String = {
    var dir = "web"+fileSeperator+"templates"+fileSeperator+"En"
    var f = new File(dir)
    lookupDirProps(f)
    null
  }
  private def lookupDirProps(dir:File) {
    val files = dir.listFiles
    val absPath = dir.getAbsolutePath
    addPropertiesInContext(absPath)
    for (i <- 0 to files.length - 1) {
      val file = files(i).asInstanceOf[File]
      if(file.isDirectory)
        lookupDirProps(file)
    }
  }
  private def addPropertiesInContext(path:String) {
    val file = new File(path, "dir.prop")
    if(file.exists) {
      var prefix = "sitemap."
      val fis = new FileInputStream(file)
      val isr = new InputStreamReader(fis,"UTF-8")
      val p = new Properties
      p.load(isr)
      isr.close
      val i = path.indexOf("template")
      prefix = prefix+path.substring(i-1)
      prefix = prefix.replace('\\','/')
      val pns = p.stringPropertyNames
      val it = pns.iterator
      while (it.hasNext) {
        var n = it.next.asInstanceOf[String]
        if(n.endsWith(".html")) {
          val t = n+".link"
          if(!pns.contains(t)) {
            val v = p.get(n).asInstanceOf[String]
            context.setCon(prefix+fileSeperator+n, v)
          }
        }
        else if (n.endsWith(".link")){
          var i = n.indexOf(".link")
          var temp = n.substring(0,i)
          val v = p.get(temp).asInstanceOf[String]
          temp = p.get(n).asInstanceOf[String]
          context.setCon(prefix+fileSeperator+temp, v)
        }
      }
    }
  }
}

object SitemapQry {
  val name = "qry-sitemap"
  val cls = "org.agilewiki.command.cmds.SitemapQry"
}