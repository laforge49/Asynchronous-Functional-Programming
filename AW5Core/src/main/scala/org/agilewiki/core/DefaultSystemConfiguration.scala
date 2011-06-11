/*
 * Copyright 2010  Alex K.
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

import org.agilewiki.kernel.operation.Config

import java.util.Properties

object DefaultSystemConfiguration {
  def apply(properties: Properties, dbName: String, arkName: String) {
    org.agilewiki.kernel.DefaultSystemConfiguration(properties, dbName, arkName)
    new Config(properties) {
      arkManager(CoreNames.ACCESS_TYPE, "Master")
      arkManager(CoreNames.ADMIN_GROUP_TYPE, "Master")
      arkManager(CoreNames.ADMIN_USER_TYPE, "Master")
      arkManager(CoreNames.GROUP_TYPE, "Master")
      arkManager(CoreNames.GROUPS_TYPE, "Master")
      arkManager(CoreNames.USERS_TYPE, "Master")
      arkManager(CoreNames.SYSTEM_USER_TYPE, "Master")
      arkManager(CoreNames.USER_TYPE, "Master")
      arkManager(CoreNames.ANONYMOUS_TYPE, "Master")
      arkManager(CoreNames.COMMANDS_TYPE, "Master")
      arkManager(CoreNames.LOGON_TYPE, "Master")
      arkManager(CoreNames.LOGOFF_TYPE, "Master")
      arkManager(CoreNames.NEW_USER_TYPE, "Master")
      arkManager(CoreNames.TAGLINE_TYPE, "Master")
      arkManager(CoreNames.TIMEZONE_TYPE, "Master")
      arkManager(CoreNames.PASSWORD_TYPE, "Master")
      arkManager(CoreNames.RENAME_TYPE, "Master")
      arkManager(CoreNames.DELETE_TYPE, "Master")
      arkManager(CoreNames.BASE_GROUPS_TYPE, "Master")
      arkManager(CoreNames.CREATE_GROUPS_TYPE, "Master")
      arkManager(CoreNames.CREATE_GROUP_TYPE, "Master")
      arkManager(CoreNames.CREATE_MY_GROUP_TYPE, "Master")
      arkManager(CoreNames.MY_GROUP_TYPE, "Master")
      arkManager(CoreNames.EDIT_MEMBERSHIP_TYPE, "Master")
      arkManager(CoreNames.EDIT_USER_ACCESS_TYPE, "Master")
      arkManager(CoreNames.EDIT_GROUP_ACCESS_TYPE, "Master")
      arkManager(CoreNames.EDIT_USER_LIMITS_TYPE, "Master")
      arkManager(CoreNames.SET_PASSWORD_TYPE, "Master")
      arkManager(CoreNames.SUBTOPICS_TYPE, "Master")
      arkManager(CoreNames.CREATE_SUBTOPICS_TYPE, "Master")
      arkManager(CoreNames.SUBTOPIC_TYPE, "Master")
      arkManager(CoreNames.CREATE_SUBTOPIC_TYPE, "Master")
      arkManager(CoreNames.EDIT_SUBTOPICS_TYPE, "Master")
      arkManager(CoreNames.ORDER_TYPE, "Master")
      arkManager(CoreNames.EDIT_TEXT_TYPE, "Master")
    }
  }
}
