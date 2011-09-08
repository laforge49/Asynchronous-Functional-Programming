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
package incDes
package blocks

import blip._
import services._
import scala.ref._

class BlockCacheComponentFactory extends ComponentFactory {
  override def instantiate(actor: Actor) = new BlockCacheComponent(actor)
}

class BlockCacheComponent(actor: Actor)
  extends Component(actor) {
  private val referenceQueue = new ReferenceQueue[Block]
  private val hashMap = new java.util.HashMap[Any, SoftBlockReference]
  private val linkedHashSet = new java.util.LinkedHashSet[Block]
  lazy val maxSize = GetProperty.int("maxBlockCacheSize", 1000)
  bind(classOf[BlockCacheClear], clear)
  bind(classOf[BlockCacheRemove], remove)
  bind(classOf[BlockCacheAdd], add)
  bind(classOf[BlockCacheGet], get)

  def clear(msg: AnyRef, rf: Any => Unit) {
    linkedHashSet.clear
    rf(null)
  }

  def remove(msg: AnyRef, rf: Any => Unit) {
    val key = msg.asInstanceOf[BlockCacheRemove].key
    val block = hashMap.remove(key)
    linkedHashSet.remove(block)
    rf(null)
  }

  def accessed(block: Block) {
    val newItem = !linkedHashSet.remove(block)
    linkedHashSet.add(block)
    if (newItem && linkedHashSet.size > maxSize) {
      val it = linkedHashSet.iterator
      it.next
      it.remove
    }
  }

  def add(msg: AnyRef, rf: Any => Unit) {
    val block = msg.asInstanceOf[BlockCacheAdd].block
    accessed(block)
    val key = block.key
    var nwr = hashMap.get(key)
    if (nwr != null) nwr.get match {
      case Some(blk) => {
        if (blk != block) throw new IllegalArgumentException("duplicate key")
        rf(null)
      }
      case None =>
    }
    nwr = new SoftBlockReference(block, referenceQueue)
    hashMap.put(key, nwr)
    var more = true
    while (more) {
      referenceQueue.poll match {
        case Some(ref) => hashMap.remove(ref.asInstanceOf[SoftBlockReference].key)
        case None => more = false
      }
    }
    rf(null)
  }

  def get(msg: AnyRef, rf: Any => Unit) {
    val key = msg.asInstanceOf[BlockCacheGet].key
    val nwr = hashMap.get(key)
    if (nwr == null) {
      rf(null)
      return
    }
    nwr.get match {
      case Some(block) => {
        accessed(block)
        rf(block)
      }
      case None => {
        hashMap.remove(key)
        rf(null)
      }
    }
  }
}
