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

import element.operation.SystemElementsComponent
import journal.logging.{RecoveringJournalEntryFactory, JournalEntryLogger}
import util._
import jit.Jits
import org.agilewiki.kernel.element.KernelRootElement
import org.agilewiki.kernel.element.RolonRootElement
import org.agilewiki.kernel.operation.Eval
import org.agilewiki.kernel.operation.Roles
import java.io.{File, EOFException, DataInputStream, FileInputStream}
import java.util.{UUID, TreeSet, Properties}
import java.util.concurrent.Semaphore

class _Kernel(configurationProperties: Properties)
  extends SystemComposite
  with SystemShutdownComponent
  with SystemConfigurationComponent
  with SystemElementsComponent
  with SystemKernelComponent {
  setProperties(configurationProperties)
  kernel.start

  override def close {
    kernel.close
  }

  override def initializeServer {
    kernel._initializeDb
  }
}

class _KernelRecover(configurationProperties: Properties)
  extends SystemComposite
  with SystemShutdownComponent
  with SystemConfigurationComponent
  with SystemElementsComponent
  with SystemKernelComponent {
  setProperties(configurationProperties)
  kernel.recover

  override def close {
    kernel.close
  }
}

object Kernel {
  def apply(context: SystemComposite) = context.asInstanceOf[SystemKernelComponent].kernel
}

trait SystemKernelComponent {
  this: SystemComposite
    with SystemShutdownComponent
    with SystemConfigurationComponent
    with SystemElementsComponent =>

  protected lazy val _kernel = defineKernel

  protected def defineKernel = new Kernel

  def kernel = _kernel

  class Kernel {
    var oldStartingTime: String = null
    private[kernel] var startingTime: String = _
    private[kernel] var kernelINodeCache: KernelINodeCache = _
    private var _roles: Roles = _
    private[kernel] var queryCache: QueryCache = _
    private var _kernelRootElement: KernelRootElement = _
    private[kernel] var jeLogger: JournalEntryLogger = _

    private var _recover = false

    def recover {
      _recover = true
      start
    }

    def start {
      kernelINodeCache = new KernelINodeCache(configuration.requiredIntProperty(MAX_KERNEL_INODE_CACHE_SIZE_PARAMETER))
      _roles = new Roles(SystemKernelComponent.this)
      queryCache = new QueryCache(configuration.requiredIntProperty(MAX_QUERY_CACHE_SIZE_PARAMETER))
      kernelINodeCache.clear
      _kernelRootElement = Jits(SystemKernelComponent.this).createJit(KERNEL_ROOT_ELEMENT_ROLE_NAME).asInstanceOf[KernelRootElement]
      if (_kernelRootElement.startup) restart
      else if (!_recover) {
        SystemKernelComponent.this.initializeServer
      }
    }

    def _initializeDb {
      processTransaction(new InitializationJEF)
    }

    private def Log(je: RolonRootElement) {
      if (jeLogger == null) return
      jeLogger.log(je)
    }

    private def restart {
      //println("restart")
      var block0Bad = false
      var block1Bad = false
      val persistence = _kernelRootElement.persistence
      val t0 = persistence.readRootBlock0
      val _cursor0 = t0._1
      val ts0 = t0._2
      if (_cursor0 == null) {
        //println("bad block 0")
        block0Bad = true
      }
      val t1 = persistence.readRootBlock1
      val _cursor1 = t1._1
      val ts1 = t1._2
      if (_cursor1 == null) {
        //println("bad block 1")
        block1Bad = true
      }
      var writeBlock0 = true
      if (block0Bad) {
        writeBlock0 = true
      } else if (block1Bad) {
        writeBlock0 = false
      } else if (ts0 < ts1) {
        writeBlock0 = true
      } else {
        writeBlock0 = false
      }
      persistence.writeBlock0 = writeBlock0
      val cursor =
        if (writeBlock0) _cursor1 else _cursor0
      //println("do the load")
      _kernelRootElement.loadJit(cursor)
      _kernelRootElement.partness(null, "RootElement", null)
    }

    /**
     * Returns the contexts for the transaction by accessing a thread variable.
     * @return The transction contexts.
     */
    def transactionContexts = TransactionContext()

    private[kernel] def kernelRootElement: KernelRootElement = _kernelRootElement

    private[kernel] def databasePathname = {
      configuration.requiredProperty(DATABASE_PATHNAME)
    }

    /**
     * The extent of a database file is the amount of space that has been allocated,
     * though not necessiarily currently in use.
     * The file extent is typically a bit larger than the file size.
     * @return The total extent of all the database files used by the kernel.
     */
    def totalExtent = {
      kernelRootElement.extent
    }

    /**
     * Each database file contains two copies of the root element's data at the
     * beginning of the file. This data must not exceed a maximum size.
     * Root block usage then is the percentage of space used. This is an important number
     * which should be monitored, as the Kernel will fail if a root block becomes
     * too large.
     * @return The largest root block usage of all the database files used by
     * the kernel.
     */
    def maxRootBlockUsage = {
      val size = kernelRootElement.maxRootBlockSize
      var len = kernelRootElement.rootBlockLength0
      val len1 = kernelRootElement.rootBlockLength1
      if (len1 > len) {
        len = len1
      }
      (len * 100) / size
    }

