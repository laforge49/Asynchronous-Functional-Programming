package org.agilewiki.blip
package interop

import org.specs.SpecificationWithJUnit
import actors.{ReplyReactor, Reactor}

case class SimpleEcho(value: String)

case class QueryEcho(value: String)

case class Ex()

class SimpleActor extends Actor {
  bind(classOf[SimpleEcho], simpleEcho)
  bindSafe(classOf[QueryEcho], new Query(queryEcho))
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
  simpleActor.setMailbox(systemServices.newSyncMailbox)

  start

  override def act {
    loop {
      react {
        case afpResponse: MailboxRsp => interop.afpResponse(afpResponse)
        case req: T1 => println("a")
        case req: T2 => {
          interop.afpSend(simpleActor, SimpleEcho("b")) {
            rsp => println(rsp)
          }
        }
        case req: T3 => {
          interop.afpSend(simpleActor, QueryEcho("c")) {
            rsp => println(rsp)
          }
        }
        case req: T4 => {
          interop.afpSend(simpleActor, Ex()) {
            rsp => {
              println(rsp.asInstanceOf[Exception].getClass.getName)
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
        simpleReactor ! T2()
        simpleReactor ! T3()
        simpleReactor ! T4()
      } finally {
        systemServices.close
      }
    }
  }
}
