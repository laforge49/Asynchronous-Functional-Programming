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

import element.operation.{ElementRole, Initialize}
import jits.KernelBlockHandleElement
import util._
import element._
import java.util.UUID
import jit.Jits
import jit.structure.JitElement
import util.cache._
import util.{Configuration, Timestamp}
import util.sequence.composit.{AppendSequence, SynchronizedSequence, InvertedTimestampSequence, FilteredSequence}
import util.sequence.{SequenceIterator, SequenceSource}
import util.sequence.basic.EmptySequence

object TransactionContext {
  private val _context = new ThreadLocal[TransactionContext]
  clear

  def apply() = _context.get

  def clear {
    _context.set(null)
  }

  def isEmpty = this() == null

  def set(context: TransactionContext) {
    if (!isEmpty) throw new UnsupportedOperationException("Context already present")
    _context.set(context)
  }
}

/**
 * TransactionContext provides a transacton access to kernel services. 
 * The Kernel.transactionContexts method, as well as the
 * Element.transactionContexts method provide convenient access to this variable.
 * <p>
 * An instance of TransactionContexts is creaated at the start of every transaction and
 * is deleted on completion of the transaction, successful or otherwise. 
 */
case class TransactionContext(systemContext: SystemComposite) {
  private[kernel] var _currentJournalEntryRoot: RolonRootElement = null

  /**
   * This variable is used for time navigation.
   * Once set to a past time, all subsequent accesses provide the state of the rolons
   * at that time.
   * Note however that when navigating time, updates are not allowed.
   */
  var _selectedTime = Timestamp.CURRENT_TIME

  var actionName: String = _

  private val kernelRootElement = Kernel(systemContext).kernelRootElement

  private[kernel] var capture = true

  private[kernel] var query = false

  private val queryNameCache = new NameCache(Configuration(systemContext).requiredIntProperty(MAX_QUERY_NAME_CACHE_SIZE_PARAMETER))

  private[kernel] val copyCache = new CanonicalMap[BlockElement](Configuration(systemContext).requiredIntProperty(MAX_COPY_CACHE_SIZE_PARAMETER))

  def selectedTime = _selectedTime

  def selectedTime(timestamp: String) {
    queryNameCache.clear
    _selectedTime = timestamp
  }

  def currentJournalEntryRoot = _currentJournalEntryRoot

  private def addressMap = kernelRootElement.addressMap

  def roleIsA(roleName1: String, roleName2: String) = {
    val role1 = Kernel(systemContext).roles.role(roleName1)
    role1.isA(roleName2)
  }

  def elementRoleIsA(roleName1: String, roleName2: String) = {
    val role1 = Jits(systemContext).jitRole(roleName1).asInstanceOf[ElementRole]
    role1.isA(roleName2)
  }

  /**
   * Returns the latest starting time of the transaction, which is also the inverse of the 
   * name/uuid of the associated journal rolon.
   * <p>
   * A transaction is often restarted when a collision with another transaction/thread occurs,
   * there being a one-to-one corrispondence between threads, transactions and journal entries.
   * When a collision occurs, the youngest transaction is rewound and restarted, a new starting 
   * time is assigned and a new journal entry is created.
   */
  def startingTime = _startingTime

  /**
   * Resets the selectedTime variable to current time. Updates then are re-enabled.
   */
  def currentTime {
    selectedTime(Timestamp.CURRENT_TIME)
  }

  /**
   * Provides the state of the selectedTime varaibel.
   * @return True when selectedTime is set to current time and updates are enabled.
   */
  def isCurrentTime = selectedTime == Timestamp.CURRENT_TIME

  def effected(uuid: String) {
    if (uuid == null || uuid.isEmpty) throw new IllegalArgumentException("Effected rolon UUID cannot be null or empty")
    val invertedStartingTime = Timestamp.invert(startingTime)
    val timestamps = kernelRootElement.timestamps
    if (uuid.startsWith(invertedStartingTime + "_")) {
      if (!timestamps.contents.contains(uuid)) {
        timestamps.contents.add(uuid, EMPTY_JIT_ELEMENT_ROLE_NAME)
      }
    }
  }

  def makeRolonRootElement(uuid: String) = {
    var rv = rolonRootElement(uuid)
    if (rv == null) {
      rv = createRolonRootElement(uuid)
    }
    rv
  }

  def rolonRootElementName(uuid: String) = uuid + 1.asInstanceOf[Char] + Timestamp.invert(startingTime)

  def createRolonRootElement(uuid: String) = {
    val rolonType = UuidExtension(uuid)
    _createRolonRootElement(uuid, rolonType)
  }

  private def _createRolonRootElement(uuid: String, rolonType: String) = {
    systemContext.synchronized{
      if (addressMap.has(uuid)) {
        throw new UnsupportedOperationException("Rolon already exists: " + uuid)
      }
      val role = Kernel(systemContext).role(rolonType)
      if (role == null) throw new IllegalArgumentException("invalid UUID/role: " + uuid + "/" + rolonType)
      val rolonRootElementRole = role.rootElementType
      val name = rolonRootElementName(uuid)
      val handle = addressMap.contents.add(name, KERNEL_BLOCK_HANDLE_ELEMENT_ROLE_NAME).asInstanceOf[KernelBlockHandleElement]
      val rolonRootElement = handle.set(rolonRootElementRole).asInstanceOf[RolonRootElement]
      rolonRootElement.setRolonType(rolonType)
      Initialize(rolonRootElement)
      if (!rolonRootElement.rolonIsA(UtilNames.PAGE_TYPE)) {
        throw new IllegalArgumentException(rolonType + " does not inclue the page type")
      }
      rolonRootElement
    }
  }

  def putRolonRootElement(uuid: String, rolonRootElement: RolonRootElement) {
    systemContext.synchronized{
      if (addressMap.has(uuid)) {
        throw new UnsupportedOperationException("Page already exists: " + uuid)
      }
      val name = rolonRootElementName(uuid)
      val handle = addressMap.contents.add(name, KERNEL_BLOCK_HANDLE_ELEMENT_ROLE_NAME).asInstanceOf[KernelBlockHandleElement]
      handle._set(rolonRootElement)
    }
  }

  def journalEntryUuid = Timestamp.invert(startingTime) + "_" + Configuration(systemContext).localServerName

  private[kernel] def createJournalEntry(
                                          journalEntryFactory: JournalEntryFactory,
                                          journalEntryType: String) = {
    _currentJournalEntryRoot = null
    _currentJournalEntryRoot = _createRolonRootElement(journalEntryUuid, journalEntryType)
    if (!_currentJournalEntryRoot.rolonIsA(KernelNames.CHANGE_TYPE)) {
      throw new IllegalArgumentException(journalEntryType + " does not include the journalEntry role")
    }
    _currentJournalEntryRoot
  }

  def putJournalEntry(journalEntry: RolonRootElement) {
    _currentJournalEntryRoot = journalEntry
    putRolonRootElement(journalEntryUuid, journalEntry)
  }

  private[kernel] def createINodeElement(rolonUuid: String, inodeType: String) = {
    systemContext.synchronized{
      var inode: INodeElement = null
      val uuid = UUID.randomUUID.toString
      val name = uuid + 1.asInstanceOf[Char] + Timestamp.invert(startingTime)
      val handle = addressMap.contents.add(name, KERNEL_BLOCK_HANDLE_ELEMENT_ROLE_NAME).asInstanceOf[KernelBlockHandleElement]
      inode = handle.set(inodeType).asInstanceOf[INodeElement]
      inode.setRolonUuid(rolonUuid)
      inode
    }
  }

  /**
   * Returns the named rolon for the time specified by the selectedTime variable.
   * Null is returned if there is no rolon associated with the uuid/name provided or
   * if the rolon had not been created or had been deleted for the time given by the selectedTime variable.
   * @param uuid The unique identifer or name of the rolon of interest.
   * @return A hard reference to the rolon, or null.
   */
  def rolonRootElement(uuid: String): RolonRootElement = {
    val rv = blockElement(uuid).asInstanceOf[RolonRootElement]
    /*
    if (rv!=null){
      val tagLine=rv.attributes.get("tagLine")
      if (tagLine!=null){
        val ut = rv.attributes.get("updateTimestamp")
        val st = TransactionContext().startingTime
        if (ut != null && ut>st){
//        if (ut != null && ut>selectedTime){
          System.err.println("tc read rolon root uuid="+uuid)
          System.err.println("tagline="+rv.attributes.get("tagLine"))
          System.err.println("  "+ut)
          System.err.println("  "+st)
//          System.err.println("  "+selectedTime)
          System.err.println("  !!!!!!!!!!!!!!!!!!!!!!!")
            }
        //val ait = rv.attributes.iterator
        //while (ait.hasNext) {
      //    val an = ait.next
    //      val av = rv.attributes.get(an)
  //        println(an+"="+av)
//        }
      }
    }
    */
    rv
  }

  private[kernel] def iNode(uuid: String) = {
    blockElement(uuid).asInstanceOf[INodeElement]
  }

  private[kernel] def blockElement(uuid: String): BlockElement = {
    var be: BlockElement = null
    val requestName = uuid + 1.asInstanceOf[Char] + selectedTime
    //System.err.println("requestName="+requestName)
    if (query) {
      val revisedName = queryNameCache.get(requestName)
      if (revisedName != null) {
        //System.err.println("revisedName="+revisedName)
        be = Kernel(systemContext).queryCache.get(revisedName)
      }
    } else {
      if (selectedTime >= startingTime) {
        be = copyCache.get(uuid)
      }
    }
    if (be == null) {
      var name: String = null
      systemContext.synchronized{
        if (addressMap == null) throw new IllegalStateException("address map is null")
        name = addressMap.getName(uuid, selectedTime)
        if (name != null) {
          //System.err.println("name="+name)
          val handle = addressMap.contents.get(name).asInstanceOf[KernelBlockHandleElement]
          be = handle.resolve
        }
      }
      if (query && be != null) {
        queryNameCache.put(requestName, name)
      }
    }
    be
  }

  /**
   * Returns the journal entry rolon associated with the given starting time.
   * Null is returned if there is no journal entry associated with the timestamp provided or
   * if the timestamp is greater than the value of the selectedTime variable.
   * @param timestamp The inverse of the journal entry's uuid/name.
   * @return A hard reference to the journal entry rolon, or null.
   */
  def journalEntry(timestamp: String) = {
    val rv = rolonRootElement(Timestamp.invert(timestamp))
    if (rv != null && !rv.rolonIsA(KernelNames.CHANGE_TYPE)) {
      throw new IllegalStateException(timestamp + " does not have a journalEntry role")
    }
    rv
  }

  /**
   * Returns the ark rolon, creating it as needed.
   */
  def ark = {
    var ark = rolonRootElement(KernelNames.HOME_UUID)
    if (ark == null) {
      if (query) throw new IllegalStateException("query on uninitialized database")
      ark = createRolonRootElement(KernelNames.HOME_UUID)
    }
    ark
  }

  /**
   * Marks the named rolon as deleted at the starting time of the transaction.
   * Prior state can however still be accessed through the use of the selectedTime variable.
   * @param uuid The unique identifer or name of the rolon of interest.
   */
  def delete(uuid: String) {
    systemContext.synchronized{
      val name = addressMap.getName(uuid)
      if (name == null) {
        null
      } else {
        val handle = addressMap.contents.get(name).asInstanceOf[KernelBlockHandleElement]
        val blockElement = handle.resolve.asInstanceOf[BlockElement]
        val persistence = blockElement.persistence
        persistence.writeLock
        blockElement._delete
        persistence.markDirty
      }
    }
  }

  /**
   * Returns a SequenceSource for the starting times of all 
   * completed transactions.
   * @return A SequenceSource of timestamps.
   */
  def journalEntryTimestampSequence = {
    val re = Kernel(systemContext).kernelRootElement
    val timestamps = re.timestamps
    val sub = timestamps.contents.sequence
    val filtered = new FilteredSequence(sub, re.inactiveFilter)
    val inv = new InvertedTimestampSequence(filtered)
    new SynchronizedSequence(systemContext, inv)
  }

  def validate {
    systemContext.synchronized{
      val re = Kernel(systemContext).kernelRootElement
      val map = new DiskMap
      re.validate(map)
      if (map.size > 1) {
        println()
        println(re.databasePathname + " does not have contiguous space:")
        map.print
        throw new IllegalStateException(re.databasePathname + " does not have contiguous space")
      }
    }
  }

  /**
   * Prints all journal entries
   */
  def printJournalEntries {
    println()
    println("All processed Journal Entries:")
    val seq = journalEntryTimestampSequence
    val it = new SequenceIterator(seq)
    while (it.hasNext) {
      val ts = it.next
      val be = journalEntry(ts)
      var rt = ""
      if (be != null) {
        rt = be.rolonType
      }
      println(ts + " = " + be + ", type = " + rt)
    }
  }

  private[kernel] var _writeOnly: String = null

  private[kernel] val _initialStartingTime = Timestamp.timestamp

  private[kernel] var _startingTime: String = null

  private[kernel] def writeOnly = _writeOnly

  private[kernel] def setWriteOnly(uuid: String) {
    _writeOnly = uuid
  }

  private[kernel] def initialStartingTime = _initialStartingTime

  private[kernel] def setStartingTime(timestamp: String) {
    _startingTime = timestamp
  }

  private[kernel] def commit {
    systemContext.synchronized{
      val root = Kernel(systemContext).kernelRootElement
      root.commit
    }
    val it = copyCache.iterator
    while (it.hasNext) {
      val uuid = it.next
      val be = copyCache.get(uuid)
      if (be.persistence.kernelHandleElement != null) {
        Kernel(systemContext).queryCache.put(be.getJitName, be)
      }
    }
  }


  def deleteSequensor(id: String) {
    val sequensors = Kernel(systemContext).kernelRootElement.contents.get("PendingSequensors").asInstanceOf[KernelPendingSequensors]
    if (sequensors != null) sequensors.contents.delete(id)
  }

  def getSequensor(id: String): KernelPendingSequensor = {
    val sequensors = Kernel(systemContext).kernelRootElement.contents.get("PendingSequensors")
    if (sequensors == null) null
    else
      sequensors.asInstanceOf[KernelPendingSequensors].contents.get(id)
        .asInstanceOf[KernelPendingSequensor]
  }

  def makeSequensor(id: String): KernelPendingSequensor = {
    val sequensors = Kernel(systemContext).kernelRootElement.
      contents.make("PendingSequensors", KERNEL_PENDING_SEQUENSORS_ROLE_NAME).asInstanceOf[KernelPendingSequensors]
    val rv = sequensors.contents.make(id, KERNEL_PENDING_SEQUENSOR_ROLE_NAME).asInstanceOf[KernelPendingSequensor]
    rv
  }

  def getSequensorsIterator = {
    val sequensors = Kernel(systemContext).kernelRootElement.
      contents.get("PendingSequensors")
    if (sequensors == null) new SequenceIterator(new EmptySequence)
    else new SequenceIterator(sequensors.asInstanceOf[KernelPendingSequensors].contents.sequence)
  }

}
