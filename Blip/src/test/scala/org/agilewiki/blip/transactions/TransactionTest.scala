package org.agilewiki.blip
package transactions

import bind._
import org.specs.SpecificationWithJUnit

case class Pause()

class Worker extends AsyncActor {
  bind(classOf[Pause], pause)

  def pause(msg: AnyRef, rf: Any => Unit) {
    Thread.sleep(30)
    rf(null)
  }
}

object Pause {
  def apply(systemServices: SystemServices)
           (rf: Any => Unit)
           (implicit srcActor: ActiveActor) {
    val worker = new Worker
    worker.setSystemServices(systemServices)
    worker(Pause())(rf)
  }
}

case class SimpleQuery(name: String)

case class SimpleUpdate(name: String)

class SimpleTransactionProcessor extends AsyncActor {
  bindMessageLogic(classOf[SimpleQuery], new Query(query))
  bindMessageLogic(classOf[SimpleUpdate], new Update(update))

  def query(msg: AnyRef, rf: Any => Unit) {
    val name = msg.asInstanceOf[SimpleQuery].name
    println("start query " + name)
    Pause(systemServices) {
      rsp => {
        println("  end query " + name)
        rf(null)
      }
    }
  }

  def update(msg: AnyRef, rf: Any => Unit) {
    val name = msg.asInstanceOf[SimpleUpdate].name
    println("start update " + name)
    Pause(systemServices) {
      rsp => {
        println("  end update " + name)
        rf(null)
      }
    }
  }
}

case class Doit()

class Driver extends AsyncActor {
  bind(classOf[Doit], doit)

  lazy val simpleTransactionProcessor = {
    val stp = new SimpleTransactionProcessor
    stp.setSystemServices(systemServices)
    stp
  }

  def doit(msg: AnyRef, rf: Any => Unit) {
    var rem = 6
    simpleTransactionProcessor(SimpleQuery("1")) {
      rsp1 => {
        rem -= 1
        if (rem == 0) rf(null)
      }
    }
    simpleTransactionProcessor(SimpleQuery("2")) {
      rsp1 => {
        rem -= 1
        if (rem == 0) rf(null)
      }
    }
    simpleTransactionProcessor(SimpleUpdate("3")) {
      rsp1 => {
        rem -= 1
        if (rem == 0) rf(null)
      }
    }
    simpleTransactionProcessor(SimpleUpdate("4")) {
      rsp1 => {
        rem -= 1
        if (rem == 0) rf(null)
      }
    }
    simpleTransactionProcessor(SimpleQuery("5")) {
      rsp1 => {
        rem -= 1
        if (rem == 0) rf(null)
      }
    }
    simpleTransactionProcessor(SimpleQuery("6")) {
      rsp1 => {
        rem -= 1
        if (rem == 0) rf(null)
      }
    }
  }
}

class TransactionTest extends SpecificationWithJUnit {
  "TransactionTest" should {
    "process transactions" in {
      val systemServices = SystemServices()
      try {
        val driver = new Driver
        driver.setSystemServices(systemServices)
        Future(driver, Doit())
      } finally {
        systemServices.close
      }
    }
  }
}