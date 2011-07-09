package org.agilewiki.blip
package exceptions

import org.specs.SpecificationWithJUnit

case class SE()
case class SNE()

class S(mailbox: Mailbox) extends Actor(mailbox, null) {
  bind(classOf[SE], se)
  private def se(msg: AnyRef, rf: Any => Unit) {
    throw new IllegalStateException
  }
  bind(classOf[SNE], sne)
  private def sne(msg: AnyRef, rf: Any => Unit) {rf(null)}
}

case class AsyncServerEx()

class D extends Actor(new Mailbox, null) {
  bind(classOf[AsyncServerEx],{(msg, rf) =>
    exceptionHandler(msg, rf, asyncServerEx){exceptionHandler =>
      println("got exception")
      rf(null)
    }
  })
  def asyncServerEx(msg: AnyRef, rf: Any => Unit) {
    val s = new S(new Mailbox)
    s(SE())(rf)
  }
}

class ExceptionTest extends SpecificationWithJUnit {
  "ExceptionTest" should {
    "exercise exception handlers" in {
      println("--no handler async test")
      Future(new D, AsyncServerEx())
    }
  }
}
