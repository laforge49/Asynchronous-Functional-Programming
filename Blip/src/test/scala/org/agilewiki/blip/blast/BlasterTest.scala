package org.agilewiki.blip.blast

import org.specs.SpecificationWithJUnit

class Echo extends Thread with Blaster {
  override protected def dispatch(msg: Any) {
    sendTo(msg.asInstanceOf[Blaster], this)
  }
}

class BlasterTest extends SpecificationWithJUnit with Blaster {
  val e = new Echo
  var c = 10000000 //make this number bigger for a real test
  e.start

  override protected def dispatch(msg: Any) {
    if (c > 0) {
      c -= 1
      sendTo(e, this)
    } else {
      e.close
      close
    }
  }

  "BlasterTest" should {
    "time msg passing" in {
      abr.set(this)
      sendTo(e, this)
      abr.set(null)
      run
    }
  }
}