    /**
     * KernelDiskBlockManager1Element is fully embedded within the root element, so if the number
     * of sub-elements becomes too large, the root block may become too large and the kernel will fail.
     * @return The total number of sub-elements of KernelDiskBlockManager1Element for all database files.
     */
    def totalDiskBlockManager1Size = {
      kernelRootElement.diskBlockManager1Size
    }

    /**
     * The space used by the INodes which hold the contents of KernelDiskBlockManager2Element is managed
     * by KernelDiskBlockManager1, so as the number of INodes grows the number of sub-elements of
     * KernelDiskBlockManager1 also grows.
     * @return The total number of sub-elements of KernelDiskBlockManager2Element for all database files.
     */
    def totalDiskBlockManager2Size = {
      kernelRootElement.diskBlockManager2Size
    }

    /**
     * The space used by the INodes which hold the contents of KernelDiskBlockManager3Element is managed
     * by KernelDiskBlockManager2, so as the number of INodes grows the number of sub-elements of
     * KernelDiskBlockManager2 also grows.
     * @return The total number of sub-elements of KernelDiskBlockManager3Element for all database files.
     */
    def totalDiskBlockManager3Size = {
      kernelRootElement.diskBlockManager3Size
    }

    /**
     * The space used by the INodes which hold the contents of KernelAddressMapElement is managed
     * by KernelDiskBlockManager3, so as the number of INodes grows the number of sub-elements of
     * KernelDiskBlockManager3 also grows.
     * @return The total number of sub-elements of KernelAddressMapElement for all database files.
     */
    def totalAddressMapSize = {
      kernelRootElement.addressMapSize
    }

    /**
     * The space used by the INodes which hold the contents of KernelTimestampsElement is managed
     * by KernelDiskBlockManager3, so as the number of INodes grows the number of sub-elements of
     * KernelDiskBlockManager3 also grows.
     * @return The total number of sub-elements of KernelTimestampsElement for all database files.
     */
    def totalTimestampsSize = {
      kernelRootElement.timestampsSize
    }

    /**
     * When a transaction completes without throwing any exceptions, the changes are committed to the database files where the
     * transaction was active.
     * @return The largest number of commits for all database files used by the kernel.
     */
    def maxCommits = {
      kernelRootElement.commits
    }

    /**
     * Prints the Kernel Statistics report.
     */
    def printKernelStatistics {
      println
      println("Kernel Statistics")
      println
      println("Total Extent for all Files: " + totalExtent)
      println("Maximum Root Block Usage: " + maxRootBlockUsage + "%")
      println("Total DiskBlockManager1 Size: " + totalDiskBlockManager1Size)
      println("Total DiskBlockManager2 Size: " + totalDiskBlockManager2Size)
      println("Total DiskBlockManager3 Size: " + totalDiskBlockManager3Size)
      println("Total AddressMap Size: " + totalAddressMapSize)
      println("Total Timestamps Size: " + totalTimestampsSize)
      println("Maximum Commits: " + maxCommits)
      println
    }

    /**
     * Closes the kernel's database files without printing any statistics.
     */
    def close {
      try {
        close(false)
      } catch {
        case ex: Throwable => {}
      }
    }

    /**
     * Closes the kernel's database files.
     * @param report If true, the Kernel Statistics report is printed.
     */
    def close(report: Boolean) {
      if (report) {
        printKernelStatistics
      }
      try {
        kernelRootElement.close
        if (jeLogger != null) {
          jeLogger.close
          jeLogger = null
        }
      } catch {
        case ex: Exception => {}
      }
    }

    private def newTransactionContexts = {
      new TransactionContext(SystemKernelComponent.this)
    }

    def startQuery: String = {
      startQuery(Timestamp.timestamp)
    }

    def startQuery(selectedTime: String): String = {
      var st = selectedTime
      val transactionContexts = newTransactionContexts
      transactionContexts.query = true
      TransactionContext.set(transactionContexts)
      transactionContexts.setWriteOnly("")
      var cst: String = Timestamp.timestamp
      synchronized{
        if (startingTime != null) cst = startingTime
      }
      if (st > cst) {
        st = cst
      }
      val i = st.indexOf("_")
      if (i > -1) st = st.substring(0, i)
      transactionContexts.selectedTime(st)
      st
    }

    def recoverJnlFiles(dirName: String) {
      val dir = new File(dirName)
      val list = dir.list
      val set = new TreeSet[String]
      var i = 0
      while (i < list.size) {
        val fileName = list(i)
        if (!fileName.endsWith(".jnl")) println("ignoring " + fileName)
        else {
          val jf = new File(dir, fileName)
          if (jf.length == 0) println("ignoring zero length file: " + fileName)
          else set.add(fileName)
        }
        i += 1
      }
      while (set.size > 0) {
        val fileName = set.first
        set.remove(fileName)
        println("recovering " + fileName)
        recover(dirName + File.separator + fileName)
        println("... finished " + fileName)
      }
      println("completed recovery")
    }

