package org.agilewiki.blip
package safe

import bind._
import org.specs.SpecificationWithJUnit

case class Print(value: Any)

case class PrintEven(value: Int)

case class AMsg()

class Driver extends AsyncActor {
  bind(classOf[AMsg], aMsgFunc)

  private def aMsgFunc(msg: AnyRef, rf: Any => Unit) {
    val safeActor = new SafeActor
    safeActor.setSystemServices(systemServices)
    safeActor(PrintEven(1)){rsp =>
      safeActor(PrintEven(2)){rsp => rf(null)}
    }
  }
}

case class SafePrintEven(safeActor: SafeActor)
  extends MessageLogic {
  override def func(target: BindActor, msg: AnyRef, rf: Any => Unit)(implicit sender: ActiveActor) {
    val printEven = msg.asInstanceOf[PrintEven]
    val value = printEven.value
    if (value % 2 == 0) safeActor(Print(value))(rf)
    else rf(null)
  }
}

class SafeActor extends AsyncActor {
  bind(classOf[Print], printFunc)

  private def printFunc(msg: AnyRef, rf: Any => Unit) {
    println(msg.asInstanceOf[Print].value)
    rf(null)
  }

  bindSafe(classOf[PrintEven], SafePrintEven(this))
}

class SafeTest extends SpecificationWithJUnit {
  "SafeTest" should {
    "print even numbers" in {
      val systemServices = SystemServices()
      try {
        val driver = new Driver
        driver.setSystemServices(systemServices)
        Future(driver, AMsg())
      } finally {
        systemServices.close
      }
    }
  }
}
