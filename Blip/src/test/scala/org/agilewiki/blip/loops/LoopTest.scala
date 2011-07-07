package org.agilewiki.blip
package loops

import org.specs.SpecificationWithJUnit

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
    var i = 0
    var r = 0
    while (i < n) {
      i += 1
      a(I(i)) { rsp =>
        r += 1
        if (r == n) rf(null)
      }
    }
  }
}

class LoopTest extends SpecificationWithJUnit {
  "LoopTest" should {
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
