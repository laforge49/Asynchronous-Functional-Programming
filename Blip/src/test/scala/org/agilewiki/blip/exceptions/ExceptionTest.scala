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
case class SyncServerEx()
case class AsyncRspEx()
case class SyncRspEx()

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

  bind(classOf[SyncServerEx],{(msg, rf) =>
    exceptionHandler(msg, rf, syncServerEx){exceptionHandler =>
      println("got exception")
      rf(null)
    }
  })
  def syncServerEx(msg: AnyRef, rf: Any => Unit) {
    val s = new S(mailbox)
    s(SE())(rf)
  }

  bind(classOf[AsyncRspEx],{(msg, rf) =>
    exceptionHandler(msg, rf, asyncRspEx){exceptionHandler =>
      println("got exception")
      rf(null)
    }
  })
  def asyncRspEx(msg: AnyRef, rf: Any => Unit) {
    val s = new S(new Mailbox)
    s(SNE()){throw new IllegalAccessException}
  }

  bind(classOf[SyncRspEx],{(msg, rf) =>
    exceptionHandler(msg, rf, syncRspEx){exceptionHandler =>
      println("got exception")
      rf(null)
    }
  })
  def syncRspEx(msg: AnyRef, rf: Any => Unit) {
    val s = new S(mailbox)
    s(SNE()){throw new IllegalAccessException}
  }
}

class ExceptionTest extends SpecificationWithJUnit {
  "ExceptionTest" should {
    "exercise exception handlers" in {
      println("--server exception async test")
      Future(new D, AsyncServerEx())
      println("--server exception sync test")
      Future(new D, SyncServerEx())
      println("--response exception async test")
      Future(new D, AsyncRspEx())
      println("--response exception sync test")
      Future(new D, SyncRspEx())
    }
  }
}
