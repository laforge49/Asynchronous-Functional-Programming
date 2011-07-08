package org.agilewiki.blip
package recursion

import org.specs.SpecificationWithJUnit
import annotation.tailrec

case class Loop(n: Int)
case class I(i: Int)

class P(mb: Mailbox) extends Actor(mb, null) {
  bind (classOf[I], ifunc)
  def ifunc(msg: AnyRef, rf: Any => Unit) {
    println(msg.asInstanceOf[I].i)
    rf(null)
  }
}

class L(mb: Mailbox, a: Actor) extends Actor(mb, null) {
  bind(classOf[Loop], lfunc)
  def lfunc(msg: AnyRef, rf: Any => Unit) {
    val n = msg.asInstanceOf[Loop].n
    if (n < 1) {
      rf(null)
      return
    }
    lrec(n, 0, rf)
  }

  @tailrec private def lrec(n: Int, _i: Int, rf: Any => Unit) {
    var async = false
    var sync = false
    val i = _i + 1
    a(I(i)) {rsp =>
      if (i == n) rf(null)
      else if (async) _lrec(n, i, rf)
      else sync = true
    }
    if (!sync) {
      async = true
      return
    }
    lrec(n, i, rf)
  }

  def _lrec(n: Int, i: Int, rf: Any => Unit) {lrec(n, i, rf)}
}

class RecursionTest extends SpecificationWithJUnit {
  "RecursionTest" should {
    "print 1, 2, 3 twice" in {
      val mb = new Mailbox
      val p = new P(mb)
      println("synchronous test")
      Future(new L(mb, p), Loop(3))
      println("asynchronous test")
      Future(new L(new Mailbox, p), Loop(3))
    }
  }
}
