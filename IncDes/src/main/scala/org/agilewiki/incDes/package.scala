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

import blip._

package object incDes {
  val INC_DES_FACTORY_ID = FactoryId("id")
  val INC_DES_INT_FACTORY_ID = FactoryId("idI")
  val INC_DES_LONG_FACTORY_ID = FactoryId("idl")
  val INC_DES_STRING_FACTORY_ID = FactoryId("idS")
  val INC_DES_BOOLEAN_FACTORY_ID = FactoryId("idB")
  val INC_DES_BYTES_FACTORY_ID = FactoryId("idA")
  val INC_DES_INCDES_FACTORY_ID = FactoryId("idW")

  val INC_DES_INT_LIST_FACTORY_ID = FactoryId("idLi")
  val INC_DES_LONG_LIST_FACTORY_ID = FactoryId("idLl")
  val INC_DES_STRING_LIST_FACTORY_ID = FactoryId("idLS")
  val INC_DES_BOOLEAN_LIST_FACTORY_ID = FactoryId("idLB")
  val INC_DES_BYTES_LIST_FACTORY_ID = FactoryId("idLA")
  val INC_DES_INCDES_LIST_FACTORY_ID = FactoryId("idLW")
}
