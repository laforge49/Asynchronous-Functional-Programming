package org.agilewiki.blip
package intro.async

import org.specs.SpecificationWithJUnit
import bind._

case class Pause()

class Worker extends Actor {
  bind(classOf[Pause], pause)

  def pause(msg: AnyRef, rf: Any => Unit) {
    Thread.sleep(100)
    rf(null)
  }
}

object Pause {
  def apply(rf: Any => Unit)
           (implicit srcActor: ActiveActor) {
    val worker = new Worker
    worker.setExchangeMessenger(srcActor.bindActor.newAsyncMailbox)
    worker(Pause())(rf)
  }
}

case class Doit()

class Driver extends Actor {
  bind(classOf[Doit], doit)
  var rem = 6
  var rf: Any => Unit = null
  var t0 = 0L

  def doit(msg: AnyRef, _rf: Any => Unit) {
    rf = _rf
    t0 = System.currentTimeMillis
    Pause(r)
    Pause(r)
    Pause(r)
    Pause(r)
    Pause(r)
    Pause(r)
  }

  def r(rsp: Any) {
    rem -= 1
    if (rem == 0) {
      val t1 = System.currentTimeMillis
      println("total time = "+(t1 - t0)+" milliseconds")
      rf(null)
    }
  }
}

class AsyncTest extends SpecificationWithJUnit {
  "AsyncTest" should {
    "process in parallel" in {
      val systemServices = SystemServices()
      try {
        val driver = new Driver
        driver.setExchangeMessenger(systemServices.newAsyncMailbox)
        Future(driver, Doit())
      } finally {
        systemServices.close
      }
    }
    /*
    Output:
    total time = 125 milliseconds
     */
  }
}
