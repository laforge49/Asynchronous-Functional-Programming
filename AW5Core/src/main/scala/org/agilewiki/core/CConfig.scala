/*
 * Copyright 2010 Alex K.
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
package core

import java.util.Properties

import kernel.operation.Config

class CConfig(properties: Properties) extends Config(properties) {
  private var propertySetItems = Map[String, Int]()
  private val rconfig = new RelationshipConfig(properties)

  def subj(relationType: String, subRoleName: String) {
    property("subj." + relationType)(subRoleName)
  }

  def obj(relationType: String, objRoleName: String) {
    property("obj." + relationType)(objRoleName)
  }

  /**
   * Add a property set to the current role being configured. If exists return it's index otherwise
   * add new property set at the end and return it's index.
   * @param propertySetName The name of the property set being added.
   * @param requiredPropertyName The required property name of the property set being added.
   * @return Index of the property set.
   */
  def nextRolePropertySetIndex(propertySetName: String, requiredPropertyName: String): Int = {
    val indexedRoleName = "role" + roleIndx
    var itemIndex = if (associatedItems contains indexedRoleName) associatedItems(indexedRoleName) else 0
    for (j <- 1 to itemIndex) {
      if (propertySetName.equals(properties.get(indexedRoleName + "." + j + ".propertySetName"))) {
        var propSetIndex = propertySetItems.get(indexedRoleName + "." + j) match {case Some(x) => x; case None => 0}
        for (k <- 1 to propSetIndex) {
          if (requiredPropertyName.equals(properties.get(indexedRoleName + "." + j + ".requiredPropertyName")))
            return j
        }
      }
    }
    addNewPropertySetIndex(propertySetName, requiredPropertyName)
  }

  /**
   * Add an property to the current role being configured.
   * @param propertySetName The name of the property set.
   * @param itemIndex The index of the property set being added.
   * @param propSetIndex The index of the property name being added.
   * @param propertyName The name of the property being added.
   * @param propertyValue The value of the property being added. The default value is "".
   */
  def property(propertySetName: String, itemIndex: Int, propertyName: String)(implicit propertyValue: String) = {
    val indexedRoleName = "role" + roleIndx
    val propSetIndex = (propertySetItems.get(indexedRoleName + "." + itemIndex) match {case Some(x) => x; case None => 0}) + 1
    properties.put(indexedRoleName + "." + itemIndex + ".propertySetName", propertySetName)
    properties.put(indexedRoleName + "." + itemIndex + "." + propSetIndex + ".propertyName", propertyName)
    properties.put(indexedRoleName + "." + itemIndex + "." + propSetIndex + ".propertyValue", propertyValue)
    propertySetItems += (indexedRoleName + "." + itemIndex -> propSetIndex)
  }

  implicit val propertyValue = ""

  /**
   * Add value to the propertySetName "relations" for the relation role being configured.
   * @param subjectPathname The value of the required perperty for property set "relations".
   * @param objectPathname The value of the second required property "objectPathname".
   */
  private def _relation(subjectPathname: String, objectPathname: String) = {
    val itemIndex = nextRolePropertySetIndex("relations", "subjectPathname")
    property("relations", itemIndex, "subjectPathname")(subjectPathname)
    property("relations", itemIndex, "objectPathname")(objectPathname)
  }

  /**
   * Add value to the propertySetName "relationship" for the current role being configured.
   * @param subjectPathname The value of the required perperty for property set "relationship".
   * @param objectPathname The value of the second required property "objectPathname".
   */
  private def _relationship(relationChildName: String, objectPathname: String) = {
    val itemIndex = nextRolePropertySetIndex("relationship", "relationChildName")
    property("relationship", itemIndex, "relationChildName")(relationChildName)
    property("relationship", itemIndex, "objectPathname")(objectPathname)
  }

  /**
   * Add value to the propertySetName "relationship" for the current role being configured.
   * @param subjectPathname The value of the required perperty for property set "inverseRelationship".
   * @param objectPathname The value of the second required property "subjectPathname".
   */
  private def _inverseRelationship(relationChildName: String, subjectPathname: String) = {
    val itemIndex = nextRolePropertySetIndex("inverseRelationship", "relationChildName")
    property("inverseRelationship", itemIndex, "relationChildName")(relationChildName)
    property("inverseRelationship", itemIndex, "subjectPathname")(subjectPathname)
  }

  /**
   * Add a new property set to the current role being configured.
   * add new property set at the end and return it's index.
   * @param propertySetName The name of the property set being added.
   * @param requiredPropertyName The required property name of the property set being added.
   * @return Index of the property set.
   */
  def addNewPropertySetIndex(propertySetName: String, requiredPropertyName: String): Int = {
    val indexedRoleName = "role" + roleIndx
    var itemIndex = if (associatedItems contains indexedRoleName) associatedItems(indexedRoleName) else 0
    itemIndex += 1
    //val propSetIndx = 1
    properties.put(indexedRoleName + "." + itemIndex + ".propertySetName", propertySetName)
    properties.put(indexedRoleName + "." + itemIndex + ".requiredPropertyName", requiredPropertyName)
    associatedItems += (indexedRoleName -> itemIndex)
    itemIndex
  }

  def rel(relationshipType: String, subj: String, obj: String, value: String) {
    rconfig(relationshipType, subj, subj, obj, obj, value)
  }
}
