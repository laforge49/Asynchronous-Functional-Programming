package org.agilewiki.blip
package bind
package dualEchoTiming

import annotation.tailrec

case class DoIt(c: Int, b: Int)

class Driver(sender1: Sender, sender2: Sender)
  extends BindActor {

  var c = 0
  var b = 0
  var i = 0
  var t0 = 0L
  var batch: Batch = null

  bind(classOf[DoIt], doIt)

  def doIt(msg: AnyRef, rf: Any => Unit) {
    val req = msg.asInstanceOf[DoIt]
    c = req.c
    i = c - 1
    b = req.b
    batch = Batch(b)
    t0 = System.currentTimeMillis
    sender1(batch) {
      rsp => processResponse(sender1, rf)
    }
    sender2(batch) {
      rsp => processResponse(sender2, rf)
    }
  }

  private def dummy(sender: Sender, rf: Any => Unit) {
    processResponse(sender, rf)
  }

  @tailrec private def processResponse(sender: Sender, rf: Any => Unit) {
    if (i < 1) {
      val t1 = System.currentTimeMillis
      if (t1 != t0) println("msgs per sec = " + (c * b * 2L * 1000L / (t1 - t0)))
      rf(null)
      return
    }
    var async = false
    var sync = false
    i -= 1
    sender(batch) {
      msg => {
        if (async) dummy(sender, rf)
        else sync = true
      }
    }
    if (!sync) {
      async = true
      return
    }
    processResponse(sender, rf)
  }
}
