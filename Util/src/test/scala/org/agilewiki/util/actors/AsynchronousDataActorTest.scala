package org.agilewiki
package util
package actors

import nonblocking._NonBlocking
import org.specs.SpecificationWithJUnit

class AsynchronousDataActorTest extends SpecificationWithJUnit {
  "adat" should {
    "print" in {
      val properties = _NonBlocking.defaultConfiguration("FUN")
      val systemContext = new _NonBlocking(properties)

      val ada = AsynchronousDataActor(systemContext, "fudge")
      val future = new InternalAddressFuture(systemContext)
      ada ! new DataRequestMsg(future, (context, actor, data) => {
        println(data)
        future ! "done"
      })
      println(future.get)
    }
  }
}