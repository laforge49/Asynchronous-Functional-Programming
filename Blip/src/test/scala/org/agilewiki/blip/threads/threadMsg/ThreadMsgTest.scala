package org.agilewiki.blip.threads.threadMsg

import org.specs.SpecificationWithJUnit

class Echo extends Thread {
  val q = new java.util.concurrent.LinkedBlockingQueue[AnyRef]

  override def run {
    while(true) {
      val m = q.take()
      if (!m.isInstanceOf[ThreadMsgTest]) return
      val t = m.asInstanceOf[ThreadMsgTest]
      t.q.put(this)
    }
  }
}

class ThreadMsgTest extends SpecificationWithJUnit {
  val q = new java.util.concurrent.LinkedBlockingQueue[AnyRef]
  "ThreadMsgTest" should {
    "time msg passing" in {
      val e = new Echo
      e.start
      var c = 1000
      while (c > 0) {
        c -= 1
        e.q.put(this)
        q.take
      }
      e.q.put("die")
    }
  }
}
