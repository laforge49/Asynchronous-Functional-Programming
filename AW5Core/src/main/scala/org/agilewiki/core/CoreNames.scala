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

import kernel.KernelNames

trait CoreNames extends KernelNames {
  val ACCESS_TYPE = "cp1"
  val ADMIN_GROUP_TYPE = "cp2"
  val ADMIN_USER_TYPE = "cp3"
  val AUTHORITY_TYPE = "cp4"
  val CORE_INITIALIZATION_CHANGE_TYPE = "cp5"
  val GROUP_TYPE = "cp9"
  val GROUPS_TYPE = "cp10"
  val USER_TYPE = "cp11"
  val SYSTEM_USER_TYPE = "cp12"
  val USERS_TYPE = "cp13"
  val BATCH_TYPE = "cp14"
  val ANONYMOUS_TYPE = "cp15"
  val COMMANDS_TYPE = "cp16"
  val COMMAND_TYPE = "cp17"
  val LOGON_TYPE = "cp18"
  val LOGOFF_TYPE = "cp19"
  val NEW_USER_TYPE = "cp20"
  val TAGLINE_TYPE = "cp21"
  val REALM_TYPE = "cp21"
  val TIMEZONE_TYPE = "cp22"
  val PASSWORD_TYPE = "cp23"
  val RENAME_TYPE = "cp24"
  val DELETE_TYPE = "cp25"
  val BASE_GROUPS_TYPE = "cp26"
  val CREATE_GROUPS_TYPE = "cp27"
  val CREATE_GROUP_TYPE = "cp28"
  val CREATE_MY_GROUP_TYPE = "cp30"
  val MY_GROUP_TYPE = "cp31"
  val EDIT_MEMBERSHIP_TYPE = "cp32"
  val EDIT_USER_ACCESS_TYPE = "cp33"
  val EDIT_GROUP_ACCESS_TYPE = "cp34"
  val EDIT_USER_LIMITS_TYPE = "cp35"
  val SET_PASSWORD_TYPE = "cp36"
  val SUBTOPICS_TYPE = "cp37"
  val CREATE_SUBTOPICS_TYPE = "cp38"
  val SUBTOPIC_TYPE = "cp39"
  val CREATE_SUBTOPIC_TYPE = "cp40"
  val EDIT_SUBTOPICS_TYPE = "cp41"
  val ORDER_TYPE = "cp42"
  val EDIT_TEXT_TYPE = "cp43"

  val ACCESS_UUID = ACCESS_TYPE + "_" + ACCESS_TYPE
  val ADMIN_GROUP_UUID = ADMIN_GROUP_TYPE + "_" + ADMIN_GROUP_TYPE
  val ADMIN_USER_UUID = ADMIN_USER_TYPE + "_" + ADMIN_USER_TYPE
  val GROUPS_UUID = GROUPS_TYPE + "_" + GROUPS_TYPE
  val SYSTEM_USER_UUID = SYSTEM_USER_TYPE + "_" + SYSTEM_USER_TYPE
  val USERS_UUID = USERS_TYPE + "_" + USERS_TYPE
  val ANONYMOUS_UUID = ANONYMOUS_TYPE + "_" + ANONYMOUS_TYPE
  val COMMANDS_UUID = COMMANDS_TYPE + "_" + COMMANDS_TYPE
  val LOGON_UUID = LOGON_TYPE + "_" + LOGON_TYPE
  val LOGOFF_UUID = LOGOFF_TYPE + "_" + LOGOFF_TYPE
  val NEW_USER_UUID = NEW_USER_TYPE + "_" + NEW_USER_TYPE
  val TAGLINE_UUID = TAGLINE_TYPE + "_" + TAGLINE_TYPE
  val TIMEZONE_UUID = TIMEZONE_TYPE + "_" + TIMEZONE_TYPE
  val PASSWORD_UUID = PASSWORD_TYPE + "_" + PASSWORD_TYPE
  val RENAME_UUID = RENAME_TYPE + "_" + RENAME_TYPE
  val DELETE_UUID = DELETE_TYPE + "_" + DELETE_TYPE
  val CREATE_GROUPS_UUID = CREATE_GROUPS_TYPE + "_" + CREATE_GROUPS_TYPE
  val CREATE_GROUP_UUID = CREATE_GROUP_TYPE + "_" + CREATE_GROUP_TYPE
  val CREATE_MY_GROUP_UUID = CREATE_MY_GROUP_TYPE + "_" + CREATE_MY_GROUP_TYPE
  val EDIT_MEMBERSHIP_UUID = EDIT_MEMBERSHIP_TYPE + "_" + EDIT_MEMBERSHIP_TYPE
  val EDIT_USER_ACCESS_UUID = EDIT_USER_ACCESS_TYPE + "_" + EDIT_USER_ACCESS_TYPE
  val EDIT_GROUP_ACCESS_UUID = EDIT_GROUP_ACCESS_TYPE + "_" + EDIT_GROUP_ACCESS_TYPE
  val EDIT_USER_LIMITS_UUID = EDIT_USER_LIMITS_TYPE + "_" + EDIT_USER_LIMITS_TYPE
  val SET_PASSWORD_UUID = SET_PASSWORD_TYPE + "_" + SET_PASSWORD_TYPE
  val CREATE_SUBTOPICS_UUID = CREATE_SUBTOPICS_TYPE + "_" + CREATE_SUBTOPICS_TYPE
  val CREATE_SUBTOPIC_UUID = CREATE_SUBTOPIC_TYPE + "_" + CREATE_SUBTOPIC_TYPE
  val EDIT_SUBTOPICS_UUID = EDIT_SUBTOPICS_TYPE + "_" + EDIT_SUBTOPICS_TYPE
  val ORDER_UUID = ORDER_TYPE + "_" + ORDER_TYPE
  val EDIT_TEXT_UUID = EDIT_TEXT_TYPE + "_" + EDIT_TEXT_TYPE

