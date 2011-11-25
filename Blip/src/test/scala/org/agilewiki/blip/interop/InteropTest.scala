package org.agilewiki.blip
package interop

import org.specs.SpecificationWithJUnit
import bind._
import actors.{ReplyReactor, Reactor}
import exchange._
import java.util.concurrent.Semaphore

case class SimpleEcho(value: String)

case class QueryEcho(value: String)

case class Ex()

class SimpleActor extends Actor {
  bind(classOf[SimpleEcho], simpleEcho)
  bindMessageLogic(classOf[QueryEcho], new Query(queryEcho))
  bind(classOf[Ex], ex)

  private def simpleEcho(msg: AnyRef, rf: Any => Unit) {
    rf(msg.asInstanceOf[SimpleEcho].value)
  }

  private def queryEcho(msg: AnyRef, rf: Any => Unit) {
    rf(msg.asInstanceOf[QueryEcho].value)
  }

  private def ex(msg: AnyRef, rf: Any => Unit) {
    throw new IllegalArgumentException
  }
}

case class T1()

case class T2()

case class T3()

case class T4()

class SimpleReactor(systemServices: SystemServices) extends Reactor[Any] {
  val interop = new Interop(this)
  val simpleActor = new SimpleActor
  val semaphore = new Semaphore(0)
  simpleActor.setExchangeMessenger(systemServices.newSyncMailbox)

  start

  override def act {
    loop {
      react {
        case afpResponse: ExchangeMessengerResponse => interop.afpResponse(afpResponse)
        case req: T1 => {
          println("a")
          semaphore.release
        }
        case req: T2 => {
          interop.afpSend(simpleActor, SimpleEcho("b")) {
            rsp => {
              println(rsp)
              semaphore.release
            }
          }
        }
        case req: T3 => {
          interop.afpSend(simpleActor, QueryEcho("c")) {
            rsp => {
              println(rsp)
              semaphore.release
            }
          }
        }
        case req: T4 => {
          interop.afpSend(simpleActor, Ex()) {
            rsp => {
              println(rsp.asInstanceOf[Exception].getClass.getName)
              semaphore.release
            }
          }
        }
      }
    }
  }
}

class InteropTest extends SpecificationWithJUnit {
  "SimpleActor" should {
    "print" in {
      val systemServices = SystemServices()
      try {
        val simpleReactor = new SimpleReactor(systemServices)
        simpleReactor ! T1()
        simpleReactor.semaphore.acquire
        simpleReactor ! T2()
        simpleReactor.semaphore.acquire
        simpleReactor ! T3()
        simpleReactor.semaphore.acquire
        simpleReactor ! T4()
        simpleReactor.semaphore.acquire
      } finally {
        systemServices.close
      }
    }
  }
}
