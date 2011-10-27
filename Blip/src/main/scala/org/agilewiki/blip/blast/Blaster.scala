package org.agilewiki.blip.blast

import java.util.concurrent._
import atomic.AtomicReference

trait Blaster extends Runnable with Callable[Blaster] {
  private val lbq = new LinkedBlockingQueue[Any]
  protected val abr = new AtomicReference[Blaster]
  private val s = new Semaphore(1)
  private var depth = 0
  private var active = true

  case class Death()

  final override def call = this

  override final def run() {
    while (true) {
      var msg = lbq.take
      while (!abr.compareAndSet(null, this)) {
        s.acquire
        s.release
      }
      depth = 0
      try {
        if (msg.isInstanceOf[Death]) {
          active = false
          return
        }
        dispatch(msg)
        while (lbq.size() > 0) {
          msg = lbq.take
          if (msg.isInstanceOf[Death]) {
            active = false
            return
          }
          dispatch(msg)
        }
      } finally {
        abr.set(null)
      }
    }
  }

  def close {lbq.put(Death())}

  final def sendTo(target: Blaster, msg: Any) {
    target.send(msg, abr.get)
  }

  private[blast] final def send(msg: Any, sender: Blaster) {
    if (!active) return
    if (sender == abr.get) {
      if (this == sender) dispatch(msg)
      else if (depth > maxDepth) lbq.put(msg)
      else {
        depth += 1
        try {
          dispatch(msg)
        } finally {
          depth -= 1
        }
      }
    } else if (!abr.compareAndSet(null, sender)) lbq.put(msg)
    else {
      s.acquire
      depth = 0
      try {
        dispatch(msg)
      } finally {
        abr.set(null)
        s.release
      }
    }
  }

  protected def maxDepth = 200 //limit recursion

  protected def forceAsync = false //Return true when doing I/O.

  protected def dispatch(msg: Any)
}