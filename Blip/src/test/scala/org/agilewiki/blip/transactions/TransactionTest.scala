package org.agilewiki.blip
package transactions

import bind._
import org.specs.SpecificationWithJUnit

case class Pause()

class Worker extends Actor {
  bind(classOf[Pause], pause)

  def pause(msg: AnyRef, rf: Any => Unit) {
    Thread.sleep(30)
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

case class SimpleQuery(name: String)

case class SimpleUpdate(name: String)

class SimpleTransactionProcessor extends Actor {
  bindMessageLogic(classOf[SimpleQuery], new Query(query))
  bindMessageLogic(classOf[SimpleUpdate], new Update(update))

  def query(msg: AnyRef, rf: Any => Unit) {
    val name = msg.asInstanceOf[SimpleQuery].name
    println("start query " + name)
    Pause {
      rsp => {
        println("  end query " + name)
        rf(null)
      }
    }
  }

  def update(msg: AnyRef, rf: Any => Unit) {
    val name = msg.asInstanceOf[SimpleUpdate].name
    println("start update " + name)
    Pause {
      rsp => {
        println("  end update " + name)
        rf(null)
      }
    }
  }
}

case class Doit()

class Driver extends Actor {
  bind(classOf[Doit], doit)

  lazy val simpleTransactionProcessor = {
    val stp = new SimpleTransactionProcessor
    stp.setExchangeMessenger(newAsyncMailbox)
    stp
  }

  var rem = 0
  var rf: Any => Unit = null

  def r(rsp: Any) {
    rem -= 1
    if (rem == 0) rf(null)
  }

  def doit(msg: AnyRef, _rf: Any => Unit) {
    rf = _rf
    rem = 6
    simpleTransactionProcessor(SimpleQuery("1"))(r)
    simpleTransactionProcessor(SimpleQuery("2"))(r)
    simpleTransactionProcessor(SimpleUpdate("3"))(r)
    simpleTransactionProcessor(SimpleUpdate("4"))(r)
    simpleTransactionProcessor(SimpleQuery("5"))(r)
    simpleTransactionProcessor(SimpleQuery("6"))(r)
  }
}

class TransactionTest extends SpecificationWithJUnit {
  "TransactionTest" should {
    "process transactions" in {
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
    start query 1
    start query 2
      end query 2
      end query 1
    start update 3
      end update 3
    start update 4
      end update 4
    start query 5
    start query 6
      end query 5
      end query 6
     */
  }
}