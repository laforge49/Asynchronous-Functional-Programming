package org.agilewiki.blip.messenger

import java.util.concurrent._
import annotation.tailrec

/**
 * A ConcurrentLinkedQueue with a take method that doesn't block when the queue isn't empty.
 * Note that this code only supports a singe reader thread.
 */
class ConcurrentLinkedBlockingQueue[E]
  extends ConcurrentLinkedQueue[E] {
  private val waiting = new atomic.AtomicBoolean //when true, take is requesting a permit
  private val wakeup = new Semaphore(0) //to wake up a pending take

  /**
   * Inserts the element at the tail of the queue.
   */
  def put(e: E) {
    offer(e)
  }

  /**
   * Inserts the element at the tail of the queue.
   * As the queue is unbounded, this method will never return {@code false}.
   */
  override def offer(e: E) = {
    super.offer(e)
    if (waiting.compareAndSet(true, false)) wakeup.release //if there is a pending take, wake it up
    true
  }

  /**
   * Returns the element at head of the queue when an element is available.
   * This method is similar to poll, except that it does not return null.
   */
  @tailrec final def take(): E = {
    var rv = poll
    if (rv != null) return rv
    //the queue may now be empty, so request a permit
    waiting.set(true)
    rv = poll
    if (rv != null) {
      //the queue was not empty
      if (!waiting.compareAndSet(true, false)) wakeup.drainPermits //clear the permit that we didn't need
      return rv
    }
    wakeup.acquire //wait for a permit
    take
  }
}
