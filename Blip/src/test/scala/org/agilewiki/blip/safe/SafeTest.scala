package org.agilewiki.blip
package safe

import org.specs.SpecificationWithJUnit

case class Print(value: Any)

case class PrintEven(value: Int)

case class AMsg()

class Driver extends Actor(new Mailbox, null) {
  bind(classOf[AMsg], aMsgFunc)

  private def aMsgFunc(msg: AnyRef, rf: Any => Unit) {
    val safeActor = new SafeActor
    safeActor(PrintEven(1)){rsp =>
      safeActor(PrintEven(2)){rsp => rf(null)}
    }
  }
}

class SafeActor extends Actor(new Mailbox, null) {
  bind(classOf[Print], printFunc)

  private def printFunc(msg: AnyRef, rf: Any => Unit) {
    println(msg.asInstanceOf[Print].value)
    rf(null)
  }

  bindSafe(classOf[PrintEven], printEvenFunc)

  private def printEvenFunc(msg: AnyRef, rf: Any => Unit, sender: ActiveActor) {
    val printEven = msg.asInstanceOf[PrintEven]
    val value = printEven.value
    if (value % 2 == 0) this(Print(value))(rf)(sender)
    else rf(null)
  }
}

class SafeTest extends SpecificationWithJUnit {
  "SafeTest" should {
    "print even numbers" in {
      val driver = new Driver
      Future(driver, AMsg())
    }
  }
}
