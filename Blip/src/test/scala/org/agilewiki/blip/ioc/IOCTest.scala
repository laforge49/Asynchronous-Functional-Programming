package org.agilewiki.blip
package ioc

import org.specs.SpecificationWithJUnit

case class Today()

class Sayings(actor: Actor, componentFactory: ComponentFactory) extends Component(actor, componentFactory){
  bind(classOf[Today], today)
  def today(msg: AnyRef, rf: Any => Unit) {rf("Today is the first day of the rest of your life.")}
}

class SayingsFactory extends ComponentFactory {
  override protected def instantiate(actor: Actor) = new Sayings(actor, this)
}

case class SaySomething()

class SayIt extends Actor(new Mailbox, null) {
  bind(classOf[SaySomething], saySomething)
  def saySomething(msg: AnyRef, rf: Any => Unit) {
    systemServices(Today())(rf)
  }
}

class IOCTest extends SpecificationWithJUnit {
  "SimpleActor" should {
    "print" in {
      val systemServices = SystemServices(new SayingsFactory)
      val sayIt = new SayIt
      sayIt.setSystemServices(systemServices)
      println(Future(sayIt, SaySomething()))
    }
  }
}