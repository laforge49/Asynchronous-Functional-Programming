package org.agilewiki.blip
package threads.threadMsgBurst

import org.specs.SpecificationWithJUnit
import messenger.ConcurrentLinkedBlockingQueue

class Echo extends Thread {
  val q = new ConcurrentLinkedBlockingQueue[AnyRef]

  override def run {
    while (true) {
      val m = q.take
      if (!m.isInstanceOf[ThreadMsgBurstTest]) return
      val t = m.asInstanceOf[ThreadMsgBurstTest]
      t.q.put(this)
    }
  }
}

class ThreadMsgBurstTest extends SpecificationWithJUnit {
  val q = new ConcurrentLinkedBlockingQueue[AnyRef]
  "ThreadMsgBurstTest" should {
    "time msg passing" in {
      val e = new Echo
      e.start
      var c = 1 //make this number bigger for a real test
      while (c > 0) {
        c -= 1
        var d = 1000
        while (d > 0) {
          d -= 1
          e.q.put(this)
        }
        d = 1000
        while (d > 0) {
          d -= 1
          q.take
        }
      }
      e.q.put("die")
    }
  }
}
