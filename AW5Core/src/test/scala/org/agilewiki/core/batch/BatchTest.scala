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
package core
package batch

import org.specs.SpecificationWithJUnit
import java.io.File
import java.util.Properties
import kernel.journal.logging._
import util.Timestamp
import kernel.element.{EmbeddedContainerElement, TreeMapElement, DocumentElement}
import rel.{GetObjectLinks, GetRelValue, SubjectUuidsSequence, ObjectUuidsSequence}
import kernel.{KernelNames, TransactionContext, Kernel}

class BatchTest extends SpecificationWithJUnit {
  val userUuid = CoreNames.SYSTEM_USER_UUID
  val logDirectory = "BatchTest"

  val dbname0 = "BatchTest0.aw5db"
  val properties0 = new Properties()
  ConfigCore(properties0)
  DefaultSystemConfiguration(properties0, dbname0, "Master")
  MultiFileLogger(properties0, logDirectory)

  val dbname1 = "BatchTest1.aw5db"
  val properties1 = new Properties()
  ConfigCore(properties1)
  DefaultSystemConfiguration(properties1, dbname1, "Master")

  "BatchTest" should {
    "delete db and log files" in {
      val f0 = new File(dbname0)
      val f1 = new File(dbname1)
      f0.delete
      f1.delete
      MultiFileLogger.emptyDirectory("BatchTest")
      f0.exists must be equalTo (false)
      f1.exists must be equalTo (false)
      Data.clear
    }

    "run an empty batch " in {
      val systemContext = new _Core(properties0)

      val bje = BatchJE(systemContext, Timestamp.timestamp, "empty test", userUuid)
      BatchJE.processTransaction(bje)

      systemContext.close
    }

    "set access on the je " in {
      val systemContext = new _Core(properties0)

      val bje = BatchJE(systemContext, Timestamp.timestamp, "set access on the je", userUuid)
      BatchJE.addRelationship(bje, CoreNames.ACCESS_RELATIONSHIP, CoreNames.ACCESS_UUID, "reader")

      val bytes = bje.jitToBytes
      val bje2 = BatchJE(systemContext, bytes)

      val jeUuid = BatchJE.processTransaction(bje2)

      Kernel(systemContext).startQuery(Timestamp.timestamp)
      try {
        val je = TransactionContext().rolonRootElement(jeUuid)
        val objectUuidsSequence = ObjectUuidsSequence(je, CoreNames.ACCESS_RELATIONSHIP)
        val objUuid = objectUuidsSequence.next(null)
        objUuid must be equalTo (CoreNames.ACCESS_UUID)
      } finally {
        TransactionContext.clear
      }

      systemContext.close
    }

    "create children " in {
      val systemContext = new _Core(properties0)

      val bje = BatchJE(systemContext, Timestamp.timestamp, "create children", userUuid)
      BatchJE.addRelationship(bje, CoreNames.ACCESS_RELATIONSHIP, CoreNames.ACCESS_UUID, "reader")

      val testRolonUuid = BatchJE.create(bje, KernelNames.PAGE_TYPE)
      Data.put("testRolon", testRolonUuid)
      AttributeSpec(bje, testRolonUuid, "a", "b")
      RelationshipSpec.add(bje, testRolonUuid, CoreNames.PARENT_RELATIONSHIP, KernelNames.HOME_UUID, "testRolon")
      RelationshipSpec.add(bje, testRolonUuid, CoreNames.ACCESS_RELATIONSHIP, CoreNames.ACCESS_UUID, "reader")
      DocumentSpec(bje, testRolonUuid, "123".getBytes)

      val testRolon6Uuid = BatchJE.create(bje, KernelNames.PAGE_TYPE)
      Data.put("testRolon6", testRolon6Uuid)
      AttributeSpec(bje, testRolon6Uuid, "a", "b")
      RelationshipSpec.add(bje, testRolon6Uuid, CoreNames.PARENT_RELATIONSHIP, KernelNames.HOME_UUID, "testRolon6")
      DocumentSpec(bje, testRolon6Uuid, "123".getBytes)

      BatchJE.processTransaction(bje)
      systemContext.close
    }

    "delete " in {
      val systemContext = new _Core(properties0)

      val bje = BatchJE(systemContext, Timestamp.timestamp, "delete", userUuid)

      val uuid = Data.get("testRolon6")
      BatchJE.delete(bje, uuid)

      val bytes = bje.jitToBytes
      val bje2 = BatchJE(systemContext, bytes)

      BatchJE.processTransaction(bje2)

      systemContext.close
    }

    "create a grandchild " in {
      val systemContext = new _Core(properties0)

      val bje = BatchJE(systemContext, Timestamp.timestamp, "create a grandchild", userUuid)

      val testRolon2Uuid = BatchJE.create(bje, CoreNames.USER_TYPE)
      RelationshipSpec.add(bje, testRolon2Uuid, CoreNames.PARENT_RELATIONSHIP, CoreNames.USERS_UUID, "testRolon2")

      Data.put("testRolon2", testRolon2Uuid)

      val testRolon3Uuid = BatchJE.create(bje, KernelNames.PAGE_TYPE)
      Data.put("testRolon3", testRolon3Uuid)
      RelationshipSpec.add(bje, testRolon3Uuid, CoreNames.PARENT_RELATIONSHIP, testRolon2Uuid, "testRolon3")

      BatchJE.processTransaction(bje)

      systemContext.close
    }

    "use a new qualifier with serialization " in {
      val systemContext = new _Core(properties0)

      val bje = BatchJE(systemContext, Timestamp.timestamp, "use a new qualifier with serialization", userUuid)

      val testRolon4Uuid = BatchJE.create(bje, CoreNames.USER_TYPE)
      Data.put("testRolon4", testRolon4Uuid)
      RelationshipSpec.add(bje, testRolon4Uuid, CoreNames.PARENT_RELATIONSHIP, CoreNames.USERS_UUID, "testRolon4")

      val testRolon5Uuid = BatchJE.create(bje, CoreNames.USER_TYPE)
      Data.put("testRolon5", testRolon5Uuid)
      RelationshipSpec.add(bje, testRolon5Uuid, CoreNames.PARENT_RELATIONSHIP, CoreNames.USERS_UUID, "testRolon5")

      val bytes = bje.jitToBytes
      val bje2 = BatchJE(systemContext, bytes)
      BatchJE.processTransaction(bje2)

      systemContext.close
    }

    "test adoption " in {
      val systemContext = new _Core(properties0)

      val bje = BatchJE(systemContext, Timestamp.timestamp, "test adoption", userUuid)

      val testRolon2Uuid = Data.get("testRolon2")
      val testRolonUuid = Data.get("testRolon")

      RelationshipSpec.add(bje, testRolonUuid, CoreNames.PARENT_RELATIONSHIP, testRolon2Uuid, "fun")

      BatchJE.processTransaction(bje)

      systemContext.close
    }

    "test abandonment " in {
      val systemContext = new _Core(properties0)

      val bje = BatchJE(systemContext, Timestamp.timestamp, "test adoption", userUuid)

      val testRolon2Uuid = Data.get("testRolon2")
      val testRolonUuid = Data.get("testRolon")

      RelationshipSpec.remove(bje, testRolonUuid, CoreNames.PARENT_RELATIONSHIP, testRolon2Uuid)

      BatchJE.processTransaction(bje)

      systemContext.close
    }

    "test rename " in {
      val systemContext = new _Core(properties0)

      val bje = BatchJE(systemContext, Timestamp.timestamp, "test rename", userUuid)

      val testRolon5Uuid = Data.get("testRolon5")

      RelationshipSpec.update(bje, testRolon5Uuid, CoreNames.PARENT_RELATIONSHIP, CoreNames.USERS_UUID, "testRolon5a")

      BatchJE.processTransaction(bje)
      systemContext.close
    }

    "test reorder " in {
      val systemContext = new _Core(properties0)
      val bje = BatchJE(systemContext, Timestamp.timestamp, "test rename", userUuid)
      ReorderSpec.before(bje, CoreNames.ADMIN_USER_UUID, CoreNames.ACCESS_RELATIONSHIP, CoreNames.ADMIN_USER_UUID, CoreNames.ACCESS_UUID)
      BatchJE.processTransaction(bje)
      systemContext.close
    }

    "recover" in {
      val systemContext = new _CoreRecover(properties1)
      Kernel(systemContext).recoverJnlFiles(logDirectory)

      var testRolonUuid: String = null
      var testRolon2Uuid: String = null
      var testRolon3Uuid: String = null
      var testRolon4Uuid: String = null
      var testRolon5Uuid: String = null
      var testRolon6Exists = false
      var aValue: String = null
      var qValue: String = null
      var doc: String = null
      var testRolon2HasFun = false
      var first = ""
      var second = ""

      Kernel(systemContext).startQuery(Timestamp.timestamp)
      try {
        val ark = TransactionContext().rolonRootElement(KernelNames.HOME_UUID)
        testRolonUuid = SubjectUuidsSequence(ark, CoreNames.PARENT_RELATIONSHIP, "testRolon").next(null)
        val testRolon = TransactionContext().rolonRootElement(testRolonUuid)
        aValue = testRolon.attributes.get("a")

        qValue = GetRelValue(testRolon, CoreNames.ACCESS_RELATIONSHIP, CoreNames.ACCESS_UUID)

        val jeUuid = testRolon.attributes.get("docJE")
        val docA = testRolon.attributes.get("docA")
        val docJE = TransactionContext().rolonRootElement(jeUuid)
        val actions = docJE.contents.get("actions").asInstanceOf[EmbeddedContainerElement]
        val action = actions.contents.get(docA).asInstanceOf[TreeMapElement]
        val document = action.contents.get("document").asInstanceOf[DocumentElement]
        doc = new String(document.getDoc)

        val users = TransactionContext().rolonRootElement(CoreNames.USERS_UUID)
        testRolon2Uuid = SubjectUuidsSequence(users, CoreNames.PARENT_RELATIONSHIP, "testRolon2").next(null)
        val testRolon2 = TransactionContext().rolonRootElement(testRolon2Uuid)
        testRolon3Uuid = SubjectUuidsSequence(testRolon2, CoreNames.PARENT_RELATIONSHIP, "testRolon3").next(null)

        testRolon4Uuid = SubjectUuidsSequence(users, CoreNames.PARENT_RELATIONSHIP, "testRolon4").next(null)
        testRolon5Uuid = SubjectUuidsSequence(users, CoreNames.PARENT_RELATIONSHIP, "testRolon5a").next(null)

        val oldTestRolon6Uuid = Data.get("testRolon6")
        testRolon6Exists = TransactionContext().rolonRootElement(oldTestRolon6Uuid) != null

        val admin = TransactionContext().rolonRootElement(CoreNames.ADMIN_USER_UUID)
        val adminObjectLinks = GetObjectLinks(admin, CoreNames.ACCESS_RELATIONSHIP)
        val it = adminObjectLinks.contents.iterator
        first = it.next
        second = it.next
      } finally {
        TransactionContext.clear
      }

      val oldTestRolonUuid = Data.get("testRolon")
      testRolonUuid must be equalTo (oldTestRolonUuid)
      aValue must be equalTo ("b")
      qValue must be equalTo ("reader")
      doc must be equalTo ("123")

      val oldTestRolon2Uuid = Data.get("testRolon2")
      testRolon2Uuid must be equalTo (oldTestRolon2Uuid)
      val oldTestRolon3Uuid = Data.get("testRolon3")
      testRolon3Uuid must be equalTo (oldTestRolon3Uuid)

      val oldTestRolon4Uuid = Data.get("testRolon4")
      testRolon4Uuid must be equalTo (oldTestRolon4Uuid)
      val oldTestRolon5Uuid = Data.get("testRolon5")
      testRolon5Uuid must be equalTo (oldTestRolon5Uuid)

      testRolon6Exists must be equalTo (false)

      testRolon2HasFun must be equalTo (false)

      first must be equalTo(CoreNames.ADMIN_USER_UUID)
      second must be equalTo(CoreNames.ACCESS_UUID)

      systemContext.close
    }
  }
}