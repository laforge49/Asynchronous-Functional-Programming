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

import org.specs.SpecificationWithJUnit

import java.util.Properties
import util.UtilNames
;
import java.io.File

import org.agilewiki.kernel.element.RolonRootElement
import org.agilewiki.kernel.operation.Eval
import org.agilewiki.kernel.operation.Config

/**
 * Unit tests for the Kernel root element functionality
 *
 * @author Bill La Forge
 */

class AddFactory extends JournalEntryFactory {
  override def journalEntryType = "addJE"
}

class AddEval extends Eval {
  override def process(
          roleName: String,
          rolonContext: RolonRootElement,
          targetRolon: RolonRootElement) {
    val transactionContexts = targetRolon.transactionContexts
    println("add transaction")
    transactionContexts.printJournalEntries

    val rolon = transactionContexts.createRolonRootElement("123_" + UtilNames.PAGE_TYPE)
    val be = transactionContexts.rolonRootElement("123_" + UtilNames.PAGE_TYPE)
    be.attributes.putInt("t0", -1, 0)
    transactionContexts.validate
    be.printRolonUuid
    be.printAttributes
    //    be.printContents
    println()
    //    transactionContexts.printElementClassStatistics
  }
}

class UpdateFactory extends JournalEntryFactory {
  override def journalEntryType = "updateJE"
}

class UpdateEval extends Eval {
  override def process(
          roleName: String,
          rolonContext: RolonRootElement,
          targetRolon: RolonRootElement) {
    val transactionContexts = targetRolon.transactionContexts
    println("update transaction")
    //    transactionContexts.printElementClassStatistics
    //    transactionContexts.printJournalEntries
    val r = transactionContexts.rolonRootElement("123_" + UtilNames.PAGE_TYPE)
    val be = r
    be.attributes.putInt("t1", 1, 0)
    /*    transactionContexts.validate
    be.printRolonUuid
    be.printAttributes
    be.printContents
    transactionContexts.printJournalEntries("123")
    transactionContexts.printEffectedRolons(transactionContexts.startingTime)
    println()
    transactionContexts.printElementClassStatistics
    */
  }
}

class DeleteFactory extends JournalEntryFactory {
  override def journalEntryType = "deleteJE"
}

class DeleteEval extends Eval {
  override def process(
          roleName: String,
          rolonContext: RolonRootElement,
          targetRolon: RolonRootElement) {
    val transactionContexts = targetRolon.transactionContexts
    println("delete transaction1")
    //    transactionContexts.printJournalEntries
    transactionContexts.delete("123_" + UtilNames.PAGE_TYPE)
    /*    transactionContexts.validate
    transactionContexts.printEffectedRolons(transactionContexts.startingTime)
    println()
    transactionContexts.printElementClassStatistics
    */
  }
}

class NoopFactory extends JournalEntryFactory {
  override def journalEntryType = "noopJE"
}

class NoopEval extends Eval {
  override def process(
          roleName: String,
          rolonContext: RolonRootElement,
          targetRolon: RolonRootElement) {
    val transactionContexts = targetRolon.transactionContexts
    println("noop transaction1")
    transactionContexts.validate
    transactionContexts.printJournalEntries
    /*
    transactionContexts.printEffectedRolons(transactionContexts.startingTime)
    println()
    transactionContexts.printElementClassStatistics
    transactionContexts.printAttributesForAllVersions("123")
    */
  }
}

class KernelRootElementTest extends SpecificationWithJUnit {
  new File("KernelRootElementTest.aw5db").delete

  val properties = new Properties()
  DefaultSystemConfiguration(properties, "KernelRootElementTest.aw5db", "Master")

  ConfigKernel(properties)

  println("open1")
  //var kernel = new Kernel(properties)

  val config = new Config(properties)

  config role "addJE"
  config rootElementType ROLON_ROOT_ELEMENT_ROLE_NAME
  config include KernelNames.CHANGE_TYPE
  config op new AddEval

  config role "updateJE"
  config rootElementType ROLON_ROOT_ELEMENT_ROLE_NAME
  config include KernelNames.CHANGE_TYPE
  config op new UpdateEval

  config role "deleteJE"
  config rootElementType ROLON_ROOT_ELEMENT_ROLE_NAME
  config include KernelNames.CHANGE_TYPE
  config op new DeleteEval

  config role "noopJE"
  config rootElementType ROLON_ROOT_ELEMENT_ROLE_NAME
  config include KernelNames.CHANGE_TYPE
  config op new NoopEval

  var systemContext = new _Kernel(properties)

  Kernel(systemContext).processTransaction(new AddFactory)
  Kernel(systemContext).processTransaction(new NoopFactory)
  println("close1")
  Kernel(systemContext).close(false)

  println("open2")
  systemContext = new _Kernel(properties)
  Kernel(systemContext).processTransaction(new NoopFactory)
  Kernel(systemContext).processTransaction(new UpdateFactory)
  Kernel(systemContext).processTransaction(new NoopFactory)
  Kernel(systemContext).processTransaction(new DeleteFactory)
  //  kernel.processTransaction(new NoopFactory)
  println()
  println("close2")
  Kernel(systemContext).close(false)
  println()

  /*
  println()
  println("open3")
  println()
  kernel = new Kernel(properties)
  kernel.processTransaction(new NoopFactory)
  println("close3")
  kernel.close(false)
  */
}
