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
package element

import util._
import org.agilewiki.kernel.component.ContainerComponent
import org.agilewiki.util.sequence.SequenceIterator
import org.agilewiki.util.sequence.composit.SubSequence
import jits.{KernelHandleElement, KernelINodeHandleElement}

private[kernel] abstract class KernelDiskBlockManagerElement
        extends EmbeddedContainerElement
                with BlockManagement {
  protected def _getMore(suggestedSize: Int): (Long, Long)

  def blockType = KERNEL_BLOCK_MANAGEMENT_ELEMENT_ROLE_NAME

  private def pad(v: Long) = {
    val s = "" + v
    "00000000000000000000".substring(s.length()) + s
  }

  private def releaseKey(loc: Long, len: Long) = "R" + pad(loc + len) + "|" + len

  private def releaseIterator = new SequenceIterator(new SubSequence(contents.sequence, "R"))

  private def locationKey(loc: Long, len: Long) = "L" + pad(loc + len) + "|" + len

  private def locationSequence = new SubSequence(contents.sequence, "L")

  private def sizeKey(loc: Long, len: Long) = "S" + pad(len) + "|" + (loc + len)

  private def sizeSequence = new SubSequence(contents.sequence, "S")

  private def parse(key: String): (Long, Long) = {
    val i = key.indexOf("|")
    var len: Long = 0
    var loc: Long = 0
    if (key.startsWith("S"))
      {
        len = java.lang.Long.parseLong(key.substring(1, i))
        loc = java.lang.Long.parseLong(key.substring(i + 1)) - len
      } else {
      len = java.lang.Long.parseLong(key.substring(i + 1))
      loc = java.lang.Long.parseLong(key.substring(1, i)) - len
    }
    (loc, len)
  }

  private[kernel] override def validate(map: DiskMap) {
    super.validate(map)
    var sit = new SequenceIterator(sizeSequence)
    while (sit.hasNext) {
      val key = "S" + sit.next
      val t = parse(key)
      val lkey = locationKey(t._1, t._2)
      if (!contents.contains(lkey)) {
        throw new IllegalStateException("missing key: " + lkey)
      }
    }
    val lit = new SequenceIterator(locationSequence)
    while (lit.hasNext) {
      val key = "L" + lit.next
      val t = parse(key)
      val skey = sizeKey(t._1, t._2)
      if (!contents.contains(skey)) {
        throw new IllegalStateException("missing key: " + skey)
      }
    }
    sit = new SequenceIterator(sizeSequence)
    while (sit.hasNext) {
      val key = "S" + sit.next
      val t = parse(key)
      map._add(t._1, t._2)
    }
    val rit = releaseIterator
    while (rit.hasNext) {
      val key = "R" + rit.next
      val t = parse(key)
      map._add(t._1, t._2)
    }
  }

  private[kernel] def getMore(suggestedSize: Int): (Long, Long) = {
    var t: (Long, Long) = null
    val ss = sizeSequence
    val pk = ss.current(null)
    if (pk != null) {
      val key = "S" + pk
      t = parse(key)
      //      println(key+" "+t+" "+contents.contains(key))
      remove(t._1, t._2)
    } else {
      t = _getMore(suggestedSize)
    }
    //    println("M "+t._1+", "+t._2+", "+(t._1+t._2)+" "+name)
    t
  }

  /**
   * Allocate a block of disk space.
   * @param size The requried size of the block.
   * @return The location of the start of the block.
   */
  def allocate(size: Int): Long = {
    //    contents.printActualContents
    val _size = Fibonacci(size)
    var loc: Long = 0L
    var len: Long = 0L
    val selection = "" + pad(_size)
    val ss = sizeSequence
    val pk = ss.current(selection)
    if (pk != null) {
      val key = "S" + pk
      //      println(name)
      //      println("selection="+selection)
      //      println("key="+key)
      val t = parse(key)
      //      println(key+" = "+t)
      loc = t._1
      len = t._2
      remove(loc, len)
    } else {
      while (len < _size) {
        if (len > 0) {
          _free(loc, len)
        }
        val t = _getMore(_size)
        loc = t._1
        len = t._2
      }
    }
    if (len > _size) {
      _free(loc + _size, len - _size)
    }
    //    println("A "+loc+", "+_size+", "+(loc+_size)+" "+name)
    loc
  }

  /**
   * Identify a block to be made available during the next commit.
   * @param offset The location of the start of the block.
   * @param size The length of the block
   */
  def release(offset: Long, size: Int) {
    //    contents.printActualContents
    val _size = Fibonacci(size)
    //    println("R "+offset+", "+_size+", "+(offset+_size)+" "+name)
    val rlsKey = releaseKey(offset, _size)
    contents.add(rlsKey, blockType)
    //    contents.printActualContents
  }

  /**
   * Make a block available for immediate reuse.
   * @param offset The location of the start of the block.
   * @param size The proposed length of the block
   */
  def free(offset: Long, size: Int) {
    val _size = Fibonacci(size)
    _free(offset, _size)
  }

  /**
   * Make a block available for immediate reuse.
   * @param offset The location of the start of the block.
   * @param size The actual length of the block
   */
  private def _free(offset: Long, size: Long) {
    var loc = offset
    var len = size
    var lKey = locationKey(loc, len)
    val lseq = locationSequence
    if (lseq.next(pad(loc)) == null) {
      add(loc, len)
    } else {
      var k = "L" + lseq.current(null)
      var t = parse(k)
      var loc1 = t._1
      var len1 = t._2
      if (loc1 + len1 == loc) {
        remove(loc1, len1)
        loc = loc1
        len += len1
        val pk = lseq.next(locationKey(loc, len).substring(1))
        if (pk != null) {
          k = "L" + pk
          t = parse(k)
          loc1 = t._1
          len1 = t._2
        } else {
          len1 = 0L
        }
      }
      if (len1 > 0 && loc + len == loc1) {
        remove(loc1, len1)
        len += len1
      }
      add(loc, len)
    }
  }

  private def add(loc: Long, len: Long) {
    val lkey = locationKey(loc, len)
    contents.add(lkey, blockType)
    val skey = sizeKey(loc, len)
    contents.add(skey, blockType)
  }

  private def remove(loc: Long, len: Long) {
    val lkey = locationKey(loc, len)
    contents.delete(lkey)
    val skey = sizeKey(loc, len)
    contents.delete(skey)
  }

  /**
   * Released blocks are made ready for reuse.
   */
  def recycle {
    deserialize
    //    kernelRootElement.validate
    //    println("size: "+contents.size)
    //    val oldSize = contents.size
    //    contents.printActualContents
    val it = releaseIterator
    var i = 0
    while (it.hasNext) {
      i += 1
      val key = "R" + it.next
      //      println("key="+key)
      val t = parse(key)
      _free(t._1, t._2)
      contents.delete(key)
    }
    //    println("count of Release keys: "+i)
    //    println("new size: "+contents.size)
    //    contents.printActualContents
    //    kernelRootElement.validate
  }

  def flushAllDirty {
    while (dirtyCount > 0) {
      flushDirty
    }
  }

  def flushDirty {
    val it = _dirty.iterator
    val blockElement = it.next
    //    println("flush "+blockElement._name+" "+blockElement.getClass.getName)
    it.remove
    //    println(""+hasDirty(blockElement))
    if ((dirtyCount == 0 || !blockElement.isInstanceOf[KernelRootElement])
            && (!blockElement.isInstanceOf[KernelINodeElement] || !hasDirty(blockElement))) {
      //      System.err.println(blockElement.getClass.getName)
      blockElement.persistence.write
    } else {
      //      println("===================================>blip!")
      _dirty.add(blockElement)
    }
  }

  def hasDirty(element: Element): Boolean = {
    var rv = false
    if (element.isInstanceOf[KernelHandleElement]) {
      val handle = element.asInstanceOf[KernelHandleElement]
      if (handle.hasReference) {
        val e = handle.reference
        rv = _dirty.contains(e)
        if (!rv && handle.getBlockSize == 0 && !e.deleted) {
          println(">>>>>>>>>>>>element " + e.getJitName + " " + e.getClass.getName)
          e.printAttributes
          println(">>>>>>>>>>>>handle " + handle.getJitName + " " + handle.getClass.getName)
          throw new IllegalStateException("virgin non-dirty reference found")
        } else {
          //          println("dirty reference found")
        }
      } else if (handle.isInstanceOf[KernelINodeHandleElement] && handle.empty) {
        throw new IllegalStateException("empty handle found!!!")
      }
    } else if (element.isInstanceOf[ContainerComponent]) {
      val contents = element.asInstanceOf[ContainerComponent].contents
      val it = contents._iteratorActual
      while (!rv && it.hasNext) {
        val key = it.next
        if (!contents.isJitDeserialized(key)) rv = false
        else {
          val e = contents._getActual(key)
          if (!e.isInstanceOf[EmbeddedElement]) rv =  false
          else rv = hasDirty(e.asInstanceOf[EmbeddedElement])
        }
      }
    }
    rv
  }

}
