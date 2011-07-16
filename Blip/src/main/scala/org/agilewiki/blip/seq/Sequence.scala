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
package blip
package seq

import annotation.tailrec

case class First()

case class Current[K](key: K)

case class Next[K](key: K)

case class KVPair[K, V](key: K, value: V)

case class Loop[K, V](f: (K, V) => Unit)

case class LoopSafe(safe: Safe)

case class Fold[V](seed: V, f: (V, V) => V)

case class Exists[V](f: V => Boolean)

case class Find[V](f: V => Boolean)

abstract class Sequence[K, V](mailbox: Mailbox, factory: Factory)
  extends Actor(mailbox, factory) {
  bind(classOf[First], first)

  def first(msg: AnyRef, rf: Any => Unit)

  bind(classOf[Current[K]], current)

  def current(msg: AnyRef, rf: Any => Unit)

  bind(classOf[Next[K]], next)

  def next(msg: AnyRef, rf: Any => Unit)

  bind(classOf[Loop[K, V]], loop)

  def loop(msg: AnyRef, rf: Any => Unit) {
    val f = msg.asInstanceOf[Loop[K, V]].f
    first(First(), r => _loop(r, f, rf))
  }

  private def aloop(rsp: Any, f: (K, V) => Unit, rf: Any => Unit) {
    _loop(rsp, f, rf)
  }

  @tailrec private def _loop(rsp: Any, f: (K, V) => Unit, rf: Any => Unit) {
    if (rsp == null) {
      rf(null)
      return
    }
    var rsp1: Any = null
    var async = false
    var sync = false
    val kvPair = rsp.asInstanceOf[KVPair[K, V]]
    f(kvPair.key, kvPair.value)
    next(Next(kvPair.key), r => {
      rsp1 = r
      if (async) aloop(rsp1, f, rf)
      else sync = true
    })
    if (!sync) {
      async = true
      return
    }
    _loop(rsp1, f, rf)
  }

  bind(classOf[LoopSafe], loopSafe)

  def loopSafe(msg: AnyRef, rf: Any => Unit) {
    val safe = msg.asInstanceOf[LoopSafe].safe
    first(First(), r => _loopSafe(r.asInstanceOf[KVPair[K, V]], safe, rf))
  }

  private def aloopSafe(rsp: KVPair[K, V], safe: Safe, rf: Any => Unit) {
    _loopSafe(rsp, safe, rf)
  }

  @tailrec private def _loopSafe(rsp: KVPair[K, V], safe: Safe, rf: Any => Unit) {
    if (rsp == null) {
      rf(null)
      return
    }
    var rsp1: KVPair[K, V] = null
    var rsp2 = false
    var async = false
    var sync = false
    safe.func(rsp, fr => {
      rsp2 = fr.asInstanceOf[Boolean]
      if (rsp2) {
        next(Next(rsp.key), r => {
          rsp1 = r.asInstanceOf[KVPair[K, V]]
          if (async) aloopSafe(rsp1, safe, rf)
          else sync = true
        })
      }
    })
    if (!sync) {
      async = true
      return
    }
    if (!rsp2) return
    _loopSafe(rsp1, safe, rf)
  }

  bind(classOf[Fold[V]], fold)

  def fold(msg: AnyRef, rf: Any => Unit) {
    val req = msg.asInstanceOf[Fold[V]]
    first(First(), r => _fold(r.asInstanceOf[KVPair[K, V]], req.seed, req.f, rf))
  }

  private def afold(rsp: KVPair[K, V], seed: V, f: (V, V) => V, rf: Any => Unit) {
    _fold(rsp, seed, f, rf)
  }

  @tailrec private def _fold(rsp: KVPair[K, V], seed: V, f: (V, V) => V, rf: Any => Unit) {
    if (rsp == null) {
      rf(seed)
      return
    }
    var rsp1: KVPair[K, V] = null
    var async = false
    var sync = false
    val s = f(seed, rsp.value)
    next(Next(rsp.key), r => {
      rsp1 = r.asInstanceOf[KVPair[K, V]]
      if (async) afold(rsp1, s, f, rf)
      else sync = true
    })
    if (!sync) {
      async = true
      return
    }
    _fold(rsp1, s, f, rf)
  }

  bind(classOf[Exists[V]], exists)

  def exists(msg: AnyRef, rf: Any => Unit) {
    val req = msg.asInstanceOf[Exists[V]]
    first(First(), r => _exists(r.asInstanceOf[KVPair[K, V]], req.f, rf))
  }

  private def aexists(rsp: KVPair[K, V], f: V => Boolean, rf: Any => Unit) {
    _exists(rsp, f, rf)
  }

  @tailrec private def _exists(rsp: KVPair[K, V], f: V => Boolean, rf: Any => Unit) {
    if (rsp == null) {
      rf(false)
      return
    }
    var rsp1: KVPair[K, V] = null
    var async = false
    var sync = false
    if (f(rsp.value)) {
      rf(true)
      return
    }
    next(Next(rsp.key), r => {
      rsp1 = r.asInstanceOf[KVPair[K, V]]
      if (async) aexists(rsp1, f, rf)
      else sync = true
    })
    if (!sync) {
      async = true
      return
    }
    _exists(rsp1, f, rf)
  }

  bind(classOf[Find[V]], find)

  def find(msg: AnyRef, rf: Any => Unit) {
    val req = msg.asInstanceOf[Find[V]]
    first(First(), r => _find(r.asInstanceOf[KVPair[K, V]], req.f, rf))
  }

  private def afind(rsp: KVPair[K, V], f: V => Boolean, rf: Any => Unit) {
    _find(rsp, f, rf)
  }

  @tailrec private def _find(rsp: KVPair[K, V], f: V => Boolean, rf: Any => Unit) {
    if (rsp == null) {
      rf(null)
      return
    }
    var rsp1: KVPair[K, V] = null
    var async = false
    var sync = false
    if (f(rsp.value)) {
      rf(rsp.value)
      return
    }
    next(Next(rsp.key), r => {
      rsp1 = r.asInstanceOf[KVPair[K, V]]
      if (async) afind(rsp1, f, rf)
      else sync = true
    })
    if (!sync) {
      async = true
      return
    }
    _find(rsp1, f, rf)
  }
}
