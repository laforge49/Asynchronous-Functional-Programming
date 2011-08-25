package org.agilewiki
package transactions

import blip._
import org.specs.SpecificationWithJUnit

case class Pause()

class Worker
  extends Actor {
  bind(classOf[Pause], pause)

  def pause(msg: AnyRef, rf: Any => Unit) {
    Thread.sleep(30)
    rf(null)
  }
}

object Pause {
  def apply(rf: Any => Unit)(implicit srcActor: ActiveActor) {
    val worker = new Worker
    worker.setMailbox(new Mailbox)
    worker(Pause())(rf)
  }
}

case class SimpleQuery(name: String)

case class SimpleUpdate(name: String)

class SimpleTransactionProcessor
  extends TransactionProcessor {
  bindSafe(classOf[SimpleQuery], new Query(query))
  bindSafe(classOf[SimpleUpdate], new Update(update))

  def query(msg: AnyRef, rf: Any => Unit) {
    val name = msg.asInstanceOf[SimpleQuery].name
    println("start query " + name)
    Pause{
      rsp => {
        println("  end query " + name)
        rf(null)
      }
    }
  }

  def update(msg: AnyRef, rf: Any => Unit) {
    val name = msg.asInstanceOf[SimpleUpdate].name
    println("start update " + name)
    Pause{
      rsp => {
        println("  end update " + name)
        rf(null)
      }
    }
  }
}

case class Doit()

class Driver
  extends Actor {
  bind(classOf[Doit], doit)

  lazy val simpleTransactionProcessor = {
    val stp = new SimpleTransactionProcessor
    stp.setMailbox(new Mailbox)
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
      val driver = new Driver
      driver.setMailbox(new Mailbox)
      Future(driver, Doit())
    }
  }
}