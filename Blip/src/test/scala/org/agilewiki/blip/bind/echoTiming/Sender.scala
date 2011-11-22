package org.agilewiki.blip
package bind
package echoTiming

import annotation.tailrec

case class DoIt(c: Int)

class Sender(echo: Echo)
  extends BindActor {

  var count = 0
  var i = 0
  var t0 = 0L
  t0 = System.currentTimeMillis

  bind(classOf[DoIt], doIt)

  def doIt(msg: AnyRef, rf: Any => Unit) {
    val req = msg.asInstanceOf[DoIt]
    count = req.c
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
    if (i < 1) {
      val t1 = System.currentTimeMillis
      if (t1 != t0) println("msgs per sec = " + (count * 2L * 1000L / (t1 - t0)))
      rf(null)
      return
    }
    var async = false
    var sync = false
    i -= 1
    echo(Ping()) {
      msg => {
        if (async) dummy(rf)
        else sync = true
      }
    }
    if (!sync) {
      async = true
      return
    }
    processResponse(rf)
  }
}
