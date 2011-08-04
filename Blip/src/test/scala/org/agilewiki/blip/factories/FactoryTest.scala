package org.agilewiki.blip
package factories

import org.specs.SpecificationWithJUnit

abstract class UserFactory(id: FactoryId) extends Factory(id) {
  def accountName: String
  override protected def instantiate = new UserActor
}

class FredFactory extends UserFactory(FactoryId("Fred")) {
  override def accountName = "fredforall"
}

case class AccountName()

class UserActor
  extends Actor {
  bind(classOf[AccountName], accountName)

  private def accountName(msg: AnyRef, rf: Any => Unit) {
    rf(factory.asInstanceOf[UserFactory].accountName)
  }
}

class FactoryTest extends SpecificationWithJUnit {
  "FactoryTest" should {
    "create and configure" in {
      val fred = (new FredFactory).newActor(null)
      println("account name = " + Future(fred, AccountName()))
    }
  }
}
