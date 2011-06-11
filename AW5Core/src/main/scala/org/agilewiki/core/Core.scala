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

import rel.SetRelValue
import util._
import java.util.Properties
import util.SystemConfigurationComponent
import kernel.element.operation.SystemElementsComponent
import kernel.{SystemKernelComponent, TransactionContext}

class _Core(configurationProperties: Properties)
  extends SystemComposite
  with SystemShutdownComponent
  with SystemConfigurationComponent
  with SystemElementsComponent
  with SystemKernelComponent
  with SystemCoreComponent {
  setProperties(configurationProperties)
  kernel.start

  override def close {
    kernel.close
  }

  override def initializeServer {
    core._initializeDb
  }
}

class _CoreRecover(configurationProperties: Properties)
  extends SystemComposite
  with SystemShutdownComponent
  with SystemConfigurationComponent
  with SystemElementsComponent
  with SystemKernelComponent
  with SystemCoreComponent {
  setProperties(configurationProperties)
  kernel.recover

  def close {
    kernel.close
  }
}

object Core {
  def apply(context: SystemComposite) = context.asInstanceOf[SystemCoreComponent].core
}

trait SystemCoreComponent {
  this: SystemComposite
    with SystemShutdownComponent
    with SystemConfigurationComponent
    with SystemElementsComponent
    with SystemKernelComponent =>

  protected lazy val _core = defineCore

  protected def defineCore = new Core

  def core = _core

  class Core {

    def _initializeDb {
      kernel.processTransaction(new CoreInitializationJEF)
    }

    def initializeDatabase(transactionContext: TransactionContext) {
      processRelConfiguration(transactionContext)
      val je = transactionContext.currentJournalEntryRoot
      val ark = transactionContext.ark
      SetRelValue(je, CoreNames.ACCESS_RELATIONSHIP, CoreNames.ACCESS_UUID, "reader")
    }

    protected def processRelConfiguration(transactionContext: TransactionContext) {
      var ndx = 1
      def relTypeKey = "rel." + ndx + ".relTyp"
      def relType = configuration.property(relTypeKey)
      def objUuidKey = "rel." + ndx + ".objUuid"
      def objUuid = configuration.property(objUuidKey)
      def objRoleName = configuration.property("rel." + ndx + ".objRole")
      def subjUuid = configuration.property("rel." + ndx + ".subjUuid")
      def subjRoleName = configuration.property("rel." + ndx + ".subjRole")
      def relValue = configuration.property("rel." + ndx + ".value")
      def nxt {
        ndx += 1
      }

      while (configuration contains objUuidKey) {
        val subjRole = kernel.role(subjRoleName)
        if (subjRole == null) throw new IllegalArgumentException("undefined role: " + subjRoleName)
        if (subjRole.getArkManager == null)
          throw new IllegalStateException("The role '" + subjRole.roleName + "' does not have an associated ark manager")
        val objRole = kernel.role(objRoleName)
        if (objRole == null) throw new IllegalArgumentException("undefined role: " + objRoleName)
        if (objRole.getArkManager == null)
          throw new IllegalStateException("The role '" + objRole.roleName + "' does not have an associated ark manager")
        if (subjRole.getArkManager == configuration.localServerName) {
          val subj = transactionContext.makeRolonRootElement(subjUuid + "_" + subjRoleName)
          SetRelValue(subj, relType, objUuid + "_" + objRoleName, relValue)
        }
        if (relType == CoreNames.PARENT_RELATIONSHIP && objRole.getArkManager == configuration.localServerName) {
          val obj = TransactionContext().makeRolonRootElement(objUuid + "_" + objRoleName)
          val count = obj.attributes.getInt("childCount", 0)
          obj.attributes.putInt("childCount", count+1, 0)
        }
        nxt
      }
    }
  }

}