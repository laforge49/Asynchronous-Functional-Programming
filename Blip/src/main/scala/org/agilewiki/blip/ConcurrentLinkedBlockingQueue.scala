package org.agilewiki.blip

import java.util.concurrent._
import annotation.tailrec

class ConcurrentLinkedBlockingQueue[E]
  extends ConcurrentLinkedQueue[E] {
  private val ab = new atomic.AtomicBoolean
  private val s = new Semaphore(1)

  s.drainPermits

  def put(e: E) {
    offer(e)
  }

  override def offer(e: E) = {
    super.offer(e)
    if (ab.compareAndSet(true, false)) s.release
    true
  }

  @tailrec final def take(): E = {
    var rv = poll
    if (rv != null) return rv
    ab.set(true)
    rv = poll
    if (rv != null) {
      if (!ab.compareAndSet(true, false)) s.drainPermits
      return rv
    }
    s.acquire
    take
  }
}
