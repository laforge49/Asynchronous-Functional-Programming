package org.agilewiki.blip
package recursion

import org.specs.SpecificationWithJUnit
import annotation.tailrec

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
    lrec(n, 0, rf)
  }

  @tailrec private def lrec(n: Int, _i: Int, rf: Any => Unit) {
    var async = false
    var sync = false
    val i = _i + 1
    a(I(i)) {
      rsp =>
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

  def _lrec(n: Int, i: Int, rf: Any => Unit) {
    lrec(n, i, rf)
  }
}

class RecursionTest extends SpecificationWithJUnit {
  "RecursionTest" should {
    "print 1, 2, 3 twice" in {
      val mailboxFactory = new MailboxFactory
      try {
        val mb = mailboxFactory.newAsyncMailbox
        val p = new P
        println("synchronous test")
        val sl = new L(p)
        sl.setMailbox(mb)
        Future(sl, Loop(3))
        println("asynchronous test")
        val al = new L(p)
        al.setMailbox(mailboxFactory.newAsyncMailbox)
        Future(al, Loop(3))
      } finally {
        mailboxFactory.close
      }
    }
  }
}
