package org.agilewiki.blip
package factories

import org.specs.SpecificationWithJUnit

abstract class UserFactory(id: FactoryId) extends Factory(id) {
  def accountName: String
  override def instantiate(mailbox: Mailbox) = new UserActor(mailbox, this)
}

class FredFactory extends UserFactory(FactoryId("Fred")) {
  override def accountName = "fredforall"
}

case class AccountName()

class UserActor(mailbox: Mailbox, userFactory: UserFactory) extends Actor(mailbox, userFactory) {
  id(ActorId(factory.id.value))
  bind(classOf[AccountName], accountName)
  private def accountName(msg: AnyRef, rf: Any => Unit) {rf(userFactory.accountName)}
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