    private val transactionLock = new Semaphore(1, true)

    def recover(journalFileName: String) {
      transactionLock.acquire
      try {
        kernelRootElement.deserialize
        val transactionContexts = newTransactionContexts
        TransactionContext.set(transactionContexts)
        kernelRootElement.recycle
        val systemContext = SystemKernelComponent.this
        val reader = new DataInputStream(new FileInputStream(journalFileName))
        try {
          var journalEntryFactory = new RecoveringJournalEntryFactory(systemContext, reader)
          while (true) {
            processJournalEntry(journalEntryFactory)
            journalEntryFactory = new RecoveringJournalEntryFactory(systemContext, reader)
            kernelRootElement.diskBlockManager3.flushAllDirty
          }
        } catch {
          case ex: EOFException => {}
          case x: Throwable => throw x
        }
        completeTransaction
      } finally {
        TransactionContext.clear
        transactionLock.release
      }
    }

    def processTransaction(journalEntryFactory: JournalEntryFactory) = {
      transactionLock.acquire
      try {
        kernelRootElement.deserialize
        val transactionContexts = newTransactionContexts
        TransactionContext.set(transactionContexts)
        kernelRootElement.recycle
        val rv = processJournalEntry(journalEntryFactory)
        completeTransaction
        rv
      } finally {
        TransactionContext.clear
        transactionLock.release
      }
    }

    private var journalEntryFactory: JournalEntryFactory = null
    private var _transactionContext: TransactionContext = null

    def transactionPhase1(journalEntryFactory: JournalEntryFactory) {
      transactionLock.acquire
      try {
        kernelRootElement.deserialize
        this.journalEntryFactory = journalEntryFactory
        _transactionContext = newTransactionContexts
        TransactionContext.set(_transactionContext)
        _transactionContext.setWriteOnly("")
        startingTime = journalEntryFactory.assignStartingTime
        _transactionContext.setStartingTime(startingTime)
        journalEntryFactory.preValidate
        TransactionContext.clear
      } catch {
        case ex: Throwable => {
          val actionName = TransactionContext().actionName
          TransactionContext.clear
          transactionPhase1Abort
          if (actionName == null || ex.isInstanceOf[PreValidateException]) throw ex
          else throw new ActionException(actionName, ex)
        }
      }
    }

    def transactionPhase1Abort {
      startingTime = null
      _transactionContext = null
      transactionLock.release
    }

    def transactionPhase2: String = {
      try {
        TransactionContext.set(_transactionContext)
        kernelRootElement.recycle
        _transactionContext.setWriteOnly(Timestamp.invert(startingTime))
        val journalEntry = journalEntryFactory.journalEntry
        _transactionContext.setWriteOnly(null)
        Eval(journalEntry)
        TransactionContext.clear
        journalEntry.uuid
      } catch {
        case ex: Throwable => {
          val actionName = TransactionContext().actionName
          TransactionContext.clear
          transactionPhase2Abort
          if (actionName == null) throw ex
          else {
            System.err.println("action = " + actionName)
            throw new ActionException(actionName, ex)
          }
        }
      }
    }

    def transactionPhase2Abort {
      startingTime = null
      _transactionContext = null
      start
      transactionLock.release
    }

    def transactionPhase3 {
      try {
        TransactionContext.set(_transactionContext)
        val journalEntry = _transactionContext._currentJournalEntryRoot
        Log(journalEntry)
        completeTransaction
        TransactionContext.clear
      } finally {
        transactionLock.release
      }
    }

    def processJournalEntry(journalEntryFactory: JournalEntryFactory) = {
      try {
        transactionContexts.setWriteOnly("")
        startingTime = journalEntryFactory.assignStartingTime
        transactionContexts.setStartingTime(startingTime)
        journalEntryFactory.preValidate
        transactionContexts.setWriteOnly(Timestamp.invert(startingTime))
        val journalEntry = journalEntryFactory.journalEntry
        transactionContexts.setWriteOnly(null)
        val value = Eval(journalEntry)
        Log(journalEntry)
        TransactionResult(startingTime, value, journalEntry.uuid)
      } catch {
        case unknown: Exception => {
          unknown.printStackTrace
          println("abort!")
          transactionContexts.setWriteOnly(null)
          startingTime = null
          start
          val actionName = TransactionContext().actionName
          if (actionName == null) throw unknown
          else throw new ActionException(actionName, unknown)
        }
      }
    }

    def completeTransaction {
      transactionContexts.setWriteOnly(null)
      transactionContexts.commit
      transactionContexts.setWriteOnly("")
      startingTime = null
    }

    def roles = _roles

    def role(name: String) = roles.role(name)

    def arkManager(roleName: String) = {
      val arole = role(roleName)
      var am: String = null
      if (arole == null) am = roleName
      else am = arole.getArkManager
      am
    }

    def generateUuid(roleName: String) = UUID.randomUUID.toString + "_" + roleName
  }

}