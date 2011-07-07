package org.agilewiki.blip
package operatingModes

import org.specs.SpecificationWithJUnit

case class AMessage()

class A(mb: Mailbox, sub: Actor) extends Actor(mb, null) {
  bind(classOf[AMessage], afunc)
  def afunc(msg: AnyRef, rf: Any => Unit)
  {
    println("start afunc")
    sub(msg) {
      rsp =>
        println("got result")
        rf("all done")
    }
    println("end afunc")
  }
}

class B(mb: Mailbox) extends Actor(mb, null) {
  bind(classOf[AMessage], bfunc)
  def bfunc(msg: AnyRef, rf: Any => Unit)
  {
    rf("ta ta")
  }
}

class BimodalTest extends SpecificationWithJUnit {
  "Bimodal" should {
    "print differently" in {
      val mb1 = new Mailbox
      val b = new B(mb1)
      val mb2 = new Mailbox
      println("synchronous test")
      Future(new A(mb1, b), AMessage())
      println("asynchronous test")
      Future(new A(mb2, b), AMessage())
    }
  }
}
