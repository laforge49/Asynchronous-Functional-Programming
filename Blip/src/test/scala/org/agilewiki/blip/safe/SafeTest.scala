package org.agilewiki.blip
package safe

import org.specs.SpecificationWithJUnit

case class Print(value: Any)

case class PrintEven(value: Int)

case class AMsg()

class Driver extends Actor {
  bind(classOf[AMsg], aMsgFunc)
  setMailbox(new Mailbox)

  private def aMsgFunc(msg: AnyRef, rf: Any => Unit) {
    val safeActor = new SafeActor
    safeActor(PrintEven(1)){rsp =>
      safeActor(PrintEven(2)){rsp => rf(null)}
    }
  }
}

case class SafePrintEven(safeActor: SafeActor)
  extends Safe {
  def func(msg: AnyRef, rf: Any => Unit)(implicit sender: ActiveActor) {
    val printEven = msg.asInstanceOf[PrintEven]
    val value = printEven.value
    if (value % 2 == 0) safeActor(Print(value))(rf)
    else rf(null)
  }
}

class SafeActor extends Actor {
  bind(classOf[Print], printFunc)
  setMailbox(new Mailbox)

  private def printFunc(msg: AnyRef, rf: Any => Unit) {
    println(msg.asInstanceOf[Print].value)
    rf(null)
  }

  bindSafe(classOf[PrintEven], SafePrintEven(this))
}

class SafeTest extends SpecificationWithJUnit {
  "SafeTest" should {
    "print even numbers" in {
      val driver = new Driver
      Future(driver, AMsg())
    }
  }
}
