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
package incDes

import blip._
import bind._
import services._

class IncDesIncDesFactory(id: FactoryId)
  extends IncDesFactory(id) {

  override protected def instantiate = new IncDesIncDes
}

object IncDesIncDes {
  def apply(mailbox: Mailbox) = {
    new IncDesIncDesFactory(INC_DES_INCDES_FACTORY_ID).newActor(mailbox).asInstanceOf[IncDesIncDes]
  }
}

class IncDesIncDes extends IncDesItem[IncDes] {
  protected var i: IncDes = null
  protected var len = -1

  bind(classOf[MakeSet], makeSet)
  bind(classOf[Assign], assign)

  lazy val factoryRegistryComponentFactory = {
    if (systemServices == null) throw new IllegalStateException("SystemServices is required")
    systemServices.factory.componentFactory(classOf[FactoryRegistryComponentFactory]).
      asInstanceOf[FactoryRegistryComponentFactory]
  }

  override def partness(container: IncDes, _key: Any, visibleContainer: IncDes) {
    super.partness(container, _key, visibleContainer)
    if (i != null) i.partness(this, key, this)
  }

  override def length = if (len == -1) intLength else intLength + len

  override def load(_data: MutableData) {
    super.load(_data)
    len = loadLen(_data)
    if (len > 0) _data.skip(len)
    i = null
    dser = false
  }

  def loadLen(_data: MutableData) = _data.readInt

  override def change(transactionContext: TransactionContext, lenDiff: Int, what: IncDes, rf: Any => Unit) {
    if (len == -1) len = lenDiff
    else len += lenDiff
    changed(transactionContext, lenDiff, what, rf)
  }

  override protected def serialize(_data: MutableData) {
    if (!dser) throw new IllegalStateException
    saveLen(_data)
    if (len < 0) return
    val incDesFactoryId = i.factoryId.value
    _data.writeString(incDesFactoryId)
    i.save(_data)
  }

  def saveLen(_data: MutableData) {
    _data.writeInt(len)
  }

  protected def deserialize(rf: Any => Unit) {
    if (dser) {
      rf(null)
      return
    }
    if (!isSerialized) throw new IllegalStateException
    val m = data.mutable
    skipLen(m)
    if (len == -1) {
      dser = true
      rf(null)
      return
    }
    val incDesFactoryId = FactoryId(m.readString)
    systemServices(Instantiate(incDesFactoryId, exchangeMessenger)) {
      rsp => {
        i = rsp.asInstanceOf[IncDes]
        i.load(m)
        i.partness(this, key, this)
        dser = true
        rf(null)
      }
    }
  }

  override def value(msg: AnyRef, rf: Any => Unit) {
    deserialize {
      rsp => rf(i)
    }
  }

  override def resolve(msg: AnyRef, rf: Any => Unit) {
    val pathname = msg.asInstanceOf[Resolve].pathname
    if (pathname.length == 0) {
      rf((this, ""))
      return
    }
    if (pathname.startsWith("/"))
      throw new IllegalArgumentException("Unexpected pathname: " + pathname)
    if (pathname == "$") {
      rf((this, "$"))
      return
    }
    if (!pathname.startsWith("$/"))
      throw new IllegalArgumentException("No match for pathname: " + pathname)
    var newPathname = pathname.substring(2)
    value(Value(), {
      rsp => {
        if (rsp == null) {
          if (pathname == "$/") rf((null, ""))
          else throw new IllegalArgumentException("No match for pathname: " + pathname)
        } else {
          val incDes = rsp.asInstanceOf[IncDes]
          incDes(Resolve(newPathname))(rf)
        }
      }
    })
  }

  def skipLen(m: MutableData) {
    m.skip(intLength)
  }

  def makeSet(msg: AnyRef, rf: Any => Unit) {
    if (len > -1) {
      value(Value(), rf)
      return
    }
    val s = msg.asInstanceOf[MakeSet]
    val tc = s.transactionContext
    val fid = s.factoryId
    writable(tc) {
      r1 => {
        systemServices(Instantiate(fid, exchangeMessenger)) {
          r2 => {
            i = r2.asInstanceOf[IncDes]
            i.partness(this, key, this)
            dser = true
            len = stringLength(fid.value) + i.length
            changed(tc, len, this, {
              r3: Any => {
                rf(i)
              }
            })
          }
        }
      }
    }
  }

  override def set(msg: AnyRef, rf: Any => Unit) {
    val s = msg.asInstanceOf[Set[IncDes]]
    val v = s.value
    val tc = s.transactionContext
    if (v != null) {
      if (v.container != null)
        throw new IllegalArgumentException("already in use: " + v)
      val vfactory = v.factory
      if (vfactory == null)
        throw new IllegalArgumentException("factory is null: " + v.getClass.getName)
      val fid = vfactory.id
      if (fid == null) throw new IllegalArgumentException("factory id is null")
      if (factoryRegistryComponentFactory == null)
        throw new IllegalStateException("SystemServices does not contain FactoryRegistryComponentFactory")
      if (factoryRegistryComponentFactory.getFactory(fid) == null)
        throw new IllegalArgumentException("unregistered factory id: " + fid)
      if (exchangeMessenger != v.exchangeMessenger) {
        if (v.exchangeMessenger == null && !v.isOpen) v.setExchangeMessenger(exchangeMessenger)
        else throw new IllegalStateException("uses a different mailbox")
      }
      if (v.systemServices == null && !v.isOpen) v.setSystemServices(systemServices)
    }
    writable(tc) {
      rsp => {
        val olen = length
        if (i != null) i.clearContainer
        i = v
        if (i == null) {
          len = -1
        } else {
          len = stringLength(i.factoryId.value) + i.length
          i.partness(this, key, this)
        }
        dser = true
        changed(tc, length - olen, this, rf)
      }
    }
  }

  def assign(msg: AnyRef, rf: Any => Unit) {
    val s = msg.asInstanceOf[Assign]
    val key = s.key
    if (key != "$") throw new IllegalArgumentException("Invalid key: " + key)
    val v = s.value
    val tc = s.transactionContext
    set(Set(tc, v), rf)
  }
}
