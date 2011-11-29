package org.agilewiki.blip
package exceptions

import org.specs.SpecificationWithJUnit
import bind._

case class SE()

case class SNE()

class S extends Actor {
  bind(classOf[SE], se)

  private def se(msg: AnyRef, rf: Any => Unit) {
    throw new IllegalStateException
  }

  bind(classOf[SNE], exh(se))

  def exh(mf: (AnyRef, Any => Unit) => Unit)(msg: AnyRef, rf: Any => Unit) {
    exceptionHandler(msg, rf, mf) {
      (ex, mailbox) =>
        println("S got exception " + ex.toString)
        rf(null)
    }
  }
}

case class AsyncServerEx()

case class SyncServerEx()

case class AsyncRspEx()

case class SyncRspEx()

class D extends Actor {
  bind(classOf[AsyncServerEx], {
    (msg, rf) =>
      exceptionHandler(msg, rf, asyncServerEx) {
        (ex, mailbox) =>
          println("D got exception " + ex.toString)
          rf(null)
      }
  })

  def asyncServerEx(msg: AnyRef, rf: Any => Unit) {
    val s = new S
    s.setExchangeMessenger(newAsyncMailbox)
    println("ase "+exchangeMessenger.curReq)
    s(SE())(rsp => rf)
  }

  bind(classOf[SyncServerEx], {
    (msg, rf) =>
      exceptionHandler(msg, rf, syncServerEx) {
        (ex, mailbox) =>
          println("D got exception " + ex.toString)
          rf(null)
      }
  })

  def syncServerEx(msg: AnyRef, rf: Any => Unit) {
    val s = new S
    s.setExchangeMessenger(exchangeMessenger)
    s(SE())(rsp => rf)
  }

  bind(classOf[AsyncRspEx], {
    (msg, rf) =>
      exceptionHandler(msg, rf, asyncRspEx) {
        (ex, mailbox) =>
          println("D got exception " + ex.toString)
          rf(null)
      }
  })

  def asyncRspEx(msg: AnyRef, rf: Any => Unit) {
    val s = new S
    s.setExchangeMessenger(newAsyncMailbox)
    s(SNE()) {
      rsp => throw new IllegalAccessException
    }
  }

  bind(classOf[SyncRspEx], {
    (msg, rf) =>
      exceptionHandler(msg, rf, syncRspEx) {
        (ex, mailbox) =>
          println("D got exception " + ex.toString)
          rf(null)
      }
  })

  def syncRspEx(msg: AnyRef, rf: Any => Unit) {
    val s = new S
    s.setExchangeMessenger(exchangeMessenger)
    s(SNE()) {
      rsp => throw new IllegalAccessException
    }
  }
}

class ExceptionTest extends SpecificationWithJUnit {
  "ExceptionTest" should {
    "exercise exception handlers" in {
      val systemServices = SystemServices()
      try {
        val d = new D
        d.setExchangeMessenger(systemServices.newSyncMailbox)
        println("--server exception async test")
        Future(d, AsyncServerEx())
        println("--server exception sync test")
        Future(d, SyncServerEx())
        println("--response exception async test")
        Future(d, AsyncRspEx())
        println("--response exception sync test")
        Future(d, SyncRspEx())
      } finally {
        systemServices.close
      }
    }
    /*
    Output:

    --server exception async test
    ase org.agilewiki.blip.MailboxReq@b29b4a
    D got exception java.lang.IllegalStateException
    --server exception sync test
    D got exception java.lang.IllegalStateException
    --response exception async test
    S got exception java.lang.IllegalStateException
    D got exception java.lang.IllegalAccessException
    --response exception sync test
    S got exception java.lang.IllegalStateException
    D got exception java.lang.IllegalAccessException

     */
  }
}
