package org.agilewiki.blip
package intro.loopTiming

import org.specs.SpecificationWithJUnit
import bind._
import seq._

/**
 * Suggested by Alex Cruise
 * See also http://www.krazykoding.com/2011/07/scala-actor-v-erlang-genserver.html
 */
class LoopTimingTest extends SpecificationWithJUnit {
  "LoopTimingTest" should {
    "time CounterActor" in {
      val msgCount = 100000000
      val increment = 100
      val counterActor = new CounterActor
      val countSafe = new MessageLogic {
        override def func(target: BindActor, msg: AnyRef, rf: Any => Unit)
                         (implicit sender: ActiveActor) {
          counterActor(AddCount(increment)) {
            rsp => rf(true)
          }
        }
      }
      val range = new Range(0, msgCount)
      val chain = new Chain
      chain.op(range, LoopSafe(countSafe))
      chain.op(counterActor, GetAndReset())
      val t0 = System.currentTimeMillis
      val count = Future(counterActor, chain).asInstanceOf[Long]
      val t1 = System.currentTimeMillis
      count must be equalTo (msgCount * 1L * increment)
      val elapsedTime = t1 - t0
      printf("Test took %s seconds for %s messages%n", elapsedTime/1000., msgCount)
      if (t1 != t0)
        printf("Throughput=%s per sec%n", msgCount*1000L / elapsedTime)
    }
    /*
    Output:
    Test took 36.561 seconds for 100000000 messages
    Throughput=2735154 per sec
    */
  }
}
