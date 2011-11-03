package org.agilewiki.blip
package operatingModes

import org.specs.SpecificationWithJUnit

case class AMessage()

class A(sub: Actor) extends Actor {
  bind(classOf[AMessage], afunc)

  def afunc(msg: AnyRef, rf: Any => Unit) {
    println("start afunc")
    sub(msg) {
      rsp =>
        println("got result")
        rf("all done")
    }
    println("end afunc")
  }
}

class B extends Actor {
  bind(classOf[AMessage], bfunc)

  def bfunc(msg: AnyRef, rf: Any => Unit) {
    rf("ta ta")
  }
}

class BimodalTest extends SpecificationWithJUnit {
  "Bimodal" should {
    "print differently" in {
      val mailboxFactory = new MailboxFactory
      try {
        val mb1 = mailboxFactory.asyncMailbox
        val mb2 = mailboxFactory.asyncMailbox
        val b = new B
        b.setMailbox(mb1)
        println("synchronous test")
        val sa = new A(b)
        sa.setMailbox(mb1)
        Future(sa, AMessage())
        println("asynchronous test")
        val aa = new A(b)
        aa.setMailbox(mb2)
        Future(aa, AMessage())
      } finally {
        mailboxFactory.close
      }
    }
  }
}
