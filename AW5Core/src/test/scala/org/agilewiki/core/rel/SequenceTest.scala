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
package core
package rel

import org.specs.SpecificationWithJUnit
import java.util.Properties
import java.io.File
import util.Timestamp
import kernel.{KernelNames, Kernel, TransactionContext}

class SequenceTest extends SpecificationWithJUnit {
  val dbname0 = "SequenceTest0.aw5db"
  val properties0 = new Properties()
  ConfigCore(properties0)
  DefaultSystemConfiguration(properties0, dbname0, "Master")

  "SequenceTest" should {
    "delete db file" in {
      val f0 = new File(dbname0)
      f0.delete
      f0.exists must be equalTo (false)
    }

    "exercise SubjectTypesSequence" in {
      val systemContext = new _Core(properties0)
      Kernel(systemContext).startQuery(Timestamp.timestamp)
      try {
        val obj = TransactionContext().rolonRootElement(KernelNames.HOME_UUID)
        val subjectTypesSequence = SubjectTypesSequence(obj)
        val typ = subjectTypesSequence.next(null)
        typ must be equalTo (CoreNames.PARENT_RELATIONSHIP)
      } finally {
        TransactionContext.clear
        systemContext.close
      }
    }

    "exercise SubjectKeysSequence" in {
      val systemContext = new _Core(properties0)
      Kernel(systemContext).startQuery(Timestamp.timestamp)
      try {
        val obj = TransactionContext().rolonRootElement(KernelNames.HOME_UUID)
        val subjectKeysSequence = SubjectKeysSequence(obj, CoreNames.PARENT_RELATIONSHIP)
        val key = subjectKeysSequence.next(null)
        key must be equalTo (CoreNames.COMMANDS_NAME + 5.asInstanceOf[Char] + CoreNames.COMMANDS_UUID)
      } finally {
        TransactionContext.clear
        systemContext.close
      }
    }

    "exercise SubjectValuesSequence" in {
      val systemContext = new _Core(properties0)
      Kernel(systemContext).startQuery(Timestamp.timestamp)
      try {
        val obj = TransactionContext().rolonRootElement(KernelNames.HOME_UUID)
        val subjectValuesSequence = SubjectValuesSequence(obj, CoreNames.PARENT_RELATIONSHIP)
        val value = subjectValuesSequence.next(null)
        value must be equalTo (CoreNames.COMMANDS_NAME)
      } finally {
        TransactionContext.clear
        systemContext.close
      }
    }

    "exercise SubjectUuidsSequence" in {
      val systemContext = new _Core(properties0)
      Kernel(systemContext).startQuery(Timestamp.timestamp)
      try {
        val obj = TransactionContext().rolonRootElement(KernelNames.HOME_UUID)
        val subjectUuidsSequence = SubjectUuidsSequence(obj, CoreNames.PARENT_RELATIONSHIP, CoreNames.COMMANDS_NAME)
        val subjUuid = subjectUuidsSequence.next(null)
        subjUuid must be equalTo (CoreNames.COMMANDS_UUID)
      } finally {
        TransactionContext.clear
        systemContext.close
      }
    }

    "exercise ObjectTypesSequence" in {
      val systemContext = new _Core(properties0)
      Kernel(systemContext).startQuery(Timestamp.timestamp)
      try {
        val subj = TransactionContext().rolonRootElement(CoreNames.ACCESS_UUID)
        val objectTypesSequence = ObjectTypesSequence(subj)
        var typ = objectTypesSequence.next(null)
        typ must be equalTo (CoreNames.ACCESS_RELATIONSHIP)
        typ = objectTypesSequence.next(typ)
        typ must be equalTo (CoreNames.PARENT_RELATIONSHIP)
      } finally {
        TransactionContext.clear
        systemContext.close
      }
    }

    "exercise ObjectUuidsSequence" in {
      val systemContext = new _Core(properties0)
      Kernel(systemContext).startQuery(Timestamp.timestamp)
      try {
        val subj = TransactionContext().rolonRootElement(CoreNames.ACCESS_UUID)
        val objectUuidsSequence = ObjectUuidsSequence(subj, CoreNames.PARENT_RELATIONSHIP)
        val objUuid = objectUuidsSequence.next(null)
        objUuid must be equalTo (CoreNames.GROUPS_UUID)
      } finally {
        TransactionContext.clear
        systemContext.close
      }
    }
  }
}
