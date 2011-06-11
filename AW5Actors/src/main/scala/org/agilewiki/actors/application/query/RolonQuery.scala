/*
 * Copyright 2010 M.Naji
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
package actors
package application
package query

import util.sequence.SequenceIterator
import kernel.element._
import kernel.{Document, TransactionContext}
import util.RolonName
import core.rel.{GetRelValue, GetObjects, GetObjectLinks}

trait RolonQuery {
  protected def processQuery(rolonUuid: String, timestamp: String, message: RepliableMessage) {
    message.reply(queryResponse(rolonUuid, timestamp))
  }

  protected def queryResponse(rolonUuid: String, timestamp: String) = {
    val rolon = TransactionContext().rolonRootElement(rolonUuid)
    if (rolon == null) {
      RolonDoesNotExist(rolonUuid, timestamp)
    } else {
      RolonResponse(RolonName(rolon.rolonUuid),
        getAttributes(rolon),
        getRelationships(rolon),
        getEffectedRolons(rolon),
        getDocument(rolon))
    }
  }

  private def getDocument(rolon: RolonRootElement): Array[Byte] = {
    val docElement = rolon.contents.get("document").asInstanceOf[Document]
    if (docElement == null) return new Array[Byte](0)
    docElement.getDoc
  }

  private def getAttributes(rolon: RolonRootElement) = {
    var attributes = Map[String, String]()
    val it = rolon.attributes.iterator
    while (it.hasNext) {
      val name = it.next
      val value = rolon.attributes.get(name)
      attributes += (name -> value)
    }
    attributes
  }

  private def getRelationships(rolon: RolonRootElement) = {
    val objects = GetObjects(rolon)
    var relationships = Map[String, List[ObjectIdentifier]]()
    if (objects != null) {
      val rit = objects.contents.iterator
      while (rit.hasNext) {
        val relType = rit.next
        var objectIdentifiers = List[ObjectIdentifier]()
        val objectLinks = GetObjectLinks(rolon, relType)
        val it = objectLinks.contents.iterator
        while (it.hasNext) {
          val objUuid = it.next
          val value = GetRelValue(rolon, relType, objUuid)
          objectIdentifiers :+= ObjectIdentifier(objUuid, value)
        }
        relationships += (relType -> objectIdentifiers)
      }
    }
    relationships
  }

  private def getEffectedRolons(rolon: RolonRootElement): Map[String, Map[String, Map[String, String]]] = {
    var effected = Map[String, Map[String, Map[String, String]]]()
    val effectedRolons = rolon.contents.get("effectedRolons")
      .asInstanceOf[KernelEffectedRolonsElement]
    if (effectedRolons != null) {
      val seq = effectedRolons.contents.sequence(false)
      val it = SequenceIterator(seq)
      while (it.hasNext) {
        val uuid = it.next
        val effectedElement = effectedRolons.contents.get(uuid).asInstanceOf[KernelEffectedElement]
        /*
        val cseq = effectedElement.contents.sequence
        val cit = SequenceIterator(cseq)
        */
        var eles = Map[String, Map[String, String]]()
        /*
        while (cit.hasNext) {
          val cnbr = cit.next
          val changeElement = effectedElement.contents.get(cnbr).asInstanceOf[KernelChangeElement]
          var attributes = Map[String, String]()
          val ait = changeElement.attributes.iterator
          while (ait.hasNext) {
            val name = ait.next
            attributes += (name -> changeElement.attributes.get(name))
          }
          eles += (cnbr -> attributes)
        }
        */
        effected += (uuid -> eles)
      }
    }
    effected
  }
}