  val ACCESS_NAME = "Everyone"
  val ADMIN_GROUP_NAME = "Admin"
  val ADMIN_USER_NAME = "Admin"
  val GROUPS_NAME = "Groups"
  val SYSTEM_USER_NAME = "System"
  val USERS_NAME = "Users"
  val ANONYMOUS_NAME = "Anonymous"
  val COMMANDS_NAME = "Commands"
  val LOGON_NAME = "Logon"
  val LOGOFF_NAME = "Logoff"
  val NEW_USER_NAME = "New User"
  val TAGLINE_NAME = "Tagline"
  val TIMEZONE_NAME = "Timezone"
  val PASSWORD_NAME = "Password"
  val RENAME_NAME = "Rename"
  val DELETE_NAME = "Delete"
  val CREATE_GROUPS_NAME = "Create Groups"
  val CREATE_GROUP_NAME = "Create User Group"
  val CREATE_MY_GROUP_NAME = "Create My Group"
  val EDIT_MEMBERSHIP_NAME = "Edit Membership"
  val BASE_GROUPS_NAME = "Groups"
  val EDIT_USER_ACCESS_NAME = "Edit User Access"
  val EDIT_GROUP_ACCESS_NAME = "Edit Group Access"
  val EDIT_USER_LIMITS_NAME = "Edit User Limits"
  val SET_PASSWORD_NAME = "Set Password"
  val SUBTOPICS_NAME = "Subtopics"
  val CREATE_SUBTOPICS_NAME = "Create Subtopics"
  val CREATE_SUBTOPIC_NAME = "Create Subtopic"
  val EDIT_SUBTOPICS_NAME = "Edit Subtopics"
  val ORDER_NAME = "Order"
  val EDIT_TEXT_NAME = "Edit Text"

  typeNames.put("change", BATCH_TYPE)
  expandTypeNames.put(BATCH_TYPE, "change")

  typeNames.put("group", GROUP_TYPE)
  expandTypeNames.put(GROUP_TYPE, "group")

  typeNames.put("adminGroup", ADMIN_GROUP_TYPE)
  expandTypeNames.put(ADMIN_GROUP_TYPE, "group")

  typeNames.put("access", ACCESS_TYPE)
  expandTypeNames.put(ACCESS_TYPE, "group")

  typeNames.put("groups", GROUPS_TYPE)
  expandTypeNames.put(GROUPS_TYPE, "groups")

  typeNames.put("myGroups", BASE_GROUPS_TYPE)
  expandTypeNames.put(BASE_GROUPS_TYPE, "myGroups")

  typeNames.put("coreInitializationChange", CORE_INITIALIZATION_CHANGE_TYPE)
  expandTypeNames.put(CORE_INITIALIZATION_CHANGE_TYPE, "initialize")

  typeNames.put("user", USER_TYPE)
  expandTypeNames.put(USER_TYPE, "user")

  typeNames.put("adminUser", ADMIN_USER_TYPE)
  expandTypeNames.put(ADMIN_USER_TYPE, "user")

  typeNames.put("anonymous", ANONYMOUS_TYPE)
  expandTypeNames.put(ANONYMOUS_TYPE, "user")

  typeNames.put("systemUser", SYSTEM_USER_TYPE)
  expandTypeNames.put(SYSTEM_USER_TYPE, "user")

  typeNames.put("users", USERS_TYPE)
  expandTypeNames.put(USERS_TYPE, "users")

  typeNames.put("commands", COMMANDS_TYPE)
  expandTypeNames.put(COMMANDS_TYPE, "commands")

  typeNames.put("logon", LOGON_TYPE)
  expandTypeNames.put(LOGON_TYPE, "command")

  typeNames.put("logoff", LOGOFF_TYPE)
  expandTypeNames.put(LOGOFF_TYPE, "command")

  typeNames.put("newUser", NEW_USER_TYPE)
  expandTypeNames.put(NEW_USER_TYPE, "command")

  typeNames.put("tagline", TAGLINE_TYPE)
  expandTypeNames.put(TAGLINE_TYPE, "command")

  typeNames.put("timezone", TIMEZONE_TYPE)
  expandTypeNames.put(TIMEZONE_TYPE, "command")

  typeNames.put("password", PASSWORD_TYPE)
  expandTypeNames.put(PASSWORD_TYPE, "command")

  typeNames.put("rename", RENAME_TYPE)
  expandTypeNames.put(RENAME_TYPE, "command")

  typeNames.put("delete", DELETE_TYPE)
  expandTypeNames.put(DELETE_TYPE, "command")

  typeNames.put("createGroups", CREATE_GROUPS_TYPE)
  expandTypeNames.put(CREATE_GROUPS_TYPE, "command")

  typeNames.put("createGroup", CREATE_GROUP_TYPE)
  expandTypeNames.put(CREATE_GROUP_TYPE, "command")

  typeNames.put("createMyGroup", CREATE_MY_GROUP_TYPE)
  expandTypeNames.put(CREATE_MY_GROUP_TYPE, "command")

  typeNames.put("myGroup", MY_GROUP_TYPE)
  expandTypeNames.put(MY_GROUP_TYPE, "myGroup")

  typeNames.put("editMembership", EDIT_MEMBERSHIP_TYPE)
  expandTypeNames.put(EDIT_MEMBERSHIP_TYPE, "command")

  typeNames.put("editUserAccess", EDIT_USER_ACCESS_TYPE)
  expandTypeNames.put(EDIT_USER_ACCESS_TYPE, "command")

  typeNames.put("editGroupAccess", EDIT_GROUP_ACCESS_TYPE)
  expandTypeNames.put(EDIT_GROUP_ACCESS_TYPE, "command")

  typeNames.put("editUserLimits", EDIT_USER_LIMITS_TYPE)
  expandTypeNames.put(EDIT_USER_LIMITS_TYPE, "command")

  typeNames.put("setPassword", SET_PASSWORD_TYPE)
  expandTypeNames.put(SET_PASSWORD_TYPE, "command")

  typeNames.put("subtopics", SUBTOPICS_TYPE)
  expandTypeNames.put(SUBTOPICS_TYPE, "subtopics")

  typeNames.put("createSubtopics", CREATE_SUBTOPICS_TYPE)
  expandTypeNames.put(CREATE_SUBTOPICS_TYPE, "command")

  typeNames.put("subtopic", SUBTOPIC_TYPE)
  expandTypeNames.put(SUBTOPIC_TYPE, "subtopic")

  typeNames.put("createSubtopic", CREATE_SUBTOPIC_TYPE)
  expandTypeNames.put(CREATE_SUBTOPIC_TYPE, "command")

  typeNames.put("editSubtopics", EDIT_SUBTOPICS_TYPE)
  expandTypeNames.put(EDIT_SUBTOPICS_TYPE, "command")

  typeNames.put("order", ORDER_TYPE)
  expandTypeNames.put(ORDER_TYPE, "command")

  typeNames.put("editText", EDIT_TEXT_TYPE)
  expandTypeNames.put(EDIT_TEXT_TYPE, "command")

  val ACCESS_RELATIONSHIP = "a"
  val COMMAND_RELATIONSHIP = "C"
  val EFFECTS_RELATIONSHIP = "e"
  val ALL_HEADLINES_RELATIONSHIP = "h"
  val REALM_HEADLINES_RELATIONSHIP = "h1"
  val LIMIT_RELATIONSHIP = "l"
  val MEMBER_RELATIONSHIP = "m"
  val PARENT_RELATIONSHIP = "p"
  val REALM_RELATIONSHIP = "r"
  val REALM_CHANGE_RELATIONSHIP = "r1"
  val SUBTOPIC_RELATIONSHIP = "s"
  val USER_RELATIONSHIP = "u"

  private val relationshipNames = new java.util.HashMap[String, String] {
    put("access", ACCESS_RELATIONSHIP)
    put("command", COMMAND_RELATIONSHIP)
    put("effects", EFFECTS_RELATIONSHIP)
    put("allHeadlines", ALL_HEADLINES_RELATIONSHIP)
    put("realmHeadlines", REALM_HEADLINES_RELATIONSHIP)
    put("limit", LIMIT_RELATIONSHIP)
    put("member", MEMBER_RELATIONSHIP)
    put("parent", PARENT_RELATIONSHIP)
    put("realm", REALM_RELATIONSHIP)
    put("realmChange", REALM_CHANGE_RELATIONSHIP)
    put("user", USER_RELATIONSHIP)
    put("subtopic", SUBTOPIC_RELATIONSHIP)
  }

  def relationshipName(name: String) = {
    relationshipNames.get(name)
  }
}

object CoreNames extends CoreNames
