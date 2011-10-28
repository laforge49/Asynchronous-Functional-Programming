package org.agilewiki.blip

import java.util.concurrent._
import annotation.tailrec

/**
 * A ConcurrentLinkedQueue with a take method that doesn't block when the queue isn't empty.
 * Note that this code only supports a singe reader thread.
 */
class ConcurrentLinkedBlockingQueue[E]
  extends ConcurrentLinkedQueue[E] {
  private val ab = new atomic.AtomicBoolean //when true, take is requesting a permit
  private val s = new Semaphore(1) //to wake up a pending take

  s.drainPermits //start with no permits

  def put(e: E) {
    offer(e)
  }

  override def offer(e: E) = {
    super.offer(e)
    if (ab.compareAndSet(true, false)) s.release //if there is a pending take, wake it up
    true
  }

  @tailrec final def take(): E = {
    var rv = poll
    if (rv != null) return rv
    //the queue may now be empty, so request a permit
    ab.set(true)
    rv = poll
    if (rv != null) { //the queue was not empty
      if (!ab.compareAndSet(true, false)) s.drainPermits //clear the permit that we didn't need
      return rv
    }
    s.acquire //wait for a permit
    take
  }
}
