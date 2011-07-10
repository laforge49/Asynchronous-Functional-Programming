package org.agilewiki.blip
package factories

import org.specs.SpecificationWithJUnit

abstract class SomeFactory(id: FactoryId) extends Factory(id) {
  def accountName: String
  override def instantiate(mailbox: Mailbox) = new SomeActor(mailbox, this)
}

class FredFactory extends SomeFactory(FactoryId("Fred")) {
  override def accountName = "fredforall"
}

case class AccountName()

class SomeActor(mailbox: Mailbox, someFactory: SomeFactory) extends Actor(mailbox, someFactory) {
  id(ActorId(factory.id.value))
  bind(classOf[AccountName], accountName)
  private def accountName(msg: AnyRef, rf: Any => Unit) {rf(someFactory.accountName)}
}

class FactoryTest extends SpecificationWithJUnit {
  "FactoryTest" should {
    "create and configure" in {
      val someActor = (new FredFactory).instantiate(null)
      println("actor id = " + someActor.id.value)
      println("account name = " + Future(someActor, AccountName()))
    }
  }
}
