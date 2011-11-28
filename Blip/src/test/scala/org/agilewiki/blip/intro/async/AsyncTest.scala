package org.agilewiki.blip
package intro.async

import org.specs.SpecificationWithJUnit
import bind._

case class Pause()

class Worker extends Actor {
  bind(classOf[Pause], pause)

  def pause(msg: AnyRef, rf: Any => Unit) {
    Thread.sleep(200)
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

case class Doit(c: Int)

class Driver extends Actor {
  bind(classOf[Doit], doit)
  var rem = 0
  var c = 0
  var rf: Any => Unit = null
  var t0 = 0L

  def doit(msg: AnyRef, _rf: Any => Unit) {
    c = msg.asInstanceOf[Doit].c
    rem = c
    rf = _rf
    t0 = System.currentTimeMillis
    var i = 0
    while(i < c) {
      i += 1
      Pause(r)
    }
  }

  def r(rsp: Any) {
    rem -= 1
    if (rem == 0) {
      val t1 = System.currentTimeMillis
      println("total time for "+c+" messages = "+(t1 - t0)+" milliseconds")
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
        driver.setExchangeMessenger(systemServices.newSyncMailbox)
        Future(driver, Doit(10))
        Future(driver, Doit(20))
      } finally {
        systemServices.close
      }
    }
    /*
    Output:
    total time for 10 messages = 208 milliseconds
    total time for 20 messages = 401 milliseconds     */
  }
}
