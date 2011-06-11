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

import java.util.Iterator
import util._
import jit._
import element._
import sequence._
import util.sequence.composit.SubSequence
import structure.JitElement
import jits.KernelINodeHandleElement

/**
 * Defines a common API for containers, and also implements a null-container.
 * @author Bill La Forge
 */
trait ContainerComponent extends ContentsComponent {
  this: Element =>

  override def contents: Container = {
    _contents.asInstanceOf[Container]
  }

  override def builder = {
    super.builder + contents.builder
  }

  override protected def serializeJit(cursor: JitMutableCursor) {
    super.serializeJit(cursor)
    contents.containerWrapper.jitToBytes(cursor)
  }

  override def elementLoader(cursor: JitMutableCursor) {
    super.elementLoader(cursor)
    contents.containerWrapper.loadJit(cursor)
  }

  /**
   * Defines a common API for containers, and also implements a null container.
   */
  abstract class Container extends Contents {

    def containerWrapper: Jit

    def builder: Int

    protected def embeddedElementsContainer: JitNamedJitTreeMap

    /**
     * Get the number of logical sub-elements in the container.
     * The read lock is also set.
     * @return The number of logical sub-elements.
     */
    def size: Int = _sizeActual

    /**
     * Get the number of sub-elements actually embedded in the container which,
     * for a null-container, is always 0.
     * @return 0, as a null container is always empty.
     */
    private[kernel] def _sizeActual: Int = embeddedElementsContainer.size

    /**
     * Checks to see if there is a logical sub-element for the given name.
     * The read lock is also set.
     * @param name The name of a logical sub-element.
     * @return True if there is a logical sub-element by the given name.
     */
    def contains(name: String) = _containsActual(name)

    /**
     * Checks to see if there is a sub-element actually embedded in the container.
     * @param name The name of an actual sub-element.
     * @return False, as a null container is always empty.
     */
    protected def _containsActual(name: String) = embeddedElementsContainer.contains(name)

    /**
     * Fetches a logical sub-element by the given name. 
     * The read lock is also set.
     * @param name The name of a logical sub-element.
     * @return The logical sub-element, or null.
     */
    def get(name: String): JitElement = _getActual(name)

    /**
     * Fetches a sub-element which is actually embedded in the container.
     * @param name The name of an element actually embedded in the container.
     * @return Null, as a null container is always empty.
     */
    private[kernel] def _getActual(name: String): JitElement =
      embeddedElementsContainer.get(name).asInstanceOf[JitElement]

    def isJitDeserialized(name: String) = {
      if (containerWrapper.isJitDeserialized) embeddedElementsContainer.isJitDeserialized(name)
      else false
    }

    /**
     * Returns the sub-element named "name". This method creates the element
     * with the type elementType if it doesn't exist.
     * @param name The name of the sub-element
     * @param elementType The type of the element to be created.
     * @return the element
     */
    def make(name: String, elementType: String) = {
      var elt: JitElement = get(name)
      if (elt == null) elt = add(name, elementType)
      elt
    }

    /**
     * Adds a new logical sub-element to the container.
     * The element is first initialized and then put is called.
     * @param name The name of the new logical sub-element.
     * @param elementType The type of element to be created.
     */
    def add(name: String, elementType: String): JitElement = {
      val embeddedElement = Jits(systemContext).createJit(elementType).
        asInstanceOf[JitElement]
      put(name, embeddedElement)
      embeddedElement
    }

    def validatePut(name: String, embeddedElement: JitElement) {
      if (contains(name)) {
        throw new IllegalArgumentException("already present: " + name)
      }
      if (embeddedElement == null) {
        throw new IllegalArgumentException("null not allowed")
      }
      if (deleted) {
        throw new UnsupportedOperationException("deleted")
      }
      writeLock
    }

    /**
     * Adds an initialized or loaded logical sub-element to the container.
     * A write lock is set on the container, _put is called and then the container element
     * is marked as dirty.
     * @param name The name of the logical sub-element.
     * @param embeddedElement The element to be added as a logical sub-element.
     * @throws IllegalArgumentException If a logical sub-element by the same name is already present
     * or if the embedded element is null.
     */
    protected def put(name: String, embeddedElement: JitElement) {
      _putActual(name, embeddedElement)
    }

    /**
     * Adds an initialized or loaded element as an actual embedded element in the container.
     * @param name The name of the element actually being embedded in the contaienr.
     * @param embeddedElement The element actually being embedded in the container.
     * @throws UnsupportedOperationException, as a null container can hold no elements.
     */
    protected def _putActual(name: String, embeddedElement: JitElement) {
      validatePut(name, embeddedElement)
      embeddedElementsContainer.put(name, embeddedElement)
    }

    def rename(oldName: String, newName: String) {
      if (contains(newName)) throw new UnsupportedOperationException
      val e: JitElement = remove(oldName)
      if (e == null) throw new UnsupportedOperationException
      put(newName, e)
    }

    /**
     * Removes a logical sub-element from the container.
     * If the container has a logical sub-element by the given name, then
     * a write lock is set on the container, 
     * _remove is called and the contaienr is marked dirty.
     * @param name The name of the logical sub-element.
     * @return The sub-element that was removed, or null.
     */
    private[kernel] def remove(name: String): JitElement = _removeActual(name)

    /**
     * Removes a sub-element which is actually embedded in the container
     * @param name The name of the actual sub-element embedded in the container.
     * @return Null, as a null container never has any content.
     */
    protected def _removeActual(name: String): JitElement = {
      var rv: JitElement = null
      if (_containsActual(name)) {
        writeLock
        rv = embeddedElementsContainer.remove(name).asInstanceOf[JitElement]
      }
      rv
    }

