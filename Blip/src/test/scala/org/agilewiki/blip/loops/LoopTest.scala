package org.agilewiki.blip
package loops

import org.specs.SpecificationWithJUnit

case class Loop(n: Int)

case class I(i: Int)

class P extends Actor {
  bind(classOf[I], ifunc)

  def ifunc(msg: AnyRef, rf: Any => Unit) {
    println(msg.asInstanceOf[I].i)
    rf(null)
  }
}

class L(a: Actor) extends Actor {
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
      a(I(i)) {
        rsp =>
          r += 1
          if (r == n) rf(null)
      }
    }
  }
}

class LoopTest extends SpecificationWithJUnit {
  "LoopTest" should {
    "print 1, 2, 3 twice" in {
      val mailboxFactory = new MailboxFactory
      try {
        val mb = mailboxFactory.asyncMailbox
        val p = new P
        p.setMailbox(mb)
        println("synchronous test")
        val sl = new L(p)
        sl.setMailbox(mb)
        Future(sl, Loop(3))
        println("asynchronous test")
        val al = new L(p)
        al.setMailbox(mailboxFactory.asyncMailbox)
        Future(al, Loop(3))
      } finally {
        mailboxFactory.close
      }
    }
  }
}
