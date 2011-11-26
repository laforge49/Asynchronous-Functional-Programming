package org.agilewiki.blip
package intro.loopTiming

case class GetAndReset()

case class AddCount(number: Long)

class CounterActor extends Actor {
  var count = 0L

  bind(classOf[GetAndReset], getAndReset)
  bind(classOf[AddCount], addCount)

  def getAndReset(msg: AnyRef, rf: Any => Unit) {
    val current = count
    count = 0L
    rf(current)
  }

  def addCount(msg: AnyRef, rf: Any => Unit) {
    count += msg.asInstanceOf[AddCount].number
    rf(null)
  }
}