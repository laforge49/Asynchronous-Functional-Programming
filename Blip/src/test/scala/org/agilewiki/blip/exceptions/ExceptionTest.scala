package org.agilewiki.blip
package exceptions

import org.specs.SpecificationWithJUnit

case class SE()

case class SNE()

class S extends Actor {
  bind(classOf[SE], se)

  private def se(msg: AnyRef, rf: Any => Unit) {
    throw new IllegalStateException
  }

  bind(classOf[SNE], {
    (msg, rf) =>
      exceptionHandler(msg, rf, se) {
        ex =>
          println("S got exception " + ex.toString)
          rf(null)
      }
  })
}

case class AsyncServerEx()

case class SyncServerEx()

case class AsyncRspEx()

case class SyncRspEx()

class D extends Actor {
  bind(classOf[AsyncServerEx], {
    (msg, rf) =>
      exceptionHandler(msg, rf, asyncServerEx) {
        ex =>
          println("D got exception " + ex.toString)
          rf(null)
      }
  })

  def asyncServerEx(msg: AnyRef, rf: Any => Unit) {
    val s = new S
    s.setMailbox(new ReactorMailbox)
    s(SE())(rsp => rf)
  }

  bind(classOf[SyncServerEx], {
    (msg, rf) =>
      exceptionHandler(msg, rf, syncServerEx) {
        ex =>
          println("D got exception " + ex.toString)
          rf(null)
      }
  })

  def syncServerEx(msg: AnyRef, rf: Any => Unit) {
    val s = new S
    s.setMailbox(mailbox)
    s(SE())(rsp => rf)
  }

  bind(classOf[AsyncRspEx], {
    (msg, rf) =>
      exceptionHandler(msg, rf, asyncRspEx) {
        ex =>
          println("D got exception " + ex.toString)
          rf(null)
      }
  })

  def asyncRspEx(msg: AnyRef, rf: Any => Unit) {
    val s = new S
    s.setMailbox(new ReactorMailbox)
    s(SNE()) {
      rsp => throw new IllegalAccessException
    }
  }

  bind(classOf[SyncRspEx], {
    (msg, rf) =>
      exceptionHandler(msg, rf, syncRspEx) {
        ex =>
          println("D got exception " + ex.toString)
          rf(null)
      }
  })

  def syncRspEx(msg: AnyRef, rf: Any => Unit) {
    val s = new S
    s.setMailbox(mailbox)
    s(SNE()) {
      rsp => throw new IllegalAccessException
    }
  }
}

class ExceptionTest extends SpecificationWithJUnit {
  "ExceptionTest" should {
    "exercise exception handlers" in {
      val mailboxFactory = new MailboxFactory
      try {
        val d = new D
        d.setMailbox(mailboxFactory.asyncMailbox)
        println("--server exception async test")
        Future(d, AsyncServerEx())
        println("--server exception sync test")
        Future(d, SyncServerEx())
        println("--response exception async test")
        Future(d, AsyncRspEx())
        println("--response exception sync test")
        Future(d, SyncRspEx())
      } finally {
        mailboxFactory.close
      }
    }
  }
}
