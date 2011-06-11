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
package component

import java.util.LinkedList
import java.util.Deque

import org.agilewiki.kernel.element.Element
import util.Configuration
import util.sequence.composit.HeadSequence
import util.sequence.SequenceSource
import util.jit._
import util.jit.structure.JitElement
import jits.INodeLinkElement

private[kernel] trait _BTreeContainerComponent extends ContainerComponent {
  this: Element =>

  var _size: JitInt = null

  private var _leaf: JitBoolean = null

  override def contents: _BTreeContainer = {
    _contents.asInstanceOf[_BTreeContainer]
  }

  def leaf = {
    if (_leaf == null) {
      _leaf = JitBoolean.createJit(systemContext)
      _leaf.setBoolean(true)
    }
    _leaf
  }

  override def builder = {
    var l = 0
    if (!isINode) {
      _size = JitInt.createJit(systemContext)
      _size.partness(this, null, this)
      l += _size.jitByteLength
    }
    leaf.partness(this, null, this)
    l += leaf.jitByteLength
    super.builder + l
  }

  override protected def serializeJit(cursor: JitMutableCursor) {
    super.serializeJit(cursor)
    leaf.jitToBytes(cursor)
    if (!isINode) _size.jitToBytes(cursor)
  }

  override def elementLoader(cursor: JitMutableCursor) {
    super.elementLoader(cursor)
    leaf.loadJit(cursor)
    if (!isINode) _size.loadJit(cursor)
  }

  protected def isINode: Boolean

  private def setEmbeddedElementsContainer(jitNamedJitTreeMap: JitNamedJitTreeMap) {
    contents.containerWrapper.setJit(jitNamedJitTreeMap)
  }

  private[kernel] abstract class _BTreeContainer extends Container {

    private val LIMIT = "" + Character.MAX_VALUE

    var _jitContainerWrapper: JitWrapper = _

    protected def isLeaf = {
      deserialize
      leaf.getBoolean
    }

    private[_BTreeContainerComponent] def setLeaf(value: Boolean) {
      deserialize
      leaf.setBoolean(value)
    }

    protected def createTerminatorElement: JitElement = Jits(systemContext).createJit(EMPTY_JIT_ELEMENT_ROLE_NAME).
      asInstanceOf[JitElement]

    protected def createEmbeddedElementsContainer(leaf: Boolean): JitNamedJitTreeMap =
      JitNamedVariableJitTreeMap.createJit(systemContext)

    override def builder = {
      _jitContainerWrapper = JitWrapper.createJit(systemContext)
      if (!isJitSerialized)
        _jitContainerWrapper.setJit(createEmbeddedElementsContainer(leaf.getBoolean))
      _jitContainerWrapper.partness(_BTreeContainerComponent.this, null, _BTreeContainerComponent.this)
      _jitContainerWrapper.jitByteLength
    }

    override def containerWrapper = {
      deserialize
      _jitContainerWrapper
    }

    override def embeddedElementsContainer = containerWrapper.getJit.asInstanceOf[JitNamedJitTreeMap]

    protected def maxNodeSize = {
      var propertyName: String = null
      if (isINode) {
        if (isLeaf) {
          propertyName = MAX_BTREE_LEAF_SIZE_PARAMETER
        } else {
          propertyName = MAX_BTREE_INODE_SIZE_PARAMETER
        }
      } else {
        propertyName = MAX_BTREE_ROOT_SIZE_PARAMETER
      }
      Configuration(systemContext).requiredIntProperty(propertyName)
    }

    override def size = {
      if (isINode) {
        throw new UnsupportedOperationException("Not supported for INodes.")
      }
      deserialize
      _size.getInt
    }

    private def _incSize(inc: Int) {
      if (isINode) {
        throw new UnsupportedOperationException("Not supported for INodes.")
      }
      deserialize
      _size.setInt(size + inc)
    }

    private def validateKey(key: String) {
      if (key == null || key.length == 0 || key >= LIMIT) {
        System.err.println("key = " + key)
        throw new IllegalArgumentException
      }
    }

    private def inode(name: String): _BTreeContainerComponent = {
      var rv: _BTreeContainerComponent = null
      var key = embeddedElementsContainer.ceiling(name)
      if (key == null) {
        key = embeddedElementsContainer.last
      }
      val referenceElement = _getActual(key).asInstanceOf[Reference]
      rv = referenceElement.resolve.asInstanceOf[_BTreeContainerComponent]
      if (rv == null) {
        System.err.println("btree inode method, name = " + name + ", key = " + key)
        System.err.println("this = " + _BTreeContainerComponent.this)
        throw new IllegalStateException
      }
      rv
    }

    override def contains(name: String): Boolean = {
      validateKey(name)
      var rv: Boolean = false
      if (isLeaf) {
        rv = _containsActual(name)
      } else {
        val _blockElement = inode(name)
        if (_blockElement != null) {
          rv = _blockElement.contents.contains(name)
        }
      }
      rv
    }

    override def get(name: String): JitElement = {
      validateKey(name)
      var rv: JitElement = null
      if (isLeaf) {
        rv = _getActual(name)
        if (rv.isInstanceOf[INodeLinkElement]) {
          if (!isINode) System.err.println(">>> "+size)
          throw new IllegalStateException("unexpected INode")
        }
      } else {
        val _blockElement = inode(name)
        if (_blockElement != null) {
          rv = _blockElement.contents.get(name)
        }
      }
      rv
    }

    override def sequence: SequenceSource = sequence(false)

    override def sequence(reverse: Boolean): SequenceSource = {

      var rv: SequenceSource = null

      if (isLeaf) {
        if (reverse) {
          rv = _sequenceActual(true)
          rv.next(LIMIT)
        } else {
          rv = new HeadSequence(_sequenceActual, LIMIT)
        }
      } else {
        rv = new SequenceSource {
          var lastKey: String = null
          init

          def init {
            var _blockElement: _BTreeContainerComponent = null
            var lastReferenceKey =
              if (reverse) embeddedElementsContainer.last else embeddedElementsContainer.first
            val referenceElement = _getActual(lastReferenceKey).asInstanceOf[Reference]
            _blockElement = referenceElement.resolve.asInstanceOf[_BTreeContainerComponent]
            var ss: SequenceSource = _blockElement.contents.sequence(reverse)

            while (_blockElement != null && ss.current == null) {
              lastReferenceKey =
                if (reverse) embeddedElementsContainer.lower(lastReferenceKey)
                else embeddedElementsContainer.higher(lastReferenceKey)
              if (lastReferenceKey == null) {
                _blockElement = null
                ss = null
              } else {
                val referenceElement = _getActual(lastReferenceKey).asInstanceOf[Reference]
                _blockElement = referenceElement.resolve.asInstanceOf[_BTreeContainerComponent]
                ss = _blockElement.contents.sequence(reverse)
              }
            }
            if (_blockElement != null) lastKey = ss.current
          }

          override def isReverse = reverse

          override def current = super.current

          override def current(_key: String): String = {
            val key = if (_key == null) lastKey else _key
            var rss: String = null
            if (key != null) {
              var lastReferenceKey: String = null
              if (reverse) {
                lastReferenceKey = embeddedElementsContainer.floor(key)
              } else {
                lastReferenceKey = embeddedElementsContainer.ceiling(key)
              }
              if (lastReferenceKey == null) lastReferenceKey =
                if (reverse) embeddedElementsContainer.first
                else embeddedElementsContainer.last
              val referenceElement = _getActual(lastReferenceKey).asInstanceOf[Reference]
              var _blockElement = referenceElement.resolve.asInstanceOf[_BTreeContainerComponent]
              var ss = _blockElement.contents.sequence(reverse)
              rss = ss.current(key)
              while (_blockElement != null && rss == null) {
                val oldRefKey = lastReferenceKey
                lastReferenceKey =
                  if (reverse) embeddedElementsContainer.lower(oldRefKey)
                  else embeddedElementsContainer.higher(oldRefKey)
                if (lastReferenceKey == null) {
                  _blockElement = null
                  ss = null
                } else {
                  val referenceElement = _getActual(lastReferenceKey).asInstanceOf[Reference]
                  _blockElement = referenceElement.resolve.asInstanceOf[_BTreeContainerComponent]
                  ss = _blockElement.contents.sequence(reverse)
                  rss = ss.current(key)
                }
              }
              if (_blockElement != null) {
                /*
                val nrss = ss.current
                if (rss != nrss) {
                  println("rss "+rss)
                  println("nrss "+nrss)
                  println("lastReferenceKey "+lastReferenceKey)
                  println("_key "+_key)
                  println("lastKey "+lastKey)
                  printActualContents
                  throw new IllegalStateException("btree corruption!")
                }
                */
                if (key != null) {
                  if ((reverse && rss > key) || (!reverse && rss < key)) {
                    throw new IllegalStateException("Current key set before key")
                  }
                }
              }
              lastKey = rss
            }
            rss
          }


          override def next(key: String): String = {
            if (key == null) current(key) else _next(key)
          }

          def _next(key: String): String = {
            var lastReferenceKey =
              if (reverse) embeddedElementsContainer.lower(key) else embeddedElementsContainer.higher(key)
            if (lastReferenceKey == null) {
              lastReferenceKey =
                if (reverse) embeddedElementsContainer.first else embeddedElementsContainer.last
            }
            val referenceElement = _getActual(lastReferenceKey).asInstanceOf[Reference]
            var _blockElement = referenceElement.resolve.asInstanceOf[_BTreeContainerComponent]
            var ss = _blockElement.contents.sequence(reverse)
            while (_blockElement != null && ss.next(key) == null) {
              lastReferenceKey =
                if (reverse) embeddedElementsContainer.lower(lastReferenceKey)
                else embeddedElementsContainer.higher(lastReferenceKey)
              if (lastReferenceKey == null) {
                _blockElement = null
                ss = null
              } else {
                val referenceElement = _getActual(lastReferenceKey).asInstanceOf[Reference]
                _blockElement = referenceElement.resolve.asInstanceOf[_BTreeContainerComponent]
                ss = _blockElement.contents.sequence(reverse)
              }
            }
            var rss: String = null
            if (_blockElement != null) {
              rss = ss.current
              if ((reverse && rss >= key) || (!reverse && rss <= key)) {
                println(key + " - " + rss)
                throw new IllegalStateException("next not set to after the key")
              }
            }
            lastKey = rss
            rss
          }
        }
      }
      rv
    }

    private[_BTreeContainer] def _removeActual(
                                                stack: Deque[_BTreeContainerComponent],
                                                nstack: Deque[String],
                                                name: String): JitElement = {
      var rv: JitElement = null
      if (embeddedElementsContainer.contains(name)) {
        writeLock
        rv = embeddedElementsContainer.remove(name).asInstanceOf[JitElement]
        if (embeddedElementsContainer.isEmpty) {
          val btc = stack.removeFirst
          val rnm = nstack.removeFirst
          val reference = btc.contents._removeActual(stack, nstack, rnm)
          reference._delete
        }
      }
      rv
    }

    private[_BTreeContainer] def _remove(
                                          stack: Deque[_BTreeContainerComponent],
                                          nstack: Deque[String],
                                          name: String): JitElement = {
      var rv: JitElement = null
      if (isLeaf) {
        rv = _removeActual(stack, nstack, name)
      } else {
        var referent: _BTreeContainerComponent = null
        var key = embeddedElementsContainer.ceiling(name)
        if (key == null) {
          key = embeddedElementsContainer.last
        }
        val reference = _getActual(key).asInstanceOf[Reference]
        referent = reference.resolve.asInstanceOf[_BTreeContainerComponent]
        val referentContents = referent.contents
        stack.addFirst(_BTreeContainerComponent.this)
        nstack.addFirst(key)
        rv = referentContents._remove(stack, nstack, name)
      }
      rv
    }

    override private[kernel] def remove(name: String): JitElement = {
      if (!contains(name)) return null
      writeLock
      validateKey(name)
      val rv = _remove(
        new LinkedList[_BTreeContainerComponent],
        new LinkedList[String],
        name)
      if (rv == null) return null
      _incSize(-1)
      rv
    }

    protected def createReferenceElement: Reference

    protected def initializeReferenceElement(re: Reference)

    private[_BTreeContainerComponent] def splitRoot {
      //System.err.println("split: "+_BTreeContainerComponent.this.getClass.getName)
      val tm1 = createEmbeddedElementsContainer(isLeaf)
      while (tm1.jitByteLength + 1 < embeddedElementsContainer.jitByteLength && embeddedElementsContainer.size > 1) {
        val key = embeddedElementsContainer.first
        val fe = embeddedElementsContainer.removeWrapper(key);
        tm1.putWrapper(key, fe);
      }
      val tm2 = embeddedElementsContainer
      setEmbeddedElementsContainer(createEmbeddedElementsContainer(false))
      val oldLeaf = isLeaf
      setLeaf(false)
      val ref1 = createReferenceElement
      val name1 = tm1.last
      embeddedElementsContainer.put(name1, ref1.asInstanceOf[JitElement])
      initializeReferenceElement(ref1)
      val node1 = ref1.resolve.asInstanceOf[_BTreeContainerComponent]
      val node1Contents = node1.contents
      node1Contents.setLeaf(oldLeaf)
      node1.setEmbeddedElementsContainer(tm1)
      val ref2 = createReferenceElement
      val name2 = tm2.last
      embeddedElementsContainer.put(name2, ref2.asInstanceOf[JitElement])
      initializeReferenceElement(ref2)
      val node2 = ref2.resolve.asInstanceOf[_BTreeContainerComponent]
      val node2Contents = node2.contents
      node2Contents.setLeaf(oldLeaf)
      node2.setEmbeddedElementsContainer(tm2)
    }

    private[_BTreeContainerComponent] def splitINode(
                                                      stack: Deque[_BTreeContainerComponent],
                                                      nstack: Deque[String]) {
      //System.err.println("inode split"+_BTreeContainerComponent.this.getClass.getName)
      val parentNode = stack.removeFirst
      val nodeName = nstack.removeFirst
      val newNodeName = embeddedElementsContainer.last
      if (nodeName < newNodeName) {
        val tm = parentNode.contents.embeddedElementsContainer
        if (nodeName != tm.last) {
          throw new IllegalStateException("corrupt split")
        }
        val v = tm.remove(nodeName).asInstanceOf[JitElement]
        tm.put(newNodeName, v)
      }
      val tm1 = createEmbeddedElementsContainer(isLeaf)
      while (tm1.jitByteLength + 1 < embeddedElementsContainer.jitByteLength && embeddedElementsContainer.size > 1) {
        val key = embeddedElementsContainer.first
        val fe = embeddedElementsContainer.removeWrapper(key)
        tm1.putWrapper(key, fe)
      }
      val ref1 = createReferenceElement
      val name1 = tm1.last
      parentNode.contents.puts(stack, nstack, name1, ref1.asInstanceOf[JitElement])
      initializeReferenceElement(ref1)
      val node1 = ref1.resolve.asInstanceOf[_BTreeContainerComponent]
      val node1Contents = node1.contents
      node1Contents.setLeaf(isLeaf)
      node1.setEmbeddedElementsContainer(tm1)
    }

    private[_BTreeContainer] def puts(
                                       stack: Deque[_BTreeContainerComponent],
                                       nstack: Deque[String],
                                       name: String,
                                       embeddedElement: JitElement) {
      writeLock
      embeddedElementsContainer.put(name, embeddedElement)
      if (embeddedElementsContainer.size > 1 && embeddedElementsContainer.jitByteLength > maxNodeSize) {
        if (isINode) {
          splitINode(stack, nstack)
        } else {
          splitRoot
        }
      }
    }

    private[_BTreeContainer] def _put(
                                       stack: Deque[_BTreeContainerComponent],
                                       nstack: Deque[String],
                                       name: String,
                                       embeddedElement: JitElement) {
      if (isLeaf) {
        puts(stack, nstack, name, embeddedElement)
      } else {
        var referent: _BTreeContainerComponent = null
        var key = embeddedElementsContainer.ceiling(name)
        if (key == null) {
          key = embeddedElementsContainer.last
        }
        val reference = _getActual(key).asInstanceOf[Reference]
        referent = reference.resolve.asInstanceOf[_BTreeContainerComponent]
        val referentContents = referent.contents
        stack.addFirst(_BTreeContainerComponent.this)
        nstack.addFirst(key)
        referentContents._put(stack, nstack, name, embeddedElement)
      }
    }

    override protected def put(
                                name: String,
                                embeddedElement: JitElement) {
      validatePut(name, embeddedElement)
      validateKey(name)
      if (_sizeActual == 0) {
        val terminatorElement = createTerminatorElement
        embeddedElementsContainer.put(LIMIT, terminatorElement)
      }
      _put(
        new LinkedList[_BTreeContainerComponent],
        new LinkedList[String],
        name,
        embeddedElement)
      _incSize(1)
    }

    protected def iNodeType: String
  }

}
