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

import java.util.TreeMap
import org.agilewiki.kernel.element.Element

/**
 * Provides access to and functioanlity based around name/value pair attributes
 *
 * @author Bill La Forge
 */
trait AttributesComponent {
  this: Element =>

  protected val _attributes = defineAttributes

  protected def defineAttributes = {new Attributes}

  def attributes = {_attributes}

  /**
   * Supports a number of get and put methods for retrieving and assigning
   * attribute values in the form of name/value pairs.
   */
  class Attributes {

    protected def atts = AttributesComponent.this.jitAttributes

    def copyAtts = {
      val rv = new TreeMap[String, String]
      val it = atts.jitIterator
      while(it.hasNext) {
        val n = it.next
        val v = atts.getString(n)
        rv.put(n, v)
      }
      rv
    }

    /**
     * Get the value of the attribute with the given name.
     * @param attributeName the name of the attribute.
     * @return the value of the attribute, or null if the attribute does not exist.
     */
    def get(attributeName: String) = {
      _get(attributeName)
    }

    /**
     * Implements get without calling persistence.accessed.
     * @param attributeName the name of the attribute.
     * @return the value of the attribute, or null if the attribute does not exist.
     */
    private[kernel] def _get(attributeName: String) = {
      atts.getString(attributeName)
    }

    /**
     * Set the value of the attribute with the given name. When the value is null
     * , the attribute is removed.
     * @param attributeName the name of the attribute.
     * @param attributeValue the value of the attribute.
     */
    def put(attributeName: String, attributeValue: String) {
      if (attributeValue == null) {
        if (atts.contains(attributeName)) {
          persistence.writeLock
          atts.remove(attributeName)
        }
      } else {
        if (!atts.contains(attributeName) || attributeValue != atts.getString(attributeName)) {
          persistence.writeLock
          atts.putString(attributeName, attributeValue)
        }
      }
    }

    /**
     * Updates an attribute without writeLock or markDirty.
     * @param attributeName the name of the attribute.
     * @param attributeValue the value of the attribute.
     */
    protected[kernel] def _put(attributeName: String, attributeValue: String) {
      if (attributeValue == null) {
        if (atts.contains(attributeName)) {
          atts.removeString(attributeName)
        }
      } else {
        if (!atts.contains(attributeName) || attributeValue != atts.getString(attributeName)) {
          atts.putString(attributeName, attributeValue)
        }
      }
    }

    /**
     * Get the value of the attribute with the given name as a Boolean.
     * @param attributeName The name of the attribute.
     * @param defaultValue The default Boolean value to return if the attribute does not exist.
     * @return The value of the attribute, or the default value if the attribute does not exist.
     */
    def getBoolean(attributeName: String, defaultValue: Boolean) = {
      val value = get(attributeName)
      var rv = defaultValue
      if (value != null) {
        rv = value.toBoolean
      }
      rv
    }

    /**
     * Get the value of the attribute with the given name as a Boolean.
     * @param attributeName The name of the attribute.
     * @param defaultValue The default Boolean value to return if the attribute does not exist.
     * @return The value of the attribute, or the default value if the attribute does not exist.
     */
    private[kernel] def _getBoolean(attributeName: String, defaultValue: Boolean) = {
      val value = _get(attributeName)
      var rv = defaultValue
      if (value != null) {
        rv = value.toBoolean
      }
      rv
    }

    /**
     * Set the value of the attribute with the given name as a Boolean.
     * @param attributeName The name of the attribute.
     * @param value The value of the attribute.
     * @param defaultValue When equal to value the attribute is removed.
     */
    def putBoolean(attributeName: String, value: Boolean, defaultValue: Boolean) {
      if (value == defaultValue) {
        put(attributeName, null)
      } else {
        put(attributeName, "" + value)
      }
    }

    /**
     * Set the value of the attribute with the given name as an integer.
     * @param attributeName The name of the attribute.
     * @param value The value of the attribute.
     * @param defaultValue When equal to value the attribute is removed.
     */
    private[kernel] def _putBoolean(attributeName: String, value: Boolean, defaultValue: Boolean) {
      if (value == defaultValue) {
        _put(attributeName, null)
      } else {
        _put(attributeName, "" + value)
      }
    }

    /**
     * Get the value of the attribute with the given name as an integer.
     * @param attributeName the name of the attribute.
     * @param defaultValue the default integer value to return if the attribute does not exist.
     * @return the value of the attribute, or the default value if the attribute does not exist.
     */
    def getInt(attributeName: String, defaultValue: Int) = {
      val value = get(attributeName)
      var rv = defaultValue
      if (value != null) {
        rv = value.toInt
      }
      rv
    }

    /**
     * Get the value of the attribute with the given name as an integer.
     * @param attributeName the name of the attribute.
     * @param defaultValue the default integer value to return if the attribute does not exist.
     * @return the value of the attribute, or the default value if the attribute does not exist.
     */
    private[kernel] def _getInt(attributeName: String, defaultValue: Int) = {
      val value = _get(attributeName)
      var rv = defaultValue
      if (value != null) {
        rv = value.toInt
      }
      rv
    }

    /**
     * Set the value of the attribute with the given name as an integer.
     * @param attributeName the name of the attribute.
     * @param value the value of the attribute.
     * @param defaultValue when equal to value the attribute is removed.
     */
    def putInt(attributeName: String, value: Int, defaultValue: Int) {
      if (value == defaultValue) {
        put(attributeName, null)
      } else {
        put(attributeName, "" + value)
      }
    }

    /**
     * Set the value of the attribute with the given name as an integer.
     * @param attributeName the name of the attribute.
     * @param value the value of the attribute.
     * @param defaultValue when equal to value the attribute is removed.
     */
    private[kernel] def _putInt(attributeName: String, value: Int, defaultValue: Int) {
      if (value == defaultValue) {
        _put(attributeName, null)
      } else {
        _put(attributeName, "" + value)
      }
    }

    /**
     * Get the value of the attribute with the given name as a long.
     * @param attributeName the name of the attribute.
     * @param defaultValue the default long value to return if the attribute does not exist.
     * @return the value of the attribute, or the default value if the attribute does not exist.
     */
    def getLong(attributeName: String, defaultValue: Long) = {
      val value = get(attributeName)
      var rv = defaultValue
      if (value != null) {
        rv = value.toLong
      }
      rv
    }

    /**
     * Set the value of the attribute with the given name as a long.
     * @param attributeName the name of the attribute.
     * @param value the value of the attribute.
     * @param defaultValue when equal to value the attribute is removed.
     */
    def putLong(attributeName: String, value: Long, defaultValue: Long) {
      if (value == defaultValue) {
        put(attributeName, null)
      } else {
        put(attributeName, "" + value)
      }
    }

    /**
     * Get an iterator for all attributes.
     * @return an iterator.
     */
    def iterator = atts.jitIterator

    /**
     * Get the number of attributes.
     * @return a count of the number of attributes.
     */
    def size = {
      atts.size
    }
  }
}
