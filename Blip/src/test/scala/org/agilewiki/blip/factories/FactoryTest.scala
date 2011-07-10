package org.agilewiki.blip
package factories

import org.specs.SpecificationWithJUnit

abstract class UserFactory(id: FactoryId) extends Factory(id) {
  def accountName: String
  override protected def instantiate(mailbox: Mailbox) = {
    val actor = new UserActor(mailbox, this)
    actor.singleton
    actor
  }
}

class FredFactory extends UserFactory(FactoryId("Fred")) {
  override def accountName = "fredforall"
}

case class AccountName()

class UserActor(mailbox: Mailbox, userFactory: UserFactory) extends Actor(mailbox, userFactory) {
  bind(classOf[AccountName], accountName)
  private def accountName(msg: AnyRef, rf: Any => Unit) {rf(userFactory.accountName)}
}

class FactoryTest extends SpecificationWithJUnit {
  "FactoryTest" should {
    "create and configure" in {
      val fred = (new FredFactory).newActor(null)
      println("actor id = " + fred.id.value)
      println("account name = " + Future(fred, AccountName()))
    }
  }
}