    /**
     * Delete a logical sub-element.
     * Calls remove(name and then calls _delete on the removed element.
     * @param name The name of the logical sub-element.
     */
    def delete(name: String) {
      val embeddedElement = remove(name)
      if (embeddedElement != null) {
        embeddedElement._delete
      }
    }

    /**
     * Delete an actual sub-element.
     * Calls remove(name and then calls _delete on the removed element.
     * @param name The name of the logical sub-element.
     */
    protected def _deleteActual(name: String) {
      val embeddedElement = _removeActual(name)
      if (embeddedElement != null) {
        embeddedElement._delete
      }
    }

    /**
     * Deletes all sub-elements.
     */
    private[kernel] def deleteAll {
      writeLock
      val it = _iteratorActual
      while (it.hasNext) {
        val name = it.next
        _deleteActual(name)
      }
    }

    /**
     * Returns a String iterator over the names of the logical sub-elements.
     * The iterator is implemented over a SequenceSource, so (1) remove is not supported and
     * (2) the contents of the container are allowed to change without conflict with the iterator.
     * A read lock is set and then _iterator is called.
     * @return A String Iterator over the names of the logical sub-elements.
     */
    final def iterator: Iterator[String] = iterator(false)

    /**
     * Returns a String iterator over the names of the logical sub-elements.
     * The iterator is implemented over a SequenceSource, so (1) remove is not supported and
     * (2) the contents of the container are allowed to change without conflict with the iterator.
     * A read lock is set and then _iterator is called.
     * @param reverse Identifies the direction of the sequence.
     * @return A String Iterator over the names of the logical sub-elements.
     */
    final def iterator(reverse: Boolean): Iterator[String] = new SequenceIterator(sequence(reverse))

    /**
     * Returns a String iterator over the names of the actual sub-elements embedded in the container.
     * @return A SequenceIterator over the names of the actual sub-elements embedded in the container.
     */
    final private[kernel] def _iteratorActual: Iterator[String] = _iteratorActual(false)

    /**
     * Returns a String iterator over the names of the actual sub-elements embedded in the container.
     * @param reverse Identifies the direction of the sequence.
     * @return A SequenceIterator over the names of the actual sub-elements embedded in the container.
     */
    final private[kernel] def _iteratorActual(reverse: Boolean): Iterator[String] =
      new SequenceIterator(_sequenceActual(reverse))

    /**
     * Returns a SubSequence of the logical sub-element names.
     * @param prefix The prefix used to select the logical sub-element names.
     * @return A SubSequence over a range of names of the logical sub-elements. 
     * Prefixes are removed from the names by the SubSequence.peek method.
     */
    def subSequence(prefix: String): SequenceSource = subSequence(prefix, false)

    /**
     * Returns a SubSequence of the logical sub-element names.
     * @param prefix The prefix used to select the logical sub-element names.
     * @param reverse Identifies the direction of the sequence.
     * @return A SubSequence over a range of names of the logical sub-elements. 
     * Prefixes are removed from the names by the SubSequence.peek method.
     */
    def subSequence(prefix: String, reverse: Boolean): SequenceSource = {
      new SubSequence(sequence(reverse), prefix)
    }

    /**
     * Returns a SequenceSource over the logical sub-element names.
     * This method sets the read lock and then calls _sequence
     * @return A SequenceSource over the logical sub-element names.
     */
    def sequence: SequenceSource = sequence(false)

    /**
     * Returns a SequenceSource over the logical sub-element names.
     * This method sets the read lock and then calls _sequence
     * @param reverse Identifies the direction of the sequence.
     * @return A SequenceSource over the logical sub-element names.
     */
    def sequence(reverse: Boolean): SequenceSource = _sequenceActual(reverse)

    /**
     * Returns a SequenceSource over the actual sub-elements embedded in the container.
     * @return An EmptySequence, as a null container is always empty.
     */
    protected def _sequenceActual: SequenceSource = _sequenceActual(false)

    /**
     * Returns a SequenceSource over the actual sub-elements embedded in the container.
     * @param reverse Identifies the direction of the sequence.
     * @return An EmptySequence, as a null container is always empty.
     */
    protected def _sequenceActual(reverse: Boolean) = embeddedElementsContainer.jitSequence(reverse)

    /**
     * Prints the contents of an element.
     */
    def printContents {
      println("" + size + " sub-elements:")
      val it = iterator
      while (it.hasNext) {
        val name = it.next
        val value = get(name)
        println(name + " = " + value)
      }
    }

    /**
     * Prints the actual contents of an element.
     */
    def printActualContents {
      printActualContents(0)
    }

    /**
     * Prints the actual contents of an element.
     */
    def printActualContents(depth: Int) {
      if (_sizeActual == 0) {
        println("Depth=" + depth + ", name=" + getJitName + ", actual size=" + _sizeActual)
      } else {
        println("Depth=" + depth + ", name=" + getJitName + ", actual size=" + _sizeActual + ", sub-elements:")
      }
      var it = _iteratorActual
      while (it.hasNext) {
        val name = it.next
        val value = _getActual(name)
        if (value.isInstanceOf[KernelINodeHandleElement]) {
          val handle = value.asInstanceOf[Reference]
          val inode = handle.resolve
          println("key: " + name)
          inode.contents.printActualContents(depth + 1)
        } else {
          println(name + " = " + value)
        }
      }
    }
  }

}
