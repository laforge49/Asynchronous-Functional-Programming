package org.agilewiki.blip
package bind
package burstTiming

import annotation.tailrec

case class DoIt(c: Int, b: Int)

class Sender(echo: Echo)
  extends BindActor {

  var count = 0
  var i = 0
  var burst = 0
  var j = 0
  var r = 0
  var t0 = 0L

  bind(classOf[DoIt], doIt)

  def doIt(msg: AnyRef, rf: Any => Unit) {
    val req = msg.asInstanceOf[DoIt]
    count = req.c
    burst = req.b
    i = req.c
    t0 = System.currentTimeMillis
    echo(Ping()) {
      rsp => processResponse(rf)
    }

    rf(null)
  }

  private def dummy(rf: Any => Unit) {
    processResponse(rf)
  }

  @tailrec private def processResponse(rf: Any => Unit) {
    if (r < 2 && i < 1) {
      val t1 = System.currentTimeMillis
      if (t1 != t0) println("msgs per sec = " +
        (count * burst * 2L * 1000L / (t1 - t0)))
      rf(null)
      return
    }
    if (r > 1 && j < 1) {
      r -= 1
      return
    }
    if (j < 1) {
      i -= 1
      j = burst
      r = burst
    } else r -= 1
    var async = true
    var sync = false
    while (j > 0 && async) {
      j -= 1
      async = false
      echo(Ping()) {
        rsp => {
          if (async) {
            dummy(rf)
          } else {
            sync = true
          }
        }
      }
      if (!sync) {
        async = true
      }
    }
    if (async) return
    processResponse(rf)
  }
}
