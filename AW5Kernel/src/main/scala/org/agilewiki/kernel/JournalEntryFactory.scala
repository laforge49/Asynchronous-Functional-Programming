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

import org.agilewiki.kernel.element.RolonRootElement
import util.{SystemComposite, Timestamp}

trait JournalEntryFactory {

  def assignStartingTime = Timestamp.timestamp

  def preValidate {}

  def journalEntry = {
    val je = TransactionContext().createJournalEntry(this, journalEntryType)
    initializeJournalEntry(je)
    je
  }

  /**
   * Returns the rolonType of the journal entry rolon to be created.
   * but if null is returned, then query is called.
   * @return rolonType or null
   */
  def journalEntryType: String = null

  /**
   * The journal entry rolon has been created and, if initialization
   * is required, it can be done at this time.
   * However, the journal entry rolon is the only item in the
   * database that can be updated.
   * @param journalEntry The rolon to be initialized.
   */
  def initializeJournalEntry(journalEntry: RolonRootElement) {}
}
