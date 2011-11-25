package org.agilewiki.blip
package bind
package dualEchoTiming

import annotation.tailrec

case class Batch(c: Int)

class Sender(echo: Echo)
  extends BindActor {

  var count = 0
  var i = 0

  bind(classOf[Batch], batch)

  def batch(msg: AnyRef, rf: Any => Unit) {
    val req = msg.asInstanceOf[Batch]
    count = req.c
    i = req.c -1
    echo(Ping()) {
      rsp => processResponse(rf)
    }
  }

  private def dummy(rf: Any => Unit) {
    processResponse(rf)
  }

  @tailrec private def processResponse(rf: Any => Unit) {
    if (i < 1) {
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
