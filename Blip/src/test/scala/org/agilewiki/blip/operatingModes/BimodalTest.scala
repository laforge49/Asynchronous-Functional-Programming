package org.agilewiki.blip
package operatingModes

import org.specs.SpecificationWithJUnit
import bind._

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
    println("processing request")
    rf("ta ta")
  }
}

class BimodalTest extends SpecificationWithJUnit {
  "Bimodal" should {
    "print differently" in {
      val mailboxFactory = new MailboxFactory
      try {
        val mb1 = mailboxFactory.newAsyncMailbox
        val mb2 = mailboxFactory.newAsyncMailbox
        println("test 1 --synchronous")
        val b1 = new B
        b1.setExchangeMessenger(mb1)
        val a1 = new A(b1)
        a1.setExchangeMessenger(mb1)
        Future(a1, AMessage())
        println("test 2 --asynchronous")
        val b2 = new B
        b2.setExchangeMessenger(mb1)
        val a2 = new A(b2)
        a2.setExchangeMessenger(mb2)
        Future(a2, AMessage())
        val mb3 = mailboxFactory.newSyncMailbox
        println("test 3 --synchronous")
        val b3 = new B
        b3.setExchangeMessenger(mb3)
        val a3 = new A(b3)
        a3.setExchangeMessenger(mb1)
        Future(a3, AMessage())
      } finally {
        mailboxFactory.close
      }
    }
  }
}
