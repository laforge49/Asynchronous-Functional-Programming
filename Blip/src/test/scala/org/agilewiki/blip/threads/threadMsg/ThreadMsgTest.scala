package org.agilewiki.blip
package threads.threadMsg

import org.specs.SpecificationWithJUnit

class Echo extends Thread {
  val q = new ConcurrentLinkedBlockingQueue[AnyRef]

  override def run {
    while(true) {
      val m = q.take
      if (!m.isInstanceOf[ThreadMsgTest]) return
      val t = m.asInstanceOf[ThreadMsgTest]
      t.q.put(this)
    }
  }
}

class ThreadMsgTest extends SpecificationWithJUnit {
  val q = new ConcurrentLinkedBlockingQueue[AnyRef]
  "ThreadMsgTest" should {
    "time msg passing" in {
      val e = new Echo
      e.start
      var c = 1000 //make this number bigger for a real test
      while (c > 0) {
        c -= 1
        e.q.put(this)
        q.take
      }
      e.q.put("die")
    }
  }
}
