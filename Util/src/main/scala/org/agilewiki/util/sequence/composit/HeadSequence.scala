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
package util
package sequence
package composit

/**
 * Provides access to the keys in a sequence up to a given limit.
 * @param ss The SequenceSource to be wrapped.
 * @param limit The key which defines the end of the sequence (excluded).
 */
class HeadSequence(ss: SequenceSource, limit: String) extends SequenceSource {
  require(ss != null, "The wrapped sequence cannot be null")
  require(limit != null, "The head sequence limit cannot be null")

  private def limitChk(key: String): Boolean = if (isReverse) key > limit else key < limit

  override def isReverse = ss.isReverse

  override def current = super.current

  override def current(key: String): String = {
    var crt = ss.current(key)
    if (crt != null) {
      if (!limitChk(crt)) {
        crt = null
      }
      if (crt != null && key != null && (if (isReverse) crt > key else crt < key))
        throw new IllegalStateException("current set to before key")
    }
    //    if (crt!=null && crt.length==1)
    //      println("!!!!!!!!!!! "+crt.charAt(0).asInstanceOf[Int]+" - "+limit.charAt(0).asInstanceOf[Int])
    crt
  }

  override def next(key: String): String = {
    if (key == null) {
      //      if (current!=null && current.length==1)
      //        println("!!!!!!!!!!! "+current.charAt(0).asInstanceOf[Int]+" - "+limit.charAt(0).asInstanceOf[Int])
      current
    }
    else {
      var nxt = ss.next(key)
      if (nxt != null) {
        if (!limitChk(nxt))
          nxt = null
        if (nxt != null && (if (isReverse) nxt > key else nxt < key))
          throw new IllegalStateException("current set to before key")
      }
      //      if (nxt!=null && nxt.length==1)
      //        println("!!!!!!!!!!! "+nxt.charAt(0).asInstanceOf[Int]+" - "+limit.charAt(0).asInstanceOf[Int])
      nxt
    }
  }
}
