package org.agilewiki.blip
package bind
package burstTiming

case class Ping()

class Echo
  extends BindActor {

  bind(classOf[Ping], ping)

  def ping(msg: AnyRef, rf: Any => Unit) {
    rf(null)
  }
}
