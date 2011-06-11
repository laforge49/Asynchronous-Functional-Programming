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
package org.agilewiki.util
package jit

import java.util.Properties

object _Jits {
  def defaultConfiguration(serverName: String) = {
    val properties = new Properties
    Configuration.defaultConfiguration(properties, serverName)
    Jits.defaultConfiguration(properties)
    properties
  }
}

class _Jits(configurationProperties: Properties)
  extends SystemComposite
  with SystemConfigurationComponent
  with SystemJitsComponent {
  setProperties(configurationProperties)

  def close {}
}

object Jits {

  def apply(context: SystemComposite) = context.asInstanceOf[SystemJitsComponent].jits

  def defaultConfiguration(properties: Properties) {
    new JitConfig(properties) {
      role(Jit defaultRoleName)
      jitClass(classOf[Jit] getName)

      role(JitBoolean defaultRoleName)
      jitClass(classOf[JitBoolean] getName)

      role(JitBytes defaultRoleName)
      jitClass(classOf[JitBytes] getName)

      role(JitInt defaultRoleName)
      jitClass(classOf[JitInt] getName)

      role(JitLong defaultRoleName)
      jitClass(classOf[JitLong] getName)

      role(JitNamedJitList defaultRoleName)
      jitClass(classOf[JitNamedJitList] getName)

      role(JitNamedStringTreeMap defaultRoleName)
      jitClass(classOf[JitNamedStringTreeMap] getName)
      jitSubRole(JitString defaultRoleName)

      role(JitNamedVariableJitTreeMap defaultRoleName)
      jitClass(classOf[JitNamedVariableJitTreeMap] getName)

      role(JitString defaultRoleName)
      jitClass(classOf[JitString] getName)

      role(JitWrapper defaultRoleName)
      jitClass(classOf[JitWrapper] getName)
    }
  }
}

trait SystemJitsComponent {
  this: SystemComposite
    with SystemConfigurationComponent =>

  protected lazy val _orgAgileWikiUtilJitsJits = defineJits

  protected def defineJits = new Jits

  def jits = _orgAgileWikiUtilJitsJits

  class Jits {
    private var _jitRoles: JitRoles = createJitRoles

    protected def createJitRoles = new JitRoles(SystemJitsComponent.this)

    def jitRoles = _jitRoles

    def jitRole(roleName: String) = jitRoles.role(roleName)

    def createJit(roleName: String) = jitRole(roleName).createJit
  }

}